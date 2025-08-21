'''
현재 파일의 경로에 위치한 .txt | .pdf | .jsonl 파일들의 내용을 chunking하여 벡터 임베딩을 만드는 코드입니다.
--out_dir 옵션으로 지정한 디렉토리 하위(out/index-v0)에 faiss.index와 meta.json이 함께 생성됩니다.

- 사용법
python .\make_embedding.py --out_dir .\out\index-v1 --scan_dir .
index-v 뒤에 숫자만 올려가며 버전 관리
'''

# -*- coding: utf-8 -*-

import argparse, re, unicodedata
from pathlib import Path
import ujson as json
from tqdm import tqdm
import numpy as np, faiss
from sentence_transformers import SentenceTransformer
from pdfminer.high_level import extract_text as pdf_extract_text
import platform

IS_WINDOWS = platform.system().lower().startswith("win")

# 파일 내용 청킹/토크나이징하기 전에 정규화
def normalize(t: str) -> str:
    t = unicodedata.normalize("NFKC", t)
    t = re.sub(r"\s+", " ", t).strip()
    return t

def to_long_path(p: Path) -> str:
    """
    윈도우에서 긴 경로/특수문자 이슈를 줄이기 위해 \\?\ prefix 부여
    다른 OS에서는 str(p) 그대로 반환함
    """
    s = str(p)

    if IS_WINDOWS:
        s = s.replace("/", "\\")
        if not s.startswith("\\\\?\\"):
            # 드라이브 경로만 지원 (UNC는 \\?\UNC\ 형태 필요)
            if re.match(r"^[A-Za-z]:\\", s):
                return "\\\\?\\" + s
    return s

def chunk_text(t: str, max_chars=1000, overlap=0.15):
    # 정규화
    t = normalize(t)
    # 파일에 내용이 없는 경우 처리
    if not t:
        return []
    out, s = [], 0 # out : chunking 결과 / s : 시작 위치
    ov = int(max_chars * overlap) # chunk 사이 겹치는 부분(overlap)의 크기
    while s < len(t):
        e = min(len(t), s + max_chars)
        # 되도록 문장 단위로 chunk하기 위해 . 탐색
        # .이 마침표 외의 역할을 할 경우 의도하지 않은 chunking이 일어날 수 있어 데이터 특성에 따라 로직 수정 필요할 듯
        cut = t.rfind(". ", s, e)
        # 구간내 .이 없는 경우 chunk의 길이가 너무 길어지지 않도록 끊어줌
        if cut == -1 or cut < s + 0.6 * max_chars:
            cut = e
        # chunk 저장
        out.append(t[s:cut].strip())
        # 다음 시작위치 지정. 이전 chunk의 끝부분에서 overlap만큼 앞으로 이동하여 다시 청킹 시작
        s = max(cut - ov, s + 1)
    # chunk 길이가 너무 짧은 경우는 버리고 반환
    # 매우 짧은 단어 또는 문장이 가진 의미가 중요한 데이터가 있다면 조건식 빼줘야 할 듯
    return [c for c in out if len(c) >= 10]

# jsonl 읽기
def read_jsonl(path: Path):
    items = []
    try:
        with path.open("r", encoding="utf-8") as f:
            for line in f:
                if line.strip():
                    try:
                        items.append(json.loads(line))
                    except Exception as e:
                        print(f"[warn] JSONL decode error @ {path}: {e}")
    except OSError as e:
        # 파일 오픈 자체가 실패했을 때는 조용히 스킵(혹은 경고)
        print(f"[warn] JSONL open error @ {path}: {e}")
    return items

# txt, pdf, jsonl 파일 내용 수집하는 함수
def collect_docs_auto(scan_dir: Path, recursive: bool):
    docs = []
    # recursive = True면 Path의 하위 디렉토리들까지 모두 검색
    pat = "**/*" if recursive else "*"
    # 확장자별 목록 구성(대괄호/괄호/한글 포함 파일도 안전하게 수집)
    txts = sorted(scan_dir.glob(pat + ".txt"))
    pdfs = sorted(scan_dir.glob(pat + ".pdf"))
    jsonls = sorted(scan_dir.glob(pat + ".jsonl"))

    # TXT
    for tp in txts:

        # utf-8 인코딩으로 읽기
        try:
            text = tp.read_text(encoding="utf-8")
        # utf-8로 못 읽으면..
        except Exception:
            # error 무시 옵션 주고 다시 읽어보기
            try:
                text = tp.read_text(encoding="utf-8", errors="ignore")
            # 그래도 못 읽으면..
            except OSError as e:
                # 다음 파일로 넘어감
                continue

        # 표준 포맷에 append
        docs.append({"id": tp.stem, "title": tp.stem.replace("_"," "),
                     "text": text, "meta": {"source": str(tp), "type": "txt"}})

    # PDF
    for pp in pdfs:
        try:
            # 윈도우 긴 경로 대응
            # pdfminer 모듈을 사용하여 pdf 내 텍스트 추출
            # OCR은 없음
            text = pdf_extract_text(to_long_path(pp)) or ""
        # 안 되면 넘어감
        except Exception as e:
            continue

        # 표준 포맷에 append
        docs.append({"id": pp.stem, "title": pp.stem,
                     "text": text, "meta": {"source": str(pp), "type": "pdf"}})

    # JSONL
    for jp in jsonls:
        rows = read_jsonl(jp)
        for it in rows:
            docs.append({
                "id": it.get("id", jp.stem), # jsonl 내부에 id 필드 없으면 파일명 사용
                "title": it.get("title", ""), # title 필드
                "text": it.get("text", ""), # text 필드
                "meta": {"source": str(jp), **(it.get("meta", {}))} # meta 필드는 언패킹하여 병합
            })

    # dict의 list 반환
    return docs

def embed_passages(model, chunks, is_e5=True, batch_size=128):
    # e5 계열 모델을 사용하므로 포맷 맞춰주기
    # 다른 모델 사용하려고 할 경우 그에 맞는 조건식/포맷 추가해줘야 함
    inputs = [f"passage: {c}" for c in chunks] if is_e5 else chunks
    # sentence-transformers 모델로 벡터화
    # OOM 발생시 batch_size 줄여서 테스트할 것
    embs = model.encode(inputs, batch_size=batch_size,
                        normalize_embeddings=True, show_progress_bar=True)
    # 메모리 효율성을 위해 float32로 저장
    return np.asarray(embs, dtype="float32")

def build_and_save_index(chunks, metas, model_name, batch_size, out_dir):

    # 임베딩할 모델명 출력
    print(f"[embed] model={model_name}")
    # 모델 로드
    model = SentenceTransformer(model_name)
    # e5계열 모델인지 확인
    # 다른 모델 사용시 관련 분기처리 필요
    is_e5 = "e5" in model_name.lower()

    # 벡터화
    X = embed_passages(model, chunks, is_e5=is_e5, batch_size=batch_size)
    d = X.shape[1] # 벡터의 차원수
    print(f"[index] dim={d}, n_chunks={len(chunks)}")

    # faiss 인덱스 생성
    index = faiss.IndexFlatIP(d) # flat index. 코사인 유사도와 호환됨
    index.add(X)

    # 결과물 저장할 디렉토리 생성
    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)

    # 윈도우에서 긴 경로 저장 이슈 최소화
    faiss_path = out / "faiss.index" # 벡터 인덱스 파일
    meta_path = out / "meta.json" # chunk 및 메타데이터 파일

    # index 파일 저장
    faiss.write_index(index, to_long_path(faiss_path))

    # 청크 + 메타데이터 JSON 파일로 저장 (ensure_ascii=False로 한글 깨짐 방지)
    meta_path.write_text(json.dumps({"chunks": chunks, "meta": metas}, ensure_ascii=False), encoding="utf-8")

    # 저장 완료 로그 출력
    print(f"[save] {faiss_path}")
    print(f"[save] {meta_path}")
    return out

def main():

    # 함수 실행시 옵션 정의
    ap = argparse.ArgumentParser()

    # 결과물 저장 디렉토리 지정(필수 옵션)
    ap.add_argument("--out_dir", required=True)
    # 사용할 임베딩 모델명(default: e5-base)
    ap.add_argument("--model", default="intfloat/multilingual-e5-base")
    # chunk(문자 단위) 최대 길이
    ap.add_argument("--max_chars", type=int, default=1000)
    # overlap 비율
    ap.add_argument("--overlap", type=float, default=0.15)
    # batch size
    ap.add_argument("--batch_size", type=int, default=128)
    # 문서 읽어올 경로(default: 현재 디렉토리)
    ap.add_argument("--scan_dir", default=".")
    # 하위 디렉토리까지 탐색할 것인지 여부(default: recursive)
    ap.add_argument("--no_recursive", action="store_true")

    args = ap.parse_args()

    # 디렉토리 설정
    # 절대경로로 변환
    scan_dir = Path(args.scan_dir).resolve()
    # recursive 옵션 변환
    recursive = not args.no_recursive
    # 디렉토리 설정 로그 출력
    print(f"[scan] base_dir={scan_dir}")
    print(f"[scan] recursive={recursive}")

    # 문서 수집
    docs = collect_docs_auto(scan_dir, recursive=recursive)
    # 수집한 문서 수 출력
    print(f"[collect] docs={len(docs)}")
    # 문서가 하나도 없으면 종료
    if not docs:
        raise SystemExit("No documents found (.txt/.pdf/.jsonl). Check scan_dir or options.")

    # 청킹
    chunks, metas = [], []
    for d in tqdm(docs, desc="[chunk]"): # tqdm으로 progress bar 표시
        text = d.get("text") or "" # 문서에서 text 추출
        # 추출한 text에 공백만 있는 경우 그냥 넘어가기
        if not text.strip():
            continue
        # chunk
        parts = chunk_text(text, args.max_chars, args.overlap)
        # 나눠진 chunk들을 각각 list에 append
        for i, c in enumerate(parts):
            chunks.append(c)
            metas.append(
                {**d["meta"], # 원본 문서의 메타 데이터
                 "doc_id": d["id"], # 문서 id
                 "title": d["title"], # 문서 제목
                 "chunk_id": i}) # 문서 내 chunk의 id

    # chunk 수 출력
    print(f"[chunk] chunks={len(chunks)}")
    # chunk가 하나도 없는 경우 종료
    if not chunks:
        raise SystemExit("No chunks produced. Check input documents or chunking params.")

    # 완성된 chunk와 메타 데이터로 faiss index 데이터 생성
    build_and_save_index(chunks, metas, args.model, args.batch_size, args.out_dir)

if __name__ == "__main__":
    main()


