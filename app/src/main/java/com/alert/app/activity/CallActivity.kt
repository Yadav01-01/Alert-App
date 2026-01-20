package com.alert.app.activity

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Chronometer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.alert.app.R
import com.alert.app.databinding.ActivityCallBinding
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine

class CallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCallBinding

    private lateinit var rtcEngine: RtcEngine
    private lateinit var chronometer: Chronometer
    private var isMuted = false
    private var isSpeakerOn = false

    private val appId = "YOUR_AGORA_APP_ID"
    private val token: String? = null
    private var channelName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        channelName = intent?.getStringExtra("channelName").toString()

        setupStatusBarAppearance()
        setupClickListeners()

        initializeAgoraEngine()
        joinChannel()
        chronometer.start()

        binding.imgMike.setOnClickListener {
            isMuted = !isMuted
            rtcEngine.muteLocalAudioStream(isMuted)
            ////for testing
            binding.imgMike.setImageResource(if (isMuted) R.drawable.mute_icon else R.drawable.volume_image)
        }

        binding.imgSpeaker.setOnClickListener {
            isSpeakerOn = !isSpeakerOn
            rtcEngine.setEnableSpeakerphone(isSpeakerOn)
            ////for testing
            binding.imgSpeaker.setImageResource(if (isSpeakerOn) R.drawable.volume_image else R.drawable.mute_icon)
        }

        binding.imgHangOut.setOnClickListener {
            endCall()
        }
    }

    private fun initializeAgoraEngine() {
        rtcEngine = RtcEngine.create(this@CallActivity, appId, object : IRtcEngineEventHandler() {})
    }

    private fun joinChannel() {
        rtcEngine.joinChannel(token, channelName, "", 0)
    }

    private fun endCall() {
        chronometer.stop()
        rtcEngine.leaveChannel()
        RtcEngine.destroy()
        onBackPressed()
    }

    private fun setupStatusBarAppearance() {
        // Change status bar color and appearance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun resetStatusBarAppearance() {
        // Reset status bar color and appearance to default
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    0, // Clear all appearance flags
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }
    }

    private fun setupClickListeners() {
        // Set click listener for the back button
        binding.imgBack.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        resetStatusBarAppearance()
    }
}
