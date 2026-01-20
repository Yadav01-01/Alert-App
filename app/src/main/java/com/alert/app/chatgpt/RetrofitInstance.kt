package com.alert.app.chatgpt

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://api.openai.com/"

    val api: OpenAIApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApi::class.java)
    }

    fun sendMessageToChatGPT(userMessage: String, apiKey: String, callback: (String) -> Unit) {
        val request = ChatRequest(
            messages = listOf(Message("user", userMessage))
        )

        val call = api.sendChatMessage(
            auth = "Bearer $apiKey",
            request = request
        )

        call.enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                val reply = response.body()?.choices?.firstOrNull()?.message?.content ?: "No response"
                callback(reply)
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                callback("Error: ${t.localizedMessage}")
            }
        })
    }

}