package com.alert.app.viewmodel

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alert.app.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InCallViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {
    private val _time = MutableLiveData<String>()
    val time: LiveData<String> = _time

    private var startTime = 0L
    private var handler: Handler? = null

    private val runnable = object : Runnable {
        override fun run() {
            val elapsedMillis = SystemClock.elapsedRealtime() - startTime
            _time.value = formatTime(elapsedMillis)
            handler?.postDelayed(this, 1000)
        }
    }

    fun startTimer() {
        startTime = SystemClock.elapsedRealtime()
        handler = Handler(Looper.getMainLooper())
        handler?.post(runnable)
    }

    fun stopTimer() {
        handler?.removeCallbacks(runnable)
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}