package com.example.technanas.ui.faq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.technanas.TechNanasApp
import com.example.technanas.data.model.ChatMessage
import com.example.technanas.databinding.FragmentFaqChatBinding
import kotlinx.coroutines.launch

class FAQChatFragment : Fragment() {

    private var _binding: FragmentFaqChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter

    private val app by lazy { requireActivity().application as TechNanasApp }

    // Keep chat history in memory (for now)
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaqChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatAdapter = ChatAdapter()

        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }

        // Initial greeting from the AI assistant
        if (messages.isEmpty()) {
            addMessageToChat(
                "Hello! I am the TechNanas & LPNM assistant. Ask me about the app, pineapple farming, or LPNM support. 🍍",
                isUser = false
            )
        }

        binding.btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        // 1) Add user message
        addMessageToChat(text, isUser = true)
        binding.etMessage.text?.clear()

        // 2) Add temporary "Thinking..." bot message
        val thinkingMessage = ChatMessage("Thinking...", isUser = false)
        messages.add(thinkingMessage)
        chatAdapter.addMessage(thinkingMessage)
        scrollToBottom()
        val thinkingIndex = messages.lastIndex

        // 3) Call backend in coroutine
        viewLifecycleOwner.lifecycleScope.launch {
            val reply = app.llmFaqChatRepository.ask(
                message = text,
                history = messages.dropLast(1) // don't include the "Thinking..." placeholder
            )

            // Replace "Thinking..." with real reply
            messages[thinkingIndex] = ChatMessage(reply, isUser = false)
            // Rebind the whole list (simple approach)
            chatAdapter.clearMessages()
            messages.forEach { chatAdapter.addMessage(it) }
            scrollToBottom()
        }
    }

    private fun addMessageToChat(text: String, isUser: Boolean) {
        val msg = ChatMessage(text = text, isUser = isUser)
        messages.add(msg)
        chatAdapter.addMessage(msg)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        binding.recyclerViewChat.post {
            if (chatAdapter.itemCount > 0) {
                binding.recyclerViewChat.scrollToPosition(chatAdapter.itemCount - 1)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
