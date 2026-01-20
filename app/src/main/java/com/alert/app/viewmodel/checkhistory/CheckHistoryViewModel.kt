package com.alert.app.viewmodel.checkhistory

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CheckHistoryViewModel @Inject constructor(private val repository: MainRepository) :
    ViewModel() {

    suspend fun checkInUserAlert(type: String?): Flow<NetworkResult<JsonObject>> {
        return repository.checkInUserAlert(type).onEach {
        }
    }

    suspend fun responseAlert(alertId: String?,description:String): Flow<NetworkResult<JsonObject>> {
        return repository.responseAlert(alertId, description).onEach {
        }
    }


}
