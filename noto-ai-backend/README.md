# Noto AI Backend

AI智能笔记App的Java Spring Boot后端服务，提供OCR识别、向量化存储、RAG问答等核心功能。

## 架构概览

```
┌─────────────────┐         ┌─────────────────────────────────────┐
│   Noto App      │         │        Java Spring Boot 后端         │
│   (Kotlin)      │  HTTP   │                                     │
│                 │ ──────► │  /api/ocr      → 调云端OCR API      │
│  · 拍照/选图    │         │  /api/notes    → 处理+存储笔记       │
│  · 展示笔记    │ ◄────── │  /api/chat     → RAG问答             │
│  · 聊天界面    │         │                                     │
│                 │         │         ┌─────────────────┐         │
└─────────────────┘         │         │   Supabase      │         │
                            │         │  · PostgreSQL   │         │
                            │         │  · pgvector     │         │
                            │         └─────────────────┘         │
                            └─────────────────────────────────────┘
```

## 技术栈

- **框架**: Spring Boot 3.2.4
- **语言**: Java 17
- **数据库**: PostgreSQL (Supabase)
- **向量存储**: pgvector
- **OCR**: 百度OCR API
- **Embedding**: 智谱AI embedding-3
- **LLM**: DeepSeek Chat

## 项目结构

```
noto-ai-backend/
├── src/main/java/com/notoai/
│   ├── NotoAiApplication.java          # 启动类
│   ├── controller/
│   │   ├── OcrController.java          # OCR识别接口
│   │   ├── NoteController.java         # 笔记管理接口
│   │   └── ChatController.java         # AI对话接口
│   ├── service/
│   │   ├── OcrService.java             # OCR业务逻辑
│   │   ├── EmbeddingService.java       # 向量化服务
│   │   ├── VectorDbService.java        # 向量数据库操作
│   │   ├── LlmService.java             # 大模型调用
│   │   ├── RagService.java             # RAG问答核心
│   │   └── NoteService.java            # 笔记处理服务
│   ├── model/dto/
│   │   ├── OcrResponse.java
│   │   ├── ChatRequest.java
│   │   ├── ChatResponse.java
│   │   ├── NoteRequest.java
│   │   ├── NoteResponse.java
│   │   └── SearchResult.java
│   └── config/
│       ├── AsyncConfig.java            # 异步线程池配置
│       ├── CacheConfig.java            # 缓存配置
│       ├── CorsConfig.java             # 跨域配置
│       ├── GlobalExceptionHandler.java # 全局异常处理
│       ├── RateLimitConfig.java        # 限流配置
│       ├── SwaggerConfig.java          # API文档配置
│       └── WebMvcConfig.java           # Web配置
├── src/main/resources/
│   ├── application.yml                 # 开发环境配置
│   ├── application-prod.yml            # 生产环境配置
│   └── db/migration/
│       └── V1__create_note_embeddings_table.sql
├── pom.xml
├── Dockerfile
└── docker-compose.yml
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL (或Supabase账号)

### 1. 克隆项目

```bash
git clone <repository-url>
cd noto-ai-backend
```

### 2. 配置环境变量

**重要：所有API密钥必须通过环境变量配置，不要提交到代码仓库！**

复制环境变量模板：
```bash
cp .env.example .env
```

编辑 `.env` 文件，填入你的真实密钥：
```bash
# 数据库配置
SPRING_DATASOURCE_URL=jdbc:postgresql://your-host:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=your_database_password

# 百度OCR
OCR_API_KEY=your_baidu_ocr_api_key
OCR_SECRET_KEY=your_baidu_ocr_secret_key

# 智谱AI Embedding
EMBEDDING_API_KEY=your_zhipu_api_key

# DeepSeek LLM
LLM_API_KEY=your_deepseek_api_key
```

> `.env` 文件已在 `.gitignore` 中，不会被提交到Git仓库。

### 3. 运行项目

```bash
# 开发环境
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/noto-ai-backend-0.0.1-SNAPSHOT.jar
```

服务启动后访问: http://localhost:8080

### 4. API文档

启动服务后访问Swagger UI查看完整API文档：
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## API接口

### OCR识别

```bash
POST /api/ocr/recognize
Content-Type: multipart/form-data

curl -X POST http://localhost:8080/api/ocr/recognize \
  -F "image=@test.jpg"
```

**响应:**
```json
{
  "success": true,
  "text": "识别出的文字内容",
  "message": "识别成功"
}
```

### 保存笔记

```bash
POST /api/notes/save
Content-Type: application/json

curl -X POST http://localhost:8080/api/notes/save \
  -H "Content-Type: application/json" \
  -d '{"noteId":"note-1","title":"测试笔记","content":"笔记内容","tags":["测试"]}'
```

**响应:**
```json
{
  "success": true,
  "message": "笔记已保存，正在建立索引..."
}
```

### AI问答

```bash
POST /api/chat/ask
Content-Type: application/json

curl -X POST http://localhost:8080/api/chat/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"我有哪些测试笔记？"}'
```

**响应:**
```json
{
  "answer": "根据您的笔记...",
  "sources": [
    {
      "noteId": "note-1",
      "title": "测试笔记",
      "similarity": 0.95
    }
  ]
}
```

## Docker部署

### 构建镜像

```bash
mvn clean package
docker build -t noto-ai-backend .
```

### 使用Docker Compose

```bash
# 设置环境变量
export DB_PASSWORD=your_password
export OCR_API_KEY=your_key
export OCR_SECRET_KEY=your_secret
export EMBEDDING_API_KEY=your_key
export LLM_API_KEY=your_key

# 启动服务
docker-compose up -d
```

### 生产环境部署

```bash
# 使用生产配置
java -jar app.jar --spring.profiles.active=prod
```

生产环境配置从环境变量读取所有密钥，确保设置以下变量：
- `SPRING_DATASOURCE_URL`
- `DB_PASSWORD`
- `OCR_API_KEY`
- `OCR_SECRET_KEY`
- `EMBEDDING_API_KEY`
- `LLM_API_KEY`

## 配置说明

### application.yml

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.port` | 服务端口 | 8080 |
| `spring.datasource.url` | 数据库连接URL | - |
| `notoai.ocr.api-key` | 百度OCR API Key | - |
| `notoai.ocr.secret-key` | 百度OCR Secret Key | - |
| `notoai.embedding.api-key` | 智谱AI API Key | - |
| `notoai.llm.api-key` | DeepSeek API Key | - |
| `notoai.llm.model` | LLM模型名称 | deepseek-chat |

## 核心功能

### OCR识别
- 支持拍照/选图上传
- 调用百度OCR API识别中英文
- 返回识别文字结果

### 向量化存储
- 使用智谱AI embedding-3模型
- 自动将笔记内容向量化
- 存储到PostgreSQL + pgvector

### RAG问答
- 基于向量相似度检索相关笔记
- 使用DeepSeek LLM生成回答
- 返回答案及参考来源

### 性能优化
- 异步处理笔记向量化
- 缓存机制减少重复调用
- 限流保护防止API滥用

## 开发计划

- [x] 项目搭建与配置
- [x] OCR服务实现
- [x] Embedding服务实现
- [x] 向量数据库集成
- [x] RAG问答服务
- [x] 缓存与限流配置
- [x] Docker部署支持
- [x] 全局异常处理
- [x] 跨域配置
- [x] 健康检查接口
- [x] API文档(Swagger)
- [x] 日志配置
- [ ] App端对接
- [ ] 生产环境部署

## 安全提示

### 保护API密钥

**永远不要将API密钥提交到代码仓库！**

1. 所有密钥通过环境变量配置
2. `.env` 文件已在 `.gitignore` 中排除
3. 使用 `.env.example` 作为模板，但不要填入真实密钥
4. 生产环境使用系统环境变量或密钥管理服务

### 检查泄露

如果意外提交了密钥：
```bash
# 从Git历史中移除敏感文件
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch .env' \
  --prune-empty --tag-name-filter cat -- --all

# 立即轮换泄露的API密钥
```

## 许可证

MIT License
