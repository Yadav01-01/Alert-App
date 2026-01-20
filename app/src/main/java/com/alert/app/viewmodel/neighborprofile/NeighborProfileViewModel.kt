package com.alert.app.viewmodel.neighborprofile

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class NeighborProfileViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {


    suspend fun neighborProfileDetails(contactId: String): Flow<NetworkResult<JsonObject>> {
        return repository.neighborProfileDetails(contactId).onEach {
        }
    }
    suspend fun neighborProfileBlock(contactId: String): Flow<NetworkResult<JsonObject>> {
        return repository.neighborProfileBlock(contactId).onEach {
        }
    }
    suspend fun neighborProfileDelete(contactId: String): Flow<NetworkResult<JsonObject>> {
        return repository.neighborProfileDelete(contactId).onEach {
        }
    }


}
