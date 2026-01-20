package com.alert.app.viewmodel.contactsviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alert.app.di.NetworkResult
import com.alert.app.model.contact.UserContactRequest
import com.alert.app.model.contact.UserEditContactRequest
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ContactsViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun getContactList(): Flow<NetworkResult<JsonObject>> {
        return repository.getContactList().onEach {
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

    suspend fun manualContact(userContactRequest: UserContactRequest): Flow<NetworkResult<JsonObject>> {
        return repository.manualContact(userContactRequest).onEach {
        }
    }

    suspend fun editManualContact(userEditContactRequest: UserEditContactRequest): Flow<NetworkResult<JsonObject>> {
        return repository.editManualContact(userEditContactRequest).onEach {
        }
    }

    suspend fun deleteContact(contactId: String): Flow<NetworkResult<JsonObject>> {
        return repository.deleteContact(contactId).onEach {
        }
    }

    suspend fun getAlert(contactId: String): Flow<NetworkResult<JsonObject>> {
        return repository.getAlert(contactId).onEach {
        }
    }

    suspend fun shareLocationApi( contact_id: String, duration: String, lat: String, long: String): Flow<NetworkResult<JsonObject>> {
        return repository.shareLocation(contact_id, duration, lat, long).onEach {
        }
    }





}