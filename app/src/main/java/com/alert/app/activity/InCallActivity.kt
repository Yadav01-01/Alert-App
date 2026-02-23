package com.alert.app.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.AppBarConfiguration
import com.alert.app.R
import com.alert.app.base.AppConstant
import com.alert.app.base.BaseApplication
import com.alert.app.calling.IncomingAudioCallService
import com.alert.app.databinding.ActivityInCallBinding
import com.alert.app.databinding.ActivityMainBinding
import com.alert.app.viewmodel.InCallViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
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

@AndroidEntryPoint
class InCallActivity : AppCompatActivity() {

    private var isSpeakerMuted = false
    private var isMuted = false
    private lateinit var binding: ActivityInCallBinding
    private var rtcEngine: RtcEngine? = null
    private lateinit var viewModel: InCallViewModel
    private var ringtonePlayer: MediaPlayer? = null

    private val TAG = "AGORA_DEBUG"
    // ✅ TEST VALUES
    private var channelName = "call_fd9e3f48-2ab8-4d35-a061-b0d18e7443eb"
    private var appId = "3d45540a74844ab68670e75d586cc630"
    private var token ="007eJxTYHjQ+UDtCXOB0HTWf3IWd5XP8P0w85rhrsHGbtxXuqXmyBoFBuMUE1NTE4NEcxMLE5PEJDMLM3ODVHPTFFMLs+RkM2ODt3enZf59PS3zvspKFkYGRgYWIAYBJjDJDCZZwKQmQ3JiTk58WoplqnGaiYWuUWKSha5JirGpbqKBmaFukkGKoUWquYmJcWoSI4MBAJ5nKbc="
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
        stopService(Intent(this, IncomingAudioCallService::class.java))

        intent?.let {
            channelName = it.getStringExtra(AppConstant.CHANNEL).orEmpty()
            token = it.getStringExtra(AppConstant.TOKEN).orEmpty()
            appId = it.getStringExtra(AppConstant.APPiD).orEmpty()
           val callerName = it.getStringExtra(AppConstant.NAME).orEmpty()
            val image = it.getStringExtra(AppConstant.IMAGE).orEmpty()
            val actionType = it.getStringExtra("ACTION_TYPE").orEmpty()
            binding.userName.text = callerName
            Glide.with(this)
                .load(image)
                .placeholder(R.drawable.user_img_icon)
                .error(R.drawable.user_img_icon)
                .into(binding.userImg)
        }

        viewModel = ViewModelProvider(this)[InCallViewModel::class.java]

        viewModel = ViewModelProvider(this)[InCallViewModel::class.java]

        viewModel.time.observe(this) { time ->
            binding.chronometer.text = time
        }
        observeIncomingCall(token)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing
            }
        })

        binding.imgMike.setOnClickListener {
            toggleMute()
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


    // MUte and Unmute task

    private fun toggleMute() {
        isMuted = !isMuted

        rtcEngine?.muteLocalAudioStream(isMuted)

        if (isMuted) {
            Log.d(TAG, "MIC MUTED")
           // binding.imgMike.setImageResource(R.drawable.ic_mic_off)
            Toast.makeText(this, "Muted", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(TAG, "MIC UNMUTED")
            //binding.imgMute.setImageResource(R.drawable.ic_mic_on)
            Toast.makeText(this, "Unmuted", Toast.LENGTH_SHORT).show()
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


    fun observeIncomingCall(chatToken: String) {
        val firestore = FirebaseFirestore.getInstance()
        val safeToken = BaseApplication.safeDocIdFromToken(chatToken)

        firestore.collection("calls")
            .document(safeToken)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val data = snapshot.data ?: return@addSnapshotListener
                val status = data["status"] as? String ?: "ringing"

                when (status) {
                    "ended" -> {
                        endCall()
                    }
                }
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

    private val rtcEventHandler = object : IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {


                startRinging()

                Log.e(TAG, "JOINED CHANNEL uid=$uid")
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {


                stopRinging()     // 🔕 STOP RINGING

                viewModel.startTimer()

                Log.e(TAG, "REMOTE USER JOINED uid=$uid")
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {

                Log.e(TAG, "REMOTE USER LEFT reason=$reason")
                endCall()
            }
        }

        override fun onConnectionLost() {
            runOnUiThread {

                Log.e(TAG, "CONNECTION LOST")
                endCall()
            }
        }

        override fun onError(err: Int) {
            runOnUiThread {

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
        val firestore = FirebaseFirestore.getInstance()
        // Update Firestore so other party knows
        firestore.collection("calls")
            .document(BaseApplication.safeDocIdFromToken(token))
            .update("status", "ended")
            .addOnSuccessListener { Log.d("CallFirestore", "Call ended") }

        stopRinging()
        viewModel.stopTimer()
        cleanupAgora()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopTimer()
        cleanupAgora()
    }


    private fun startRinging() {
        if (ringtonePlayer == null) {
            ringtonePlayer = MediaPlayer.create(this, R.raw.call_ringing)
            ringtonePlayer?.isLooping = true
            ringtonePlayer?.start()
        }
    }

    private fun stopRinging() {
        ringtonePlayer?.stop()
        ringtonePlayer?.release()
        ringtonePlayer = null
    }
}
