package com.alert.app.chatgpt


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApi {
    @POST("v1/chat/completions")
    fun sendChatMessage(
        @Header("Authorization") auth: String,
        @Body request: ChatRequest
    ): Call<ChatResponse>
}