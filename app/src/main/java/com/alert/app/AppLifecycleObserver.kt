package com.alert.app

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

import com.alert.app.chatgpt.UserRepository;

class AppLifecycleObserver(
    private val userRepository:UserRepository,
    private val userId: String
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        // App foreground
        userRepository.setUserOnlineStatus(userId, true)
    }

    override fun onStop(owner: LifecycleOwner) {
        // App background or closed
        userRepository.setUserOnlineStatus(userId, false)
    }
}