package com.alert.app.viewmodel.chatbot

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alert.app.di.NetworkResult
import com.alert.app.location.LiveLocationService
import com.alert.app.model.Message
import com.alert.app.repository.ChatRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor( private val repository: ChatRepository) : ViewModel() {


    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    fun loadMessages(chatId: String, myUserId: String) {
        viewModelScope.launch {
            repository.observeMessages(chatId,myUserId).collect {
                _messages.postValue(it)
                repository.markChatRead(chatId, myUserId)
            }
        }
    }


    fun deleteChatForMe(chatId: String,currentUserId:String) {
        repository.deleteChatForMe(chatId = chatId,
            currentUserId){ success ->
            if (success) {
                Log.d("TESTING_MESSAGE","I AM HERE IN DELETE SUCCESS")
                _messages.value   = emptyList<Message>()
            } else {
                Log.d("TESTING_MESSAGE","I AM HERE IN DELETE fAILURE")

            }
        }
    }


    fun getTimeAgo(timestamp: Timestamp?): String {
        if (timestamp == null) return ""

        val now = System.currentTimeMillis()
        val time = timestamp.seconds * 1000
        val diff = now - time

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr${if (hours > 1) "s" else ""} ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
            else -> {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(Date(time))
            }
        }
    }

    fun sendMessage(
        chatId: String,
        text: String,
        senderId: String,
        receiverId: String
    ) {
        viewModelScope.launch {
            repository.sendTextMessage(
                chatId = chatId,
                text = text,
                senderId = senderId,
                receiverId = receiverId
            )
        }
    }

    fun startSharingLocation(
        context: Context,
        chatId: String,
        senderId: String,
        receiverId: String,
        duration: Int
    ) {

        viewModelScope.launch {
            // Step 1: create message in Firestore
            val messageId = repository.createLiveLocationMessage(
                chatId,
                senderId,
                receiverId,
                duration
            )

            val intent = Intent(context, LiveLocationService::class.java).apply {
                putExtra("chatId", chatId)
                putExtra("messageId", messageId)
                putExtra("duration", duration)
            }

            ContextCompat.startForegroundService(context, intent)
        }

    }





//    fun sendLiveLocation(
//        chatId: String,
//        senderId: String,
//        receiverId: String
//    ) {
//        viewModelScope.launch {
//            repository.sendLiveLocation(
//                chatId,
//                senderId,
//                receiverId
//            )
//        }
//    }


@RequiresApi(Build.VERSION_CODES.O)
fun formatTimeAmPm(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }



}