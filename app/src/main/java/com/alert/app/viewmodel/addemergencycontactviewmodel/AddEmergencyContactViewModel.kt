package com.alert.app.viewmodel.addemergencycontactviewmodel

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.model.helpingneighbormodel.CreateHelpingNeighbor
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AddEmergencyContactViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    suspend fun addEmergencyContact(createHelpingNeighbor: CreateHelpingNeighbor): Flow<NetworkResult<JsonObject>> {
        return repository.addEmergencyContact(createHelpingNeighbor).onEach {
        }
    }

    suspend fun getEmergencyContact(): Flow<NetworkResult<JsonObject>> {
        return repository.getEmergencyContact().onEach {
        }
    }

    suspend fun getRelation(): Flow<NetworkResult<JsonObject>> {
        return repository.getRelation().onEach {
        }
    }

    suspend fun getAllAlerts(): Flow<NetworkResult<JsonObject>> {
        return repository.getAllAlerts().onEach {
        }
    }

}