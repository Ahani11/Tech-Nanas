package com.example.technanas.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface FaqChatApi {

    @POST("api/faq-chat")
    suspend fun askFaq(
        @Body request: LlmFaqRequest
    ): LlmFaqResponse
}
