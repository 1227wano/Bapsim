- rag context 사용여부

  ```bash
  curl -sS --json '{"user_id":"u1","message":"오늘 학식 뭐야?","language":"ko"}' http://localhost:8000/chat \
  | jq '.meta.rag_used, (.meta.rag_sources | length // 0)'
  ```



- 인덱스 / 메타 크기 확인

  ```bash
  ```

  

- 스코어 / 인덱스 확인

  ```bash
  docker exec bmpm_ai sh -lc 'python -c "import os,re,unicodedata,json,faiss,numpy as np;from pathlib import Path;from sentence_transformers import SentenceTransformer as S;norm=lambda t:re.sub(r\"\\s+\",\" \",unicodedata.normalize(\"NFKC\",t)).strip();d=Path(os.getenv(\"RAG_INDEX_DIR\",\"./out/index-v1\"));idx=faiss.read_index(str(d/\"faiss.index\"));model=S(os.getenv(\"EMBED_MODEL_NAME\",\"intfloat/multilingual-e5-base\"));q=model.encode([f\"query: {norm(\"오늘 학식 뭐야?\")}\"],normalize_embeddings=True);D,I=idx.search(np.asarray(q,dtype=\"float32\"),5);print(\"scores\",D[0].tolist());print(\"idx\",I[0].tolist())"'
  ```

  