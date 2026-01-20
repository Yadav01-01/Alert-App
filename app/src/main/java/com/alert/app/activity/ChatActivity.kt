package com.alert.app.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alert.app.adapter.ChatAdapter
import com.alert.app.databinding.ActivityChatBinding
import com.alert.app.model.chatbot.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider


class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private var popup: EmojiPopup? = null

    private val firestore = FirebaseFirestore.getInstance()

    private val senderId by lazy {
        FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    }

    private val receiverId by lazy {
        intent.getStringExtra("receiverId").orEmpty()
    }

    private val chatId by lazy {
        listOf(senderId, receiverId).sorted().joinToString("_")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        setupToolbar()
        setupEmoji()
        setupRecyclerView()
        listenMessages()
        setupClicks()

        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .update("unread_$senderId", 0)

    }

    private fun setupToolbar() {
        binding.imgBack.setOnClickListener { finish() }

        binding.imgCall.setOnClickListener {
            val channelName = "call_${System.currentTimeMillis()}"
            startActivity(
                Intent(this, CallActivity::class.java)
                    .putExtra("channelName", channelName)
            )
        }
    }

    private fun setupEmoji() {
        EmojiManager.install(GoogleEmojiProvider())
        popup = EmojiPopup.Builder
            .fromRootView(binding.root)
            .build(binding.edMsg)

        binding.imgImogi.setOnClickListener {
            popup?.toggle()
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(senderId)

        binding.rvMessages.layoutManager =
            LinearLayoutManager(this).apply {
                stackFromEnd = true
            }

        binding.rvMessages.adapter = adapter
    }

    private fun setupClicks() {
        binding.btnSend.setOnClickListener {
            val text = binding.edMsg.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                binding.edMsg.text.clear()
            }
        }
    }

    private fun sendMessage(messageText: String) {

        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val chatRef = FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)

        val messageData = hashMapOf(
            "senderId" to currentUserId,
            "message" to messageText,
            "timestamp" to System.currentTimeMillis()
        )

        chatRef.get().addOnSuccessListener { doc ->

            if (!doc.exists()) {
                // FIRST MESSAGE → CREATE CHAT
                val chatData = hashMapOf(
                    "participants" to listOf(currentUserId, receiverId),
                    "lastMessage" to messageText,
                    "lastMessageTime" to System.currentTimeMillis(),
                    "unread_$receiverId" to 1,
                    "unread_$currentUserId" to 0
                )

                chatRef.set(chatData)
            } else {
                //  CHAT EXISTS → UPDATE
                chatRef.update(
                    "lastMessage", messageText,
                    "lastMessageTime", System.currentTimeMillis(),
                    "unread_$receiverId", FieldValue.increment(1)
                )
            }

            //  SEND MESSAGE
            chatRef.collection("messages")
                .add(messageData)
        }
    }

    private fun listenMessages() {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) return@addSnapshotListener

                val messages = snapshot.toObjects(ChatMessage::class.java)
                adapter.submitList(messages)

                if (messages.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }
            }
    }

}
