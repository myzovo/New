-- 启用向量扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 笔记向量表
CREATE TABLE IF NOT EXISTS note_embeddings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    note_id TEXT NOT NULL UNIQUE,
    title TEXT,
    content TEXT NOT NULL,
    tags TEXT[],
    embedding VECTOR(2048),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 创建向量检索索引
CREATE INDEX IF NOT EXISTS idx_note_embeddings_vector
ON note_embeddings USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- 创建相似度检索函数
CREATE OR REPLACE FUNCTION match_notes(
    query_embedding VECTOR(2048),
    match_count INT DEFAULT 5,
    match_threshold FLOAT DEFAULT 0.7
)
RETURNS TABLE (
    id UUID,
    note_id TEXT,
    title TEXT,
    content TEXT,
    similarity FLOAT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        ne.id,
        ne.note_id,
        ne.title,
        ne.content,
        1 - (ne.embedding <=> query_embedding) AS similarity
    FROM note_embeddings ne
    WHERE 1 - (ne.embedding <=> query_embedding) > match_threshold
    ORDER BY ne.embedding <=> query_embedding
    LIMIT match_count;
END;
$$;
