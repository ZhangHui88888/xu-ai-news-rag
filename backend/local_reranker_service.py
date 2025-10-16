"""
本地重排模型服务
无需 API Key，完全免费
"""
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sentence_transformers import CrossEncoder
from typing import List
import uvicorn

app = FastAPI(title="本地重排服务")

# 加载重排模型（首次运行会自动下载）
print("正在加载重排模型...")
reranker = CrossEncoder('BAAI/bge-reranker-v2-m3')
print("重排模型加载完成！")

class RerankRequest(BaseModel):
    query: str
    documents: List[str]
    top_n: int = 5

class RerankResult(BaseModel):
    index: int
    relevance_score: float

@app.post("/v1/rerank")
async def rerank(request: RerankRequest):
    """重排接口"""
    try:
        # 构建查询-文档对
        pairs = [[request.query, doc] for doc in request.documents]
        
        # 计算相关性分数
        scores = reranker.predict(pairs)
        
        # 排序并返回 Top N
        results = []
        for idx, score in enumerate(scores):
            results.append({
                "index": idx,
                "relevance_score": float(score)
            })
        
        # 按分数降序排序
        results.sort(key=lambda x: x["relevance_score"], reverse=True)
        results = results[:request.top_n]
        
        return {
            "model": "BAAI/bge-reranker-v2-m3",
            "results": results
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health():
    return {"status": "ok", "model": "BAAI/bge-reranker-v2-m3"}

if __name__ == "__main__":
    print("=" * 50)
    print("本地重排服务启动中...")
    print("地址: http://localhost:8000")
    print("文档: http://localhost:8000/docs")
    print("=" * 50)
    uvicorn.run(app, host="0.0.0.0", port=8000)

