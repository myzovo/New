package com.noto.app.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noto.app.R
import com.noto.app.databinding.FragmentChatBinding
import com.noto.app.databinding.ItemChatMessageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 聊天消息数据类
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val sources: List<ChatResponse.Source> = emptyList()
)

// 聊天适配器
class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        with(holder.binding) {
            if (message.isUser) {
                cardUserMessage.visibility = View.VISIBLE
                cardAiMessage.visibility = View.GONE
                tvUserMessage.text = message.text
            } else {
                cardUserMessage.visibility = View.GONE
                cardAiMessage.visibility = View.VISIBLE
                tvAiMessage.text = message.text

                if (message.sources.isNotEmpty()) {
                    tvSources.visibility = View.VISIBLE
                    val sourcesText = message.sources.joinToString("\n") { "📎 ${it.title}" }
                    tvSources.text = "参考来源:\n$sourcesText"
                } else {
                    tvSources.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount() = messages.size
}

// 聊天Fragment
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSendButton()
    }

    private fun setupToolbar() {
        binding.tbChat.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChat.adapter = adapter
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val question = binding.etQuestion.text.toString().trim()
            if (question.isNotEmpty()) {
                sendQuestion(question)
                binding.etQuestion.text?.clear()
            }
        }
    }

    private fun sendQuestion(question: String) {
        // 添加用户消息
        messages.add(ChatMessage(question, isUser = true))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.scrollToPosition(messages.size - 1)

        // 显示加载状态
        val loadingMessage = ChatMessage("思考中...", isUser = false)
        messages.add(loadingMessage)
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.scrollToPosition(messages.size - 1)

        lifecycleScope.launch {
            try {
                val request = ChatRequest(question = question)
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.askQuestion(request)
                }

                // 替换加载消息为实际回答
                messages.removeAt(messages.size - 1)
                messages.add(ChatMessage(
                    text = response.answer,
                    isUser = false,
                    sources = response.sources
                ))
                adapter.notifyItemChanged(messages.size - 1)
                binding.rvChat.scrollToPosition(messages.size - 1)
            } catch (e: Exception) {
                messages.removeAt(messages.size - 1)
                messages.add(ChatMessage("抱歉，出现错误: ${e.message}", isUser = false))
                adapter.notifyItemChanged(messages.size - 1)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
