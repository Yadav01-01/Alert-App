package com.alert.app.viewmodel.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alert.app.di.NetworkResult
import com.alert.app.model.notification.AlertModel
import com.alert.app.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    suspend fun loadNotifications(type: String): Flow<NetworkResult<List<AlertModel>>> {
        return repository.getNotifications(type)
    }
}
