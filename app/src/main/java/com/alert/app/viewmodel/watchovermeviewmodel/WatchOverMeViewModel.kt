package com.alert.app.viewmodel.watchovermeviewmodel

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.model.watchoverme.AllLiveJourneyResponse
import com.alert.app.model.watchoverme.JourneyStarted
import com.alert.app.model.watchoverme.LiveLocationResponse
import com.alert.app.repository.MainRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import retrofit2.http.Field
import javax.inject.Inject

@HiltViewModel
class WatchOverMeViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()   {
    // Without Loading - using null as initial value
    private val _allLiveJourneys = MutableStateFlow<NetworkResult<AllLiveJourneyResponse>?>(null)
    val allLiveJourneys: StateFlow<NetworkResult<AllLiveJourneyResponse>?> = _allLiveJourneys.asStateFlow()

    private val _liveLocation = MutableStateFlow<NetworkResult<LiveLocationResponse>?>(null)
    val liveLocation: StateFlow<NetworkResult<LiveLocationResponse>?> = _liveLocation.asStateFlow()

    suspend fun startJourney(
        currentLatitude : String,
        currentLongitude : String,
         destinationLatitude : String,
         destinationLongitude : String,
    ): Flow<NetworkResult<JourneyStarted>> {
        return repository.startJourney(currentLatitude,currentLongitude,destinationLatitude,destinationLongitude).onEach {
        }
    }

    suspend fun liveLocation(
userId : String
    ): Flow<NetworkResult<LiveLocationResponse>> {
        return repository.liveLocation(userId).onEach {
                result ->
            _liveLocation.value = result
        }
    }

    suspend fun getAllLiveLocation(

    ): Flow<NetworkResult<AllLiveJourneyResponse>> {
        return repository.getAllLiveLocation().onEach {   result ->
            _allLiveJourneys.value = result

        }
    }


    suspend fun setAlertWrongPath(
        journeyId : String,
    ): Flow<NetworkResult<String>> {
        return repository.setAlertWrongPath(journeyId).onEach {   result ->
         //   _allLiveJourneys.value = result

        }
    }

}