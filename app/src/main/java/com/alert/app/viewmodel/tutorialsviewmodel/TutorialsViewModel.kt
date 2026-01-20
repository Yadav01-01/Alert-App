package com.alert.app.viewmodel.tutorialsviewmodel

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class TutorialsViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun tutorialsDataRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.tutorialsDataRequestApi { successCallback(it) }
    }

    suspend fun termsAndConditionRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.termsAndConditionRequestApi { successCallback(it) }
    }

    suspend fun privacyPolicy(): Flow<NetworkResult<JsonObject>> {
        return repository.privacyPolicy().onEach {
        }
    }
    suspend fun aboutUs(): Flow<NetworkResult<JsonObject>> {
        return repository.aboutUs().onEach {
        }
    }

    suspend fun getFaq(): Flow<NetworkResult<JsonObject>> {
        return repository.getFaq().onEach {
        }
    }
    suspend fun userLogout(): Flow<NetworkResult<JsonObject>> {
        return repository.userLogout().onEach {
        }
    }
    suspend fun deleteUser(): Flow<NetworkResult<JsonObject>> {
        return repository.deleteUser().onEach {
        }
    }


}