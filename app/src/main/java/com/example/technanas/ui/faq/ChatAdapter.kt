package com.example.technanas.ui.faq

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.technanas.data.model.ChatMessage
import com.example.technanas.databinding.ItemChatMessageBinding

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val items = mutableListOf<ChatMessage>()

    /**
     * Append a new message at the bottom and notify RecyclerView.
     */
    fun addMessage(message: ChatMessage) {
        items.add(message)
        notifyItemInserted(items.size - 1)
    }

    /**
     * Optional helper if you ever want to reset the conversation.
     */
    fun clearMessages() {
        items.clear()
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.text

            // Align user messages to the right, bot messages to the left.
            val params = binding.tvMessage.layoutParams
            if (params is LinearLayout.LayoutParams) {
                params.gravity = if (message.isUser) {
                    Gravity.END
                } else {
                    Gravity.START
                }
                binding.tvMessage.layoutParams = params
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemChatMessageBinding.inflate(inflater, parent, false)
        return ChatViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
