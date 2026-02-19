package com.alert.app.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.ui.AppBarConfiguration
import com.alert.app.R
import com.alert.app.base.AppConstant
import com.alert.app.databinding.ActivityInCallBinding
import com.alert.app.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInCallBinding
    private var rtcEngine: RtcEngine? = null

    private val TAG = "AGORA_DEBUG"

    // ✅ TEST VALUES
    private val channelName = "call_fd9e3f48-2ab8-4d35-a061-b0d18e7443eb"
    private val appId = "3d45540a74844ab68670e75d586cc630"
    private val token ="007eJxTYHjQ+UDtCXOB0HTWf3IWd5XP8P0w85rhrsHGbtxXuqXmyBoFBuMUE1NTE4NEcxMLE5PEJDMLM3ODVHPTFFMLs+RkM2ODt3enZf59PS3zvspKFkYGRgYWIAYBJjDJDCZZwKQmQ3JiTk58WoplqnGaiYWuUWKSha5JirGpbqKBmaFukkGKoUWquYmJcWoSI4MBAJ5nKbc="
    private val uId =0
    @Volatile
    private var isAgoraInitialized = false

    @Volatile
    private var isCallEnded = false

    companion object {
        private const val PERMISSION_REQ_ID = 22
    }

    // ================= LIFECYCLE =================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgHangOut.setOnClickListener {
            Toast.makeText(this, "Call End Clicked", Toast.LENGTH_SHORT).show()
            endCall()
        }

        if (!hasPermissions()) {
            requestPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasPermissions()) {
            binding.root.post {
                initAgoraAndJoinSafely()
            }
        }
    }

    // ================= PERMISSIONS =================

    private fun hasPermissions(): Boolean {
        return requiredPermissions().all {
            ContextCompat.checkSelfPermission(this, it) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            requiredPermissions(),
            PERMISSION_REQ_ID
        )
    }

    private fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }
    }

    // ================= AGORA INIT =================

    private fun initAgoraAndJoinSafely() {
        if (isAgoraInitialized) {
            Log.e(TAG, "Agora already initialized")
            return
        }

        isAgoraInitialized = true
        Toast.makeText(this, "Init Agora Started", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "Init Agora Started")

        try {
            initializeAgora()
            Toast.makeText(this, "Agora Engine Created", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Agora Engine Created")

            setupAudio()
            Toast.makeText(this, "Audio Setup Done", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Audio Setup Done")

            joinChannel()
            Toast.makeText(this, "Join Channel Called", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Join Channel Called")

        } catch (e: Exception) {
            isAgoraInitialized = false
            Toast.makeText(this, "Agora Init Failed: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Agora Init Failed", e)
        }
    }

    private fun initializeAgora() {
        val cleanAppId = appId.trim()
        require(cleanAppId.length == 32) { "Invalid App ID" }

        val config = RtcEngineConfig().apply {
            mContext = applicationContext
            mAppId = cleanAppId
            mEventHandler = rtcEventHandler
        }

        rtcEngine = RtcEngine.create(config)
    }

    private fun setupAudio() {
        rtcEngine?.apply {
            setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            enableAudio()
            enableLocalAudio(true)
            muteLocalAudioStream(false)
            setEnableSpeakerphone(true)
            setDefaultAudioRoutetoSpeakerphone(true)
            enableAudioVolumeIndication(300, 3, true)
        }
    }

    private fun joinChannel() {
        val options = ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            publishMicrophoneTrack = true
            autoSubscribeAudio = true
        }

        val result = rtcEngine?.joinChannel(token, channelName, uId, options)
        Log.e(TAG, "joinChannel result = $result")

        rtcEngine?.muteAllRemoteAudioStreams(false)
    }

    // ================= EVENTS =================

    private val rtcEventHandler = object : IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {
                Toast.makeText(
                    this@InCallActivity,
                    "JOINED CHANNEL ✅ uid=$uid",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "JOINED CHANNEL uid=$uid")
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                Toast.makeText(
                    this@InCallActivity,
                    "REMOTE USER JOINED ✅ uid=$uid",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "REMOTE USER JOINED uid=$uid")
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Toast.makeText(
                    this@InCallActivity,
                    "REMOTE USER LEFT ❌ reason=$reason",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "REMOTE USER LEFT reason=$reason")
                endCall()
            }
        }

        override fun onConnectionLost() {
            runOnUiThread {
                Toast.makeText(
                    this@InCallActivity,
                    "CONNECTION LOST ❌",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "CONNECTION LOST")
                endCall()
            }
        }

        override fun onError(err: Int) {
            runOnUiThread {
                Toast.makeText(
                    this@InCallActivity,
                    "AGORA ERROR ❌ code=$err",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "AGORA ERROR code=$err")
            }
        }

        override fun onAudioVolumeIndication(
            speakers: Array<out AudioVolumeInfo>?,
            totalVolume: Int
        ) {
            Log.e(TAG, "MIC VOLUME = $totalVolume")
        }
    }

    // ================= CLEANUP =================

    private fun cleanupAgora() {
        rtcEngine?.leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
        isAgoraInitialized = false
        Log.e(TAG, "Agora cleaned up")
    }

    private fun endCall() {
        if (isCallEnded) return
        isCallEnded = true

        cleanupAgora()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupAgora()
    }
}