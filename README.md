# Noto AI — 智能笔记应用

> 基于开源笔记 App [Noto](https://github.com/alialbaali/Noto) + Java Spring Boot 后端，集成 OCR 识别、语义检索、AI 问答功能
>
> Built on the open-source [Noto](https://github.com/alialbaali/Noto) app + Java Spring Boot backend, with OCR, semantic search, and AI-powered Q&A

---

## Architecture / 架构

```
┌─────────────────────┐          ┌──────────────────────────────────────┐
│    Noto App (Kotlin) │          │     Spring Boot Backend (Java)       │
│                      │   HTTP   │                                      │
│  · Take photo        │ ──────► │  /api/ocr      → Baidu OCR API       │
│  · View notes        │         │  /api/notes    → Save & vectorize     │
│  · AI chat           │ ◄────── │  /api/chat     → RAG Q&A             │
│                      │         │                                      │
└─────────────────────┘          │         ┌──────────────────┐         │
                                 │         │    Supabase       │         │
                                 │         │  · PostgreSQL     │         │
                                 │         │  · pgvector       │         │
                                 │         └──────────────────┘         │
                                 └──────────────────────────────────────┘
```

---

## Features / 功能特性

| Feature | Description | 说明 |
|---------|-------------|------|
| **OCR Recognition** | Upload image → extract text via Baidu OCR | 拍照/选图，自动识别文字 |
| **Smart Notes** | Save notes with auto-vectorization | 保存笔记，自动生成向量索引 |
| **Semantic Search** | Find relevant notes by meaning, not keywords | 按语义相似度检索，而非关键词匹配 |
| **AI Q&A** | Chat with your notes using DeepSeek LLM | 基于笔记内容的智能问答 |

**How it works / 工作原理：**

```
Photo ──→ OCR ──→ Save Note ──→ Vectorize ──→ Store in Supabase
                                                │
Question ──→ Embed ──→ Search similar notes ──→ LLM generates answer
拍照 ──→ OCR ──→ 保存笔记 ──→ 向量化 ──→ 存入 Supabase
                                        │
提问 ──→ 向量化 ──→ 检索相关笔记 ──→ LLM 生成回答
```

---

## Tech Stack / 技术栈

| Component | Technology |
|-----------|-----------|
| **Android App** | Kotlin, Jetpack Compose, Retrofit 2.9, OkHttp 4.12 |
| **Backend** | Java 17, Spring Boot 3.2.4, Spring Data JPA |
| **Database** | PostgreSQL (Supabase), pgvector 0.8 |
| **OCR** | Baidu OCR API |
| **Embedding** | Zhipu AI `embedding-3` (1024 dimensions) |
| **LLM** | DeepSeek Chat |
| **Deployment** | Docker, Docker Compose |

---

## Project Structure / 项目结构

```
New/
├── Noto/                          # Android App / 安卓应用
│   └── app/src/main/java/com/noto/app/
│       ├── ai/
│       │   ├── NotoApiService.kt       Retrofit API definitions / API 接口定义
│       │   └── ChatFragment.kt         AI chat UI / AI 对话界面
│       ├── note/
│       │   └── NoteFragment.kt         OCR button & upload / OCR 按钮与上传
│       └── res/
│           ├── layout/
│           │   ├── fragment_chat.xml   Chat layout / 聊天布局
│           │   └── item_chat_message.xml
│           └── xml/
│               └── network_security_config.xml  HTTP security config
│
├── noto-ai-backend/               # Spring Boot Backend / 后端服务
│   └── src/main/java/com/notoai/
│       ├── controller/            API endpoints / API 接口
│       ├── service/               Business logic / 业务逻辑
│       └── config/                Configuration / 配置
│
├── plan.md                        Development plan / 开发计划
└── work.log                       Agent work log / 工作日志
```

---

## Quick Start / 快速开始

### Prerequisites / 环境要求

| Requirement | Version |
|-------------|---------|
| JDK | 17+ |
| Maven | 3.6+ |
| Android Studio | Latest / 最新版 |
| PostgreSQL | Supabase free tier or local |

### 1. Backend Setup / 后端配置

```bash
cd noto-ai-backend

# Configure environment / 配置环境变量
cp .env.example .env
# Edit .env with your API keys / 编辑 .env 填入密钥

# Start backend / 启动后端
mvn spring-boot:run

# Verify / 验证
curl http://localhost:8080/health
# → {"status":"UP"}
```

### 2. Database Setup / 数据库配置

The Supabase database is pre-configured with:
Supabase 数据库已预配置：

- `note_embeddings` table with pgvector column
- `match_notes()` similarity search function
- `vector` extension enabled

### 3. App Setup / 应用配置

```
1. Open Noto/ in Android Studio
   用 Android Studio 打开 Noto/ 项目

2. Update BASE_URL in NotoApiService.kt:
   修改 NotoApiService.kt 中的 BASE_URL:
   - Emulator / 模拟器: "http://10.0.2.2:8080"
   - Real device / 真机: "http://<your-pc-ip>:8080"

3. Run on device / 部署到设备
```

---

## API Reference / API 接口

### Health Check / 健康检查

```
GET /health
→ {"service":"noto-ai-backend","status":"UP"}
```

### OCR / OCR 识别

```
POST /api/ocr/recognize
Content-Type: multipart/form-data

Body: image (file)
```

**Example / 示例:**
```bash
curl -X POST http://localhost:8080/api/ocr/recognize -F "image=@photo.jpg"
```

**Response / 响应:**
```json
{ "success": true, "text": "Extracted text...", "message": "识别成功" }
```

### Save Note / 保存笔记

```
POST /api/notes/save
Content-Type: application/json
```

**Example / 示例:**
```bash
curl -X POST http://localhost:8080/api/notes/save \
  -H "Content-Type: application/json" \
  -d '{"noteId":"1","title":"My Note","content":"Hello World","tags":["demo"]}'
```

**Response / 响应:**
```json
{ "success": true, "message": "笔记已保存，正在建立索引..." }
```

### AI Q&A / AI 问答

```
POST /api/chat/ask
Content-Type: application/json
```

**Example / 示例:**
```bash
curl -X POST http://localhost:8080/api/chat/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"What notes do I have?"}'
```

**Response / 响应:**
```json
{
  "answer": "Based on your notes, you have 1 note: 'My Note'...",
  "sources": [{ "noteId": "1", "title": "My Note", "similarity": 0.56 }]
}
```

### Swagger UI / API 文档

```
http://localhost:8080/swagger-ui/index.html
```

---

## Configuration / 配置

### Environment Variables / 环境变量

| Variable | Description / 说明 | Required |
|----------|-------------------|----------|
| `DB_HOST` | Database host / 数据库地址 | Yes |
| `DB_PORT` | Database port / 数据库端口 | Yes |
| `DB_NAME` | Database name / 数据库名 | Yes |
| `DB_USERNAME` | Database user / 数据库用户 | Yes |
| `DB_PASSWORD` | Database password / 数据库密码 | Yes |
| `OCR_API_KEY` | Baidu OCR Key / 百度 OCR Key | Yes |
| `OCR_SECRET_KEY` | Baidu OCR Secret / 百度 OCR Secret | Yes |
| `EMBEDDING_API_KEY` | Zhipu AI Key / 智谱 AI Key | Yes |
| `LLM_API_KEY` | DeepSeek Key | Yes |
| `LLM_MODEL` | LLM model / 模型名 | No (default: `deepseek-chat`) |

### Network Config / 网络配置

For real device testing, update `NotoApiService.kt` with your PC's LAN IP:
真机测试时，将 `NotoApiService.kt` 中的地址改为电脑局域网 IP：

```kotlin
private const val BASE_URL = "http://192.168.x.x:8080"  // Your PC IP / 你的电脑 IP
```

The `network_security_config.xml` allows HTTP traffic for development:
`network_security_config.xml` 允许开发环境下的 HTTP 明文通信：

```xml
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">192.168.x.x</domain>
    </domain-config>
</network-security-config>
```

---

## Docker Deployment / Docker 部署

```bash
# Build / 构建
cd noto-ai-backend
mvn clean package
docker build -t noto-ai-backend .

# Run / 启动
export DB_PASSWORD=your_password
export OCR_API_KEY=your_key
export OCR_SECRET_KEY=your_secret
export EMBEDDING_API_KEY=your_key
export LLM_API_KEY=your_key
docker-compose up -d
```

---

## Development Status / 开发进度

| Module / 模块 | Status / 状态 |
|---------------|---------------|
| Backend services / 后端服务 | ✅ Done / 完成 |
| Supabase database / 数据库 | ✅ Done / 完成 |
| App OCR feature / OCR 功能 | ✅ Done / 完成 |
| App chat feature / 对话功能 | ✅ Done / 完成 |
| Real device testing / 真机测试 | ✅ Done / 完成 |
| Unit tests / 单元测试 | ⏳ Pending / 待做 |

---

## Known Issues / 已知问题

- Emulator uses `10.0.2.2`, real device uses LAN IP — update `BASE_URL` accordingly
  模拟器用 `10.0.2.2`，真机用局域网 IP — 需手动切换
- Gradle wrapper uses Tencent mirror (fast in China, may slow elsewhere)
  Gradle 使用腾讯镜像（国内快，国外可能慢）

---

## License / 许可证

MIT License
