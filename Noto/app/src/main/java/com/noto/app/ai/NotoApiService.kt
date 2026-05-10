package com.noto.app.ai

import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// API响应数据类
data class OcrResponse(
    val success: Boolean,
    val text: String?,
    val message: String?
)

data class NoteRequest(
    val noteId: String,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList()
)

data class NoteResponse(
    val success: Boolean,
    val message: String?
)

data class ChatRequest(
    val question: String
)

data class ChatResponse(
    val answer: String,
    val sources: List<Source>
) {
    data class Source(
        val noteId: String,
        val title: String,
        val similarity: Float
    )
}

// API接口定义
interface NotoApiService {

    @Multipart
    @POST("/api/ocr/recognize")
    suspend fun recognizeText(@Part image: MultipartBody.Part): OcrResponse

    @POST("/api/notes/save")
    suspend fun saveNote(@Body request: NoteRequest): NoteResponse

    @POST("/api/chat/ask")
    suspend fun askQuestion(@Body request: ChatRequest): ChatResponse
}

// API客户端单例
object ApiClient {

    // 真机用局域网IP，模拟器用10.0.2.2
    private const val BASE_URL = "http://192.168.43.229:8080"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: NotoApiService = retrofit.create(NotoApiService::class.java)
}
