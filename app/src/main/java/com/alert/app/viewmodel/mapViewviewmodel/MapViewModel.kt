package com.alert.app.viewmodel.mapViewviewmodel

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.model.contact.UserContactRequest
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class MapViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {



    suspend fun getNearbyUser(latitude: String,longitude: String): Flow<NetworkResult<JsonObject>> {
        return repository.getNearbyUser(latitude, longitude).onEach {
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
    suspend fun addContact(userContactRequest: UserContactRequest): Flow<NetworkResult<JsonObject>> {
        return repository.addContact(userContactRequest).onEach {
        }
    }


}