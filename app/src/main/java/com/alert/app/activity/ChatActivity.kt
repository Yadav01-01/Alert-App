package com.alert.app.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.alert.app.R
import com.alert.app.base.AppConstant
import com.alert.app.base.SessionManagement
import com.alert.app.chatgpt.UserRepository
import com.alert.app.di.NetworkResult
import com.alert.app.model.Message
import com.alert.app.viewmodel.ChatScreenViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private var popup: EmojiPopup? = null
    private var statusJob: Job? = null
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

        if(intent.hasExtra("contactUserId")){
           contactUserId = intent.getIntExtra("contactUserId",-1).toString()
        }
        Log.d("TESTING_CHAT_ID","Inside chat activity "+contactUserId)

        if(intent.hasExtra(AppConstant.CHAT_ID)){
            chatId = intent.getStringExtra(AppConstant.CHAT_ID)?:"-1"
        }

        currentUserId = SessionManagement(this).getUserId().toString()

        setupToolbar()

        setupEmoji()
        setupRecyclerView()
        settingProfileData()

        if( !contactUserId.isNullOrEmpty() && contactUserId.equals("-1")==false) {
            makingChatId(contactUserId)
        }

        setupClicks()
        Log.d("FINAL_CHAT_ID", "Loading messages for chatId: $chatId")
        viewModel.loadMessages(chatId,currentUserId)
        viewModel.messages.observe(this) { messages ->
              messageList = messages
              Log.d("Testing_message",messages.size.toString())
              adapter.submitList(messages.toMutableList())
            if(adapter.itemCount>1) {
                binding.rvMessages.smoothScrollToPosition(adapter.itemCount - 1)
            }
        }

        workingForLiveLocationSharing()
<<<<<<< HEAD
       checkingOtherUserStatus()
       // observeUserStatus()
=======
        checkingOtherUserStatus()
>>>>>>> 936a5d1183412f3d73443959fd2f54b6905796f6
    }

    private fun workingForLiveLocationSharing(){
        if(intent.hasExtra(AppConstant.Duration)){
            val milliseconds = 15
            var duration = intent.getIntExtra(AppConstant.Duration,milliseconds)
            duration = duration* 60 * 1000
            if(messageList.size ==0){
                callingCreateChannelApiLocation(duration)
            }
        }
    }


    private fun checkingOtherUserStatus(){
        lifecycleScope.launch {
            val repository = UserRepository()
            val id = getOtherUserId(chatId,currentUserId)
            repository.observeUserOnlineStatus(id)
                .collect { (isOnline, lastSeen) ->
                    Log.d("TESTING_CURRENT_USER_STATUS","Status is"+ isOnline+" "+lastSeen)
                    binding.tvStatus.text =
                        if (isOnline) {
                            "Online"
                        }
                        else "Last seen ${viewModel.getTimeAgo(lastSeen)}"

                    if(isOnline){
                        binding.imgUpload.visibility =View.VISIBLE
                    }

                }
          }

    }






    private fun settingProfileData(){

        if (intent?.hasExtra(AppConstant.NAME) == true) {
            val userName = intent.getStringExtra(AppConstant.NAME)
            binding.userName.setText(userName)
        }

        if(intent?.hasExtra(AppConstant.PROFILE) == true){

            val userProfileImage = intent.getStringExtra(AppConstant.PROFILE)

            Log.d("TESTING_USER_PROFILE","Profile is "+userProfileImage.toString())

            adapter.receiverProfile(userProfileImage.toString())

            Glide.with(this)
                .load(userProfileImage)
                .placeholder(R.drawable.user_img_icon) // shown while loading
                .error(R.drawable.user_img_icon)       // shown if load fails
                .into(binding.userImg)

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

    fun getOtherUserId(chatId: String, currentUserId: String): String {
       Log.d("TESTING_CHAT_ID","CHAT ID IS"+chatId+" Current UserId is"+ currentUserId)
        val (id1, id2) = chatId.split("_")
        return if (id1 == currentUserId) id2 else id1
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

        popup = EmojiPopup.Builder.fromRootView(binding.root).build(binding.edMsg)

        binding.imgImogi.setOnClickListener {
            popup?.toggle()
        }

    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(currentUserId)

        binding.rvMessages.layoutManager = LinearLayoutManager(this).apply {
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

    private fun callingCreateChannelApiLocation(text:Int){
        lifecycleScope.launch {
            chatViewModel.createChannel(contactUserId,chatId).collect {
                when(it){
                    is NetworkResult.Success ->{
                        val currentUserId = SessionManagement(this@ChatActivity).getUserId()

                        viewModel.startSharingLocation(
                            context = this@ChatActivity,
                            chatId = chatId,
                            senderId = currentUserId.toString(),
                            receiverId = contactUserId,
                            duration = text
                        )

                    //  sendMessage(text)
                    //    binding.edMsg.text.clear()
                    }
                    is NetworkResult.Error ->{
                        Toast.makeText(this@ChatActivity,"Something Went Wrong Try Again",Toast.LENGTH_LONG).show()
                    }
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
    override fun onResume() {
        super.onResume()
        // Re-observe status when activity comes to foreground
        observeUserStatus()
    }
/*    private fun observeUserStatus() {
        // Use lifecycleScope.launch with repeatOnLifecycle properly
        lifecycleScope.launch {
            // This ensures the flow collection is tied to the lifecycle
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val repository = UserRepository()
                val otherUserId = getOtherUserId(chatId, currentUserId)

                repository.observeUserOnlineStatus(otherUserId)
                    .collect { (isOnline, lastSeen) ->
                        updateUserStatusUI(isOnline, lastSeen)
                    }
            }
        }
    }*/
private fun observeUserStatus() {
    val repository = UserRepository()
    val otherUserId = getOtherUserId(chatId, currentUserId)

    Log.d("USER_STATUS", "🔵 Starting to observe status for user: $otherUserId")

    // Cancel previous job if any
    statusJob?.cancel()

    statusJob = repository.observeUserOnlineStatus(otherUserId)
        .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
        .onEach { (isOnline, lastSeen) ->
            Log.d("USER_STATUS", "📨 Status received in Activity - isOnline: $isOnline, lastSeen: $lastSeen")
            updateUserStatusUI(isOnline, lastSeen)
        }
        .onStart {
            Log.d("USER_STATUS", "▶️ Flow started for user: $otherUserId")
        }
        .catch { e ->
            Log.e("USER_STATUS", "💥 Error in status flow", e)
        }
        .launchIn(lifecycleScope)
}

    // Add this property at class level


    private fun updateUserStatusUI(isOnline: Boolean, lastSeen: com.google.firebase.Timestamp?) {
        binding.tvStatus.text = when {
            isOnline -> {
                Log.d("USER_STATUS_UI", "User is ONLINE")
                binding.imgUpload.visibility = View.VISIBLE
                "Online"
            }
            lastSeen != null -> {
                val lastSeenText = getLastSeenText(lastSeen)
                Log.d("USER_STATUS_UI", "User is OFFLINE - last seen: $lastSeenText")
                binding.imgUpload.visibility = View.GONE
                "Last seen ${lastSeenText}"
            }
            else -> {
                Log.d("USER_STATUS_UI", "User status UNKNOWN - no lastSeen data")
                binding.imgUpload.visibility = View.GONE
                "Offline"
            }
        }
    }
    private fun getLastSeenText(timestamp: com.google.firebase.Timestamp): String {
        val now = System.currentTimeMillis()
        val lastSeenTime = timestamp.seconds * 1000
        val diffInMillis = now - lastSeenTime
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(now - lastSeenTime)
        Log.d("LAST_SEEN", "Current time: $now")
        Log.d("LAST_SEEN", "Last seen time: $lastSeenTime")
        Log.d("LAST_SEEN", "Difference in millis: $diffInMillis")
        val diffInHours = TimeUnit.MILLISECONDS.toHours(now - lastSeenTime)
        val diffInDays = TimeUnit.MILLISECONDS.toDays(now - lastSeenTime)
        Log.d("LAST_SEEN", "Minutes: $diffInMinutes, Hours: $diffInHours, Days: $diffInDays")
       // return when {
           /* diffInMinutes < 1 -> {
                "just now"
            }
            diffInMinutes < 60 -> "$diffInMinutes minutes ago"
            diffInHours < 24 -> {
                val hours = diffInHours
                "$hours hour${if (hours > 1) "s" else ""} ago"
            }
            diffInDays == 1L -> "yesterday at ${getFormattedTime(lastSeenTime)}"
            diffInDays < 7 -> "$diffInDays days ago"
            else -> {
                // Show full date for older messages
                val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                dateFormat.format(Date(lastSeenTime))
            }*/
            return when {
                diffInMinutes < 1 -> {
                    Log.d("LAST_SEEN", "Case: just now")
                    "just now"
                }
                diffInMinutes < 60 -> {
                    Log.d("LAST_SEEN", "Case: $diffInMinutes minutes ago")
                    "$diffInMinutes minutes ago"
                }
                diffInHours < 24 -> {
                    val hours = diffInHours
                    Log.d("LAST_SEEN", "Case: $hours hours ago")
                    "$hours hour${if (hours > 1) "s" else ""} ago"
                }
                diffInDays == 1L -> {
                    val time = getFormattedTime(lastSeenTime)
                    Log.d("LAST_SEEN", "Case: yesterday at $time")
                    "yesterday at $time"
                }
                diffInDays < 7 -> {
                    Log.d("LAST_SEEN", "Case: $diffInDays days ago")
                    "$diffInDays days ago"
                }
                else -> {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(lastSeenTime))
                    Log.d("LAST_SEEN", "Case: full date - $formattedDate")
                    formattedDate
                }
            }
    }

    private fun getFormattedTime(timestamp: Long): String {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(Date(timestamp))
    }

    private fun sendMessage(messageText: String) {

        viewModel.sendMessage(chatId,messageText,currentUserId,contactUserId)

    }


}
