package com.alert.app.viewmodel.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alert.app.di.NetworkResult
import com.alert.app.model.notification.AlertModel
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatBotViewModel@Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val _chatBotResponse = MutableStateFlow<NetworkResult<JsonObject>>(NetworkResult.Success(JsonObject()))
    val chatBotResponse: StateFlow<NetworkResult<JsonObject>> = _chatBotResponse

    fun fetchChatBotResponse(query: String) {
        viewModelScope.launch {
            repository.getChatBot(query).collect { result ->
                _chatBotResponse.value = result
            }
        }
    }
}