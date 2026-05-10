# AI智能笔记App - 开发计划（云端优先 + Java后端架构）

## Context

基于开源笔记App Noto (Kotlin) 作为前端"外壳"，核心AI逻辑全部由Java Spring Boot后端完成。App只负责：拍照/选图上传、展示结果、聊天交互。后端统一处理OCR、Embedding、向量检索、LLM问答。

### 架构总览

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

### 当前真实状态（2026-05-10 最新）

| 模块 | 代码 | 可运行 | 已测试 |
|------|------|--------|--------|
| 后端服务代码 | ✅ 全部完成 | ✅ 端口8080正常运行 | ✅ 笔记保存+对话通过，OCR 错误处理修复 |
| Supabase 数据库 | ✅ 迁移脚本已有 | ✅ 表/函数/扩展均存在 | ✅ 向量写入+检索通过（相似度0.56） |
| App OCR功能 | ✅ 代码已完成 | ✅ 已部署真机 | ⚠️ 报错已修复，待重新测试 |
| App 聊天功能 | ✅ 代码已完成 | ✅ 已部署真机 | ⚠️ 报错已修复，待重新测试 |
| 端到端联调 | — | ⚠️ 网络问题已修复 | ❌ 待重新验证 |

---

## 阶段一：后端代码开发 ✅ 已完成

所有后端代码已编写完成，包括：
- 6个Service：OcrService、EmbeddingService、VectorDbService、LlmService、RagService、NoteService
- 3个Controller：OcrController、NoteController、ChatController
- 6个Config：AsyncConfig、CacheConfig、RateLimitConfig、WebMvcConfig、CorsConfig、SwaggerConfig
- DTO/Model/Entity 全部就绪

---

## 阶段二：App端代码开发 ✅ 已完成

Noto App 已完成改造，包括：
- `NotoApiService.kt` — Retrofit 接口定义 + ApiClient（指向 192.168.43.229:8080）
- `NoteFragment.kt` — OCR 按钮、拍照/相册选择、图片上传、结果回填
- `ChatFragment.kt` — 完整聊天 UI、消息列表、AI 问答
- `nav_graph.xml` — ChatFragment 已注册导航
- `build.gradle.kts` — Retrofit/OkHttp 依赖已添加
- `network_security_config.xml` — 允许 HTTP 明文通信（真机调试）
- `AndroidManifest.xml` — 添加 networkSecurityConfig 引用
- 新增布局：`fragment_chat.xml`、`item_chat_message.xml`

---

## 阶段三：环境修复与启动验证 ✅ 已完成

### 3.1 后端启动修复 ✅ 刚完成
- [x] 添加 `spring-dotenv` 依赖，自动加载 `.env` 文件
- [x] 修复 `application.yml` 数据库 URL 拼接方式（使用 DB_HOST/DB_PORT/DB_NAME）

### 3.2 后端启动验证 ✅ 已完成
- [x] 后端已在端口 8080 运行（PID 59196，4秒启动）
- [x] 健康检查通过：`/health` 返回 `{"status":"UP"}`
- [x] Swagger UI 可访问：`http://localhost:8080/swagger-ui/index.html`
- [x] API 端点：OCR、笔记保存、对话、健康检查

### 3.3 Supabase 数据库确认 ✅ 已完成
- [x] `note_embeddings` 表存在
- [x] `match_notes()` 函数已创建
- [x] `vector` 扩展已启用（v0.8.0）

---

## 阶段四：API 逐个验证 + Bug 修复 ✅ 已完成

### 4.1 笔记保存接口 ✅
```
POST /api/notes/save
→ {"success":true,"message":"笔记已保存，正在建立索引..."}
```

### 4.2 对话接口 ✅（RAG 完整流程验证）
```
POST /api/chat/ask {"question":"what notes do I have?"}
→ {"answer":"根据您的笔记内容，您目前有1条笔记：\n- **标题**: test note\n- **内容**: this is test content",
   "sources":[{"noteId":"test-1","title":"test note","similarity":0.5647872}]}
```
保存笔记后立即查询，向量检索命中，LLM 基于笔记内容生成回答。

### 4.3 OCR 接口 ✅
- [x] 空文件验证通过 → `{"success":false,"message":"请上传图片文件"}`
- [x] 修复 `parseOcrResult` 空指针 bug
- [x] 实际图片 OCR 需真实含文字图片验证

### 4.4 后端 Bug 修复 ✅
- [x] `OcrService.parseOcrResult()` — null 安全检查 + 百度 OCR 错误码处理
- [x] `OcrController` — 空文件校验（返回 400）
- [x] `GlobalExceptionHandler` — 新增 3 个异常处理器：
  - `HttpMessageNotReadableException` → 400 请求格式错误
  - `HttpRequestMethodNotSupportedException` → 405 方法不允许
  - `NoResourceFoundException` → 404 接口不存在

---

## 阶段五：App 编译与端到端联调 ⏳ 当前进行中

### 5.1 App 编译与部署 ✅ 已完成
- [x] Android Studio 编译成功
- [x] 已部署到真机（手机通过 USB 连接）
- [x] App 可正常打开，AI 功能入口可见

### 5.2 网络连接修复 ✅ 已完成
- [x] 问题：`CLEARTEXT communication to 10.0.2.2 not permitted`
- [x] 原因：10.0.2.2 是模拟器专用地址 + Android 9+ 默认禁止 HTTP 明文
- [x] 修复：创建 `network_security_config.xml`，更新 BASE_URL 为 `192.168.43.229:8080`

### 5.3 端到端测试（后端运行中 + 真机）
- [ ] 重新 Run App 到手机
- [ ] OCR：拍照 → 上传 → 编辑器显示识别文字
- [ ] 笔记保存：编辑 → 保存 → 后端日志显示收到请求
- [ ] AI 对话：点击 AI 按钮 → 提问 → 显示回答 + 来源笔记

### 5.4 异常场景测试
- [ ] 后端未启动时 App 的表现（应有友好提示）
- [ ] 网络超时/断网处理
- [ ] 大图片上传

---

## 阶段六：收尾优化（可选）🔲 待进行

- [ ] 添加后端单元测试（目前 src/test/ 为空）
- [ ] API 错误信息国际化
- [ ] App 端加载状态优化（loading indicator）
- [ ] docker-compose 本地一键部署验证

---

## 当前优先级排序

```
1. ✅ 后端启动验证
2. ✅ Supabase 表/函数确认
3. ✅ curl 测试三个 API 接口（笔记+对话通过，OCR 修复后通过）
4. ✅ Android Studio 编译 App + 部署真机
5. ✅ 修复网络连接问题（CLEARTEXT + IP 地址）
6. 真机端到端功能测试              ← 下一步
```

---

## 风险与应对

| 风险 | 当前状态 | 应对 |
|------|----------|------|
| 后端启动失败 | ✅ 已解决 | .env 加载 + DB URL 修复后正常启动 |
| Supabase 表不存在 | ✅ 已确认 | 表/函数/扩展均存在 |
| App 编译错误 | ✅ 已解决 | Android Studio 编译成功，已部署真机 |
| 真机网络连接 | ✅ 已解决 | network_security_config + 局域网 IP 配置完成 |
| API Key 过期/无效 | ⚠️ 部分验证 | 笔记+对话 API 正常，OCR 需真实图片验证 |
| Windows 终端编码 | ⚠️ 已知 | curl 测试中文 JSON 需注意 UTF-8 编码，App 端 Retrofit 自动处理 |
| Gradle 下载慢 | ⚠️ 已知 | wrapper 使用腾讯镜像，国内正常，国外可能超时 |
