package com.alert.app.viewmodel.mainactivitymodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alert.app.di.NetworkResult
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel@Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun getEmergencyMessage(): Flow<NetworkResult<JsonObject>> {
        return repository.getEmergencyMessage().onEach {
        }
    }

    private val _chatBotResponse = MutableStateFlow<NetworkResult<JsonObject>>(NetworkResult.Success(JsonObject()))
    val chatBotResponse: StateFlow<NetworkResult<JsonObject>> = _chatBotResponse

    fun fetchChatBotResponse(query: String) {
        viewModelScope.launch {
            repository.getChatBot(query).collect { result ->
                _chatBotResponse.value = result
            }
        }
    }

    suspend fun sendEmergencyMessageRequest(): Flow<NetworkResult<JsonObject>> {
        return repository.sendEmergencyMessageRequest().onEach {
        }
    }

    private val _checkInResponse = MutableLiveData<CheckInResponse?>()
    val checkInResponse: LiveData<CheckInResponse?> get() = _checkInResponse

    fun setResponse(response: CheckInResponse) {
        _checkInResponse.value = response
    }

    enum class CheckInResponse {
        YES, NO
    }
}