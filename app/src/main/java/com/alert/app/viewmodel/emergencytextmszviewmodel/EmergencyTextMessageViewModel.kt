package com.alert.app.viewmodel.emergencytextmszviewmodel

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class EmergencyTextMessageViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    suspend fun addEmergencyMessage(message: String): Flow<NetworkResult<JsonObject>> {
        return repository.addEmergencyMessage(message).onEach {
        }
    }

    suspend fun getEmergencyMessage(): Flow<NetworkResult<JsonObject>> {
        return repository.getEmergencyMessage().onEach {
        }
    }


}