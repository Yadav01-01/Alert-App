package com.alert.app.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alert.app.adapter.ChatAdapter
import com.alert.app.databinding.ActivityChatBinding
import com.alert.app.viewmodel.chatbot.ChatViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alert.app.base.AppConstant
import com.alert.app.base.SessionManagement
import com.alert.app.di.NetworkResult
import com.alert.app.model.Message
import com.alert.app.viewmodel.ChatScreenViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private var popup: EmojiPopup? = null
    private lateinit var viewModel : ChatViewModel
    private val firestore = FirebaseFirestore.getInstance()
    private var chatId = "123"
    private lateinit var currentUserId : String
    private var messageList : List<Message> = mutableListOf()
    private lateinit var  chatViewModel : ChatScreenViewModel
    var contactUserId :String =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        viewModel =     ViewModelProvider(this)[ChatViewModel::class.java]
        chatViewModel = ViewModelProvider(this)[ChatScreenViewModel::class.java]
        contactUserId = intent.getStringExtra("contactUserId")?:"-1"
        settingProfileData()
        makingChatId(contactUserId)
        currentUserId = SessionManagement(this).getUserId().toString()

        setupToolbar()

        setupEmoji()

        setupRecyclerView()

        setupClicks()

        viewModel.loadMessages(chatId,currentUserId)

        viewModel.messages.observe(this) { messages ->
            messageList = messages
            Log.d("Testing_message",messages.size.toString())
              adapter.submitList(messages.toMutableList())
           }

    }

    private fun settingProfileData(){
        if (intent?.hasExtra(AppConstant.NAME) == true) {
            val userName = intent.getStringExtra(AppConstant.NAME)
            binding.userName.setText(userName)
        }

        if(intent?.hasExtra(AppConstant.PROFILE) == true){
            val userProfileImage = intent.getStringExtra(AppConstant.PROFILE)
            Glide.with(this).load(userProfileImage).into( binding.userImg)
        }

    }

    private fun makingChatId(contactUserId: String){
        val userId = SessionManagement(this).getUserId()
        val otherUserId = contactUserId.toInt()
        if (userId != null) {
            if(userId < otherUserId){
                chatId = ""+userId+"_"+otherUserId
            }else{
              chatId = ""+otherUserId+"_"+userId
            }
        }

        Log.d("TESTING_CHAT_ID",chatId)
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
        adapter = ChatAdapter(currentUserId,mutableListOf())

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
                if(messageList.size ==0){
                    callingCreateChannelApi(text)
                }else {
                    sendMessage(text)
                    binding.edMsg.text.clear()
                }
            }
        }
    }

    private fun callingCreateChannelApi(text:String){
             lifecycleScope.launch {
                 chatViewModel.createChannel(contactUserId,chatId).collect {
                     when(it){
                         is NetworkResult.Success ->{
                             sendMessage(text)
                             binding.edMsg.text.clear()
                         }
                         is NetworkResult.Error ->{
                             Toast.makeText(this@ChatActivity,"Something Went Wrong Try Again",Toast.LENGTH_LONG).show()
                         }
                     }
                 }
             }
    }

    private fun sendMessage(messageText: String) {

        viewModel.sendMessage(chatId,messageText,currentUserId,contactUserId)

    }


}
