#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse, os, re, unicodedata, sys
from pathlib import Path
import ujson as json
from tqdm import tqdm
import numpy as np, faiss
from sentence_transformers import SentenceTransformer
from pdfminer.high_level import extract_text as pdf_extract_text
import platform

IS_WINDOWS = platform.system().lower().startswith("win")

def normalize(t: str) -> str:
    t = unicodedata.normalize("NFKC", t)
    t = re.sub(r"\s+", " ", t).strip()
    return t

def to_long_path(p: Path) -> str:
    """
    윈도우에서 긴 경로/특수문자 이슈를 줄이기 위해 \\?\ 프리픽스 부여.
    다른 OS에서는 str(p) 그대로 반환.
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
    t = normalize(t)
    if not t:
        return []
    out, s = [], 0
    ov = int(max_chars * overlap)
    while s < len(t):
        e = min(len(t), s + max_chars)
        cut = t.rfind(". ", s, e)
        if cut == -1 or cut < s + 0.6 * max_chars:
            cut = e
        out.append(t[s:cut].strip())
        s = max(cut - ov, s + 1)
    return [c for c in out if len(c) >= 40]

def read_jsonl(path: Path, debug=False):
    items = []
    try:
        with path.open("r", encoding="utf-8") as f:
            for line in f:
                if line.strip():
                    try:
                        items.append(json.loads(line))
                    except Exception as e:
                        if debug:
                            print(f"[warn] JSONL decode error @ {path}: {e}")
    except OSError as e:
        if debug:
            print(f"[warn] JSONL open error @ {path}: {e}")
    return items

def collect_docs_auto(scan_dir: Path, recursive: bool, print_files=False, debug=False):
    docs = []
    pat = "**/*" if recursive else "*"
    # 확장자별 목록 구성 (대괄호/괄호/한글 포함 파일도 안전하게 수집)
    txts = sorted(scan_dir.glob(pat + ".txt"))
    pdfs = sorted(scan_dir.glob(pat + ".pdf"))
    jsonls = sorted(scan_dir.glob(pat + ".jsonl"))

    if print_files:
        print("[files] TXT:")
        for p in txts: print("  ", p)
        print("[files] PDF:")
        for p in pdfs: print("  ", p)
        print("[files] JSONL:")
        for p in jsonls: print("  ", p)

    # TXT
    for tp in txts:
        try:
            text = tp.read_text(encoding="utf-8")
        except Exception:
            try:
                text = tp.read_text(encoding="utf-8", errors="ignore")
            except OSError as e:
                if debug:
                    print(f"[warn] TXT read failed @ {tp}: {e}")
                continue
        docs.append({"id": tp.stem, "title": tp.stem.replace("_"," "),
                     "text": text, "meta": {"source": str(tp), "type": "txt"}})

    # PDF
    for pp in pdfs:
        try:
            # 윈도우 긴 경로 대응
            text = pdf_extract_text(to_long_path(pp)) or ""
        except Exception as e:
            if debug:
                print(f"[warn] PDF parse failed @ {pp}: {e}")
            text = ""
        docs.append({"id": pp.stem, "title": pp.stem,
                     "text": text, "meta": {"source": str(pp), "type": "pdf"}})

    # JSONL
    for jp in jsonls:
        rows = read_jsonl(jp, debug=debug)
        for it in rows:
            docs.append({
                "id": it.get("id", jp.stem), "title": it.get("title", ""),
                "text": it.get("text", ""),
                "meta": {"source": str(jp), **(it.get("meta", {}))}
            })

    return docs

def embed_passages(model, chunks, is_e5=True, batch_size=128):
    inputs = [f"passage: {c}" for c in chunks] if is_e5 else chunks
    embs = model.encode(inputs, batch_size=batch_size,
                        normalize_embeddings=True, show_progress_bar=True)
    return np.asarray(embs, dtype="float32")

def build_and_save_index(chunks, metas, model_name, batch_size, out_dir, debug=False):
    print(f"[embed] model={model_name}")
    model = SentenceTransformer(model_name)
    is_e5 = "e5" in model_name.lower()

    X = embed_passages(model, chunks, is_e5=is_e5, batch_size=batch_size)
    d = X.shape[1]
    print(f"[index] dim={d}, n_chunks={len(chunks)}")

    index = faiss.IndexFlatIP(d)
    index.add(X)

    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)

    # 윈도우에서 긴 경로 저장 이슈 최소화
    faiss_path = out / "faiss.index"
    meta_path = out / "meta.json"
    faiss.write_index(index, to_long_path(faiss_path))
    meta_path.write_text(json.dumps({"chunks": chunks, "meta": metas}, ensure_ascii=False), encoding="utf-8")

    print(f"[save] {faiss_path}")
    print(f"[save] {meta_path}")
    return out

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out_dir", required=True)
    ap.add_argument("--model", default="intfloat/multilingual-e5-base")
    ap.add_argument("--max_chars", type=int, default=1000)
    ap.add_argument("--overlap", type=float, default=0.15)
    ap.add_argument("--batch_size", type=int, default=128)
    ap.add_argument("--scan_dir", default=".")
    ap.add_argument("--no_recursive", action="store_true")
    ap.add_argument("--print_files", action="store_true", help="스캔된 파일 목록만 먼저 출력")
    ap.add_argument("--debug", action="store_true")
    args = ap.parse_args()

    scan_dir = Path(args.scan_dir).resolve()
    recursive = not args.no_recursive
    print(f"[scan] base_dir={scan_dir}")
    print(f"[scan] recursive={recursive}")

    docs = collect_docs_auto(scan_dir, recursive=recursive, print_files=args.print_files, debug=args.debug)
    print(f"[collect] docs={len(docs)}")
    if not docs:
        raise SystemExit("No documents found (.txt/.pdf/.jsonl). Check scan_dir or options.")

    # 청킹
    chunks, metas = [], []
    for d in tqdm(docs, desc="[chunk]"):
        text = d.get("text") or ""
        if not text.strip():
            continue
        parts = chunk_text(text, args.max_chars, args.overlap)
        for i, c in enumerate(parts):
            chunks.append(c)
            metas.append({**d["meta"], "doc_id": d["id"], "title": d["title"], "chunk_id": i})

    print(f"[chunk] chunks={len(chunks)}")
    if not chunks:
        raise SystemExit("No chunks produced. Check input documents or chunking params.")

    build_and_save_index(chunks, metas, args.model, args.batch_size, args.out_dir, debug=args.debug)

if __name__ == "__main__":
    main()


# python .\make_embedding.py --out_dir .\out\index-v1 --scan_dir . --debug