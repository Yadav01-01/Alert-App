package com.alert.app.viewmodel.locationmain

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _locationData = MutableLiveData<Pair<Double, Double>>() // lat, lng
    val locationData: LiveData<Pair<Double, Double>> = _locationData

    fun setLocation(lat: Double, lng: Double) {
        _locationData.value = Pair(lat, lng)
    }
}
