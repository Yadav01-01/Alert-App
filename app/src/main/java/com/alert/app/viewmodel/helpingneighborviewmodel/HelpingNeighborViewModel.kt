package com.alert.app.viewmodel.helpingneighborviewmodel

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
class HelpingNeighborViewModel@Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun getNeighbor(): Flow<NetworkResult<JsonObject>> {
        return repository.getNeighbor().onEach {
        }
    }

    suspend fun addNeighbor(createHelpingNeighbor: CreateHelpingNeighbor): Flow<NetworkResult<JsonObject>> {
        return repository.addNeighbor(createHelpingNeighbor).onEach {
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

    suspend fun getUserAddress(): Flow<NetworkResult<JsonObject>> {
        return repository.getUserAddress().onEach {
        }
    }

    suspend fun addUserAddress(type: String?, address: String?, latitude: String?, longitude: String?): Flow<NetworkResult<JsonObject>> {
        return repository.addUserAddress(type, address, latitude, longitude).onEach {
        }
    }

    suspend fun deleteAddress(addressId: String?): Flow<NetworkResult<JsonObject>> {
        return repository.deleteAddress(addressId).onEach {
        }
    }



}