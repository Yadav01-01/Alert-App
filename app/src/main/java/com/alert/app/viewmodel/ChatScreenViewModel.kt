package com.alert.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.model.message.ChatListItem
import com.alert.app.model.ChatUserModel
import com.alert.app.model.Message
import com.alert.app.repository.ChatRepository
import com.alert.app.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import retrofit2.http.Field
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.alert.app.model.MessageType
import kotlinx.coroutines.launch
import java.util.Date

@HiltViewModel
class ChatScreenViewModel  @Inject constructor(
    private val repository1: MainRepository,
    private val repository: ChatRepository
    ) : ViewModel(){

  var currentUserId:String =""


    private var chatJob: Job? = null

    private val _chatList = MutableLiveData<List<ChatListItem>>()
    val chatList: LiveData<List<ChatListItem>> = _chatList

    fun loadChatList(usersFromApi: List<ChatUserModel>, myUserId: String) {
        chatJob?.cancel() // Purana collection band karo
        chatJob = viewModelScope.launch {
            repository.observeChatList(usersFromApi, myUserId).collect { updatedList ->
              updatedList?.let {
                  _chatList.value = updatedList
              }
            }
        }
    }

    suspend fun getChatList(): Flow<NetworkResult<MutableList<ChatUserModel>>> {
        return repository1.getChannelList().onEach {

        }
    }


    suspend fun createChannel(contactUserId: String, chatId: String): Flow<NetworkResult<String>> {
        return repository1.createChannel(contactUserId, chatId)
       }


    }