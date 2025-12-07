package com.example.technanas.data.repository

import com.example.technanas.data.model.ChatMessage
import com.example.technanas.data.remote.FaqChatApi
import com.example.technanas.data.remote.LlmChatTurn
import com.example.technanas.data.remote.LlmFaqRequest

/**
 * Repository that talks to our Flask + GPT backend.
 */
class LlmFaqChatRepository(
    private val api: FaqChatApi
) {

    /**
     * @param message  latest user message
     * @param history  chat history (oldest first, newest last)
     */
    suspend fun ask(message: String, history: List<ChatMessage>): String {
        if (message.isBlank()) {
            return "Please type your question first."
        }

        // Convert last few UI messages to API history format
        val turns = history
            .takeLast(6) // last few turns only, to keep tokens low
            .map { msg ->
                LlmChatTurn(
                    role = if (msg.isUser) "user" else "assistant",
                    content = msg.text
                )
            }

        return try {
            val response = api.askFaq(
                LlmFaqRequest(
                    message = message.trim(),
                    history = turns
                )
            )
            val reply = response.reply.trim()
            if (reply.isEmpty()) {
                "Sorry, I did not get any answer. Please try again."
            } else {
                reply
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Sorry, I couldn't contact the help server..."
        }
    }
}
