package com.alert.app.viewmodel.selfalertviewmodel

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.model.selfAlert.CreateSelfAlertRequest
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class SelfAlertViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {



    suspend fun getSelfAlerts(type: String?): Flow<NetworkResult<JsonObject>> {
        return repository.getSelfAlerts(type).onEach {
        }
    }

    suspend fun addSelfAlert(createSelfAlertRequest: CreateSelfAlertRequest)
        : Flow<NetworkResult<JsonObject>> {
        return repository.addSelfAlert(createSelfAlertRequest).onEach {
        }
    }

    suspend fun deleteUserAlert(alertId: String,type: String)
            : Flow<NetworkResult<JsonObject>> {
        return repository.deleteUserAlert(alertId,type).onEach {
        }
    }


}