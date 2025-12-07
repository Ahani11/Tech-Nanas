package com.example.technanas.data.remote

data class LlmChatTurn(
    val role: String,   // "user" or "assistant"
    val content: String
)

data class LlmFaqRequest(
    val message: String,
    val history: List<LlmChatTurn> = emptyList()
)

data class LlmFaqResponse(
    val reply: String
)
