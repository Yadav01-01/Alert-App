package com.alert.app.viewmodel.chatbot

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alert.app.di.NetworkResult
import com.alert.app.model.Message
import com.alert.app.repository.ChatRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor( private val repository: ChatRepository) : ViewModel() {


    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    fun loadMessages(chatId: String, myUserId: String) {
        viewModelScope.launch {
            repository.observeMessages(chatId).collect {
                _messages.postValue(it)
                repository.markChatRead(chatId, myUserId)
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