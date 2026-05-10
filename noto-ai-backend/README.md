# Noto AI Backend

> AI 智能笔记后端服务 —— 为 Noto 笔记 App 提供 OCR 识别、语义检索、AI 问答能力
>
> AI-powered note-taking backend — provides OCR, semantic search, and RAG-based Q&A for the Noto app

---

## Features / 功能

| Feature | Description | Dependency |
|---------|-------------|------------|
| **OCR Recognition / OCR 识别** | Upload an image, extract text automatically / 拍照或选图，自动提取文字 | Baidu OCR API |
| **Vectorization / 笔记向量化** | Convert note content into vectors and store in DB / 笔记内容自动转为向量存入数据库 | Zhipu AI Embedding |
| **Semantic Search / 语义检索** | Search notes using natural language / 用自然语言搜索最相关的笔记 | pgvector |
| **AI Q&A / AI 问答** | Answer questions based on your notes / 基于笔记内容智能回答问题 | DeepSeek LLM |

**Workflow / 工作流程：**

```
Take photo ──→ OCR extracts text ──→ Save note ──→ Auto-vectorize & store
                                                      │
Ask a question ──→ Vectorize question ──→ Semantic search ──→ LLM generates answer
拍照上传 ──→ OCR 提取文字 ──→ 保存笔记 ──→ 自动向量化入库
                                              │
用户提问 ──→ 问题向量化 ──→ 语义检索相关笔记 ──→ LLM 生成回答
```

---

## Tech Stack / 技术栈

| Layer / 层级 | Technology / 技术 |
|--------------|-------------------|
| Framework / 框架 | Spring Boot 3.2.4 / Java 17 |
| Database / 数据库 | PostgreSQL (Supabase) + pgvector extension |
| OCR | Baidu OCR API / 百度 OCR API |
| Embedding | Zhipu AI `embedding-3` / 智谱 AI |
| LLM | DeepSeek Chat |
| Deployment / 部署 | Docker / Docker Compose |

---

## Quick Start / 快速开始

### Prerequisites / 环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL database (recommended: [Supabase](https://supabase.com) free tier)
- PostgreSQL 数据库（推荐 Supabase 免费版）

### Step 1: Clone / 克隆项目

```bash
git clone <repository-url>
cd noto-ai-backend
```

### Step 2: Configure / 配置密钥

Copy the template and fill in your own keys:
复制模板并填入你自己的密钥：

```bash
cp .env.example .env
```

`.env` file contents / `.env` 文件内容：

```ini
# Database (Supabase or local PostgreSQL)
# 数据库（Supabase 或本地 PostgreSQL）
DB_HOST=your-supabase-host.supabase.co
DB_PORT=5432
DB_NAME=postgres
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Baidu OCR / 百度 OCR (https://ai.baidu.com/tech/ocr)
OCR_API_KEY=your_key
OCR_SECRET_KEY=your_secret

# Zhipu AI / 智谱 AI (https://open.bigmodel.cn)
EMBEDDING_API_KEY=your_key

# DeepSeek (https://platform.deepseek.com)
LLM_API_KEY=your_key
```

> `.env` is excluded by `.gitignore` and will not be committed to Git.
>
> `.env` 已被 `.gitignore` 排除，不会提交到 Git。

### Step 3: Run / 启动服务

```bash
mvn spring-boot:run
```

Look for `Started NotoAiApplication` in the console output.
看到 `Started NotoAiApplication` 表示启动成功。

### Step 4: Verify / 验证

```bash
# Health check / 健康检查
curl http://localhost:8080/health

# API docs (open in browser) / API 文档（浏览器访问）
# http://localhost:8080/swagger-ui/index.html
```

---

## API Endpoints / API 接口

### OCR Recognition / OCR 识别

```
POST /api/ocr/recognize
Content-Type: multipart/form-data
```

```bash
curl -X POST http://localhost:8080/api/ocr/recognize -F "image=@photo.jpg"
```

```json
// Response / 响应
{ "success": true, "text": "Extracted text / 识别出的文字", "message": "Success / 识别成功" }
```

### Save Note / 保存笔记

```
POST /api/notes/save
Content-Type: application/json
```

```bash
curl -X POST http://localhost:8080/api/notes/save \
  -H "Content-Type: application/json" \
  -d '{"noteId":"1","title":"My Note","content":"Some content","tags":["demo"]}'
```

```json
// Response / 响应
{ "success": true, "message": "Note saved, indexing... / 笔记已保存，正在建立索引..." }
```

### AI Q&A / AI 问答

```
POST /api/chat/ask
Content-Type: application/json
```

```bash
curl -X POST http://localhost:8080/api/chat/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"What notes do I have?"}'
```

```json
// Response / 响应
{
  "answer": "Based on your notes... / 根据您的笔记...",
  "sources": [{ "noteId": "1", "title": "My Note", "similarity": 0.95 }]
}
```

---

## Docker Deployment / Docker 部署

```bash
# 1. Build / 打包
mvn clean package

# 2. Set environment variables / 设置环境变量
export DB_PASSWORD=your_password
export OCR_API_KEY=your_key
export OCR_SECRET_KEY=your_secret
export EMBEDDING_API_KEY=your_key
export LLM_API_KEY=your_key

# 3. Start / 启动
docker-compose up -d
```

Production / 生产环境：

```bash
java -jar app.jar --spring.profiles.active=prod
```

---

## Project Structure / 项目结构

```
noto-ai-backend/
├── src/main/java/com/notoai/
│   ├── controller/               # API layer / API 接口层
│   │   ├── OcrController             POST /api/ocr/recognize
│   │   ├── NoteController            POST /api/notes/save
│   │   └── ChatController            POST /api/chat/ask
│   ├── service/                  # Business logic / 业务逻辑层
│   │   ├── OcrService                Baidu OCR integration / 百度 OCR 调用
│   │   ├── EmbeddingService          Zhipu AI vectorization / 智谱 AI 向量化
│   │   ├── VectorDbService           pgvector read & write / pgvector 读写
│   │   ├── LlmService                DeepSeek chat / DeepSeek 对话
│   │   ├── RagService                RAG search + generation / RAG 检索+生成
│   │   └── NoteService               Note processing orchestration / 笔记处理编排
│   └── config/                   # Configuration / 配置
│       ├── AsyncConfig               Thread pool / 异步线程池
│       ├── CacheConfig               Caching / 缓存
│       ├── CorsConfig                CORS / 跨域
│       ├── RateLimitConfig           Rate limiting / 限流
│       └── GlobalExceptionHandler    Error handling / 统一错误处理
├── src/main/resources/
│   ├── application.yml               Dev config / 开发配置
│   ├── application-prod.yml          Prod config / 生产配置
│   └── db/migration/                 DB migrations / 数据库迁移脚本
├── .env.example                        Env template / 环境变量模板
├── Dockerfile
└── docker-compose.yml
```

---

## Configuration / 配置参考

| Key / 配置项 | Description / 说明 | Default / 默认值 |
|-------------|-------------------|-----------------|
| `server.port` | Server port / 服务端口 | `8080` |
| `DB_HOST` | Database host / 数据库地址 | `localhost` |
| `DB_PORT` | Database port / 数据库端口 | `5432` |
| `DB_NAME` | Database name / 数据库名 | `postgres` |
| `OCR_API_KEY` | Baidu OCR Key / 百度 OCR Key | - |
| `OCR_SECRET_KEY` | Baidu OCR Secret / 百度 OCR Secret | - |
| `EMBEDDING_API_KEY` | Zhipu AI Key / 智谱 AI Key | - |
| `LLM_API_KEY` | DeepSeek Key | - |
| `LLM_MODEL` | LLM model name / LLM 模型名 | `deepseek-chat` |

---

## Security / 安全提示

- **Never commit `.env` to Git** — it is excluded by `.gitignore`
- **永远不要提交 `.env` 文件到 Git** — 它已被 `.gitignore` 排除
- All API keys are injected via environment variables, never hardcoded
- 所有 API 密钥通过环境变量注入，不硬编码在代码中
- Use a secrets manager (Vault, AWS Secrets Manager) in production
- 生产环境建议使用密钥管理服务
- Rotate keys immediately if accidentally exposed
- 如果密钥意外泄露，立即轮换

---

## License / 许可证

MIT License
