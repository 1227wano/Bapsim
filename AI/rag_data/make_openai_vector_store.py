import requests
from io import BytesIO
from typing import List
from openai import OpenAI
import os
from dotenv import load_dotenv

load_dotenv()

client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

def create_file(client: OpenAI, file_path: str) -> str:
    """
    공식 문서 스타일 그대로: files.create 로 '단일 파일' 업로드.
    - URL: 다운로드 후 (filename, BytesIO) 튜플로 업로드
    - 로컬 경로: 파일 핸들 그대로 업로드
    반환값: file_id (e.g., 'file_***')
    """
    if file_path.startswith(("http://", "https://")):
        resp = requests.get(file_path)
        resp.raise_for_status()
        file_content = BytesIO(resp.content)
        file_name = file_path.split("/")[-1] or "remote_file"
        result = client.files.create(
            file=(file_name, file_content),
            purpose="assistants",
        )
    else:
        with open(file_path, "rb") as f:
            result = client.files.create(
                file=f,
                purpose="assistants",
            )
    print("uploaded file_id:", result.id)
    return result.id

def create_vector_store_with_files(client: OpenAI, name: str, file_paths: List[str]) -> str:
    """
    1) 파일들을 files.create 로 각각 업로드 (공식 문서 스타일)
    2) 업로드된 file_id 리스트로 '하나의 Vector Store' 생성
       (인덱싱은 비동기적으로 진행됨)
    반환값: vector_store_id (e.g., 'vs_***')
    """
    file_ids = [create_file(client, p) for p in file_paths]

    vs = client.vector_stores.create(
        name=name,
        file_ids=file_ids,  # 여러 파일을 한 번에 스토어에 연결
    )
    print("vector_store_id:", vs.id)
    return vs.id

if __name__ == "__main__":

    paths = [
        "C:\\Users\\SSAFY\\Desktop\\workspaces\\Bapsim\\AI\\rag_data\\Cafeteria_Handbook_KO.pdf",
        "C:\\Users\\SSAFY\\Desktop\\workspaces\\Bapsim\\AI\\rag_data\\new_events.txt",
        "C:\\Users\\SSAFY\\Desktop\\workspaces\\Bapsim\\AI\\rag_data\\new_policies.txt",
        "C:\\Users\\SSAFY\\Desktop\\workspaces\\Bapsim\\AI\\rag_data\\new_user_guides.txt"
    ]

    vs_id = create_vector_store_with_files(client, "my-knowledge-base", paths)