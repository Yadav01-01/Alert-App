package com.alert.app.viewmodel.homeviewmodel

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun sendEmergencyMessageRequest(): Flow<NetworkResult<JsonObject>> {
        return repository.sendEmergencyMessageRequest().onEach {
        }
    }

}