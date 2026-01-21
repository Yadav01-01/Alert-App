package com.alert.app.viewmodel.healthviewmodel

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class HealthAlertViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {


    suspend fun getSelfAlerts(type: String?): Flow<NetworkResult<JsonObject>> {
        return repository.getSelfAlerts(type).onEach {
        }
    }


    suspend fun addHealthAlert(
        alertFor: String,
        alertDuration: String,
        healthAlert: String,
        startDate: String,
        endDate: String,
        time: String,
        note: String,
        contact: List<String>?
    ): Flow<NetworkResult<JsonObject>> {
        return repository.addHealthAlerts(
            alertFor,
            alertDuration,
            healthAlert,
            startDate,
            endDate,
            time,
            note,
            contact
        )
    }









}