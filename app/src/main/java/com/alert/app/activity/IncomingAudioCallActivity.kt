package com.alert.app.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.alert.app.R
import com.alert.app.base.AppConstant
import com.alert.app.calling.IncomingAudioCallService
import com.alert.app.calling.IncomingCallService
import com.alert.app.databinding.ActivityIncomingAudioCallBinding

class IncomingAudioCallActivity : AppCompatActivity() {

//    private lateinit var binding : ActivityIncomingAudioCallBinding
//
//    private lateinit var channelName: String
//    private lateinit var agoraToken: String
//    private lateinit var appId: String
//    private lateinit var callerName: String
//    private lateinit var image: String
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        binding = ActivityIncomingAudioCallBinding.inflate(LayoutInflater.from(this))
//        setContentView(binding.root)
//
//        intent?.let {
//            channelName = it.getStringExtra("channel_name") ?: ""
//            agoraToken = it.getStringExtra("agora_token") ?: ""
//            appId = it.getStringExtra("agora_app_id") ?: ""
//            callerName = it.getStringExtra("caller_name") ?: "Unknown Caller"
//            image = it.getStringExtra("image") ?: ""
//        }
//        binding.tvCaller.text = callerName
//        binding.tvInfo.text ="Incoming Audio Call"
//
//
//        Log.d(
//            "IncomingCall",
//            """
//            channelName: $channelName
//            agoraToken: $agoraToken
//            appId: $appId
//            callerName: $callerName
//            image: $image
//            """.trimIndent()
//        )
//
//        binding.btnAccept.setOnClickListener {
//
//            val inCallIntent = Intent(this, InCallActivity::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                putExtra(AppConstant.CHANNEL, channelName)
//                putExtra(AppConstant.TOKEN, agoraToken)
//                putExtra(AppConstant.APPiD, appId)
//                putExtra(AppConstant.NAME, callerName)
//                putExtra(AppConstant.IMAGE, image)
//                putExtra("ACTION_TYPE", "ANSWER")
//            }
//
//            startActivity(inCallIntent)
//            stopService(Intent(this, IncomingAudioCallService::class.java))
//            clearLockScreenFlags()
//            finish()
//        }
//
//        binding.btnDecline.setOnClickListener {
//            stopService(Intent(this, IncomingAudioCallService::class.java))
//            finish()
//        }
//    }
//
//    private fun clearLockScreenFlags() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setShowWhenLocked(false)
//            setTurnScreenOn(false)
//        } else {
//            @Suppress("DEPRECATION")
//            window.clearFlags(
//                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
//                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//            )
//        }
//        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//
//        // Optional: Force Keyguard to reappear
//        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            km.requestDismissKeyguard(this, null)
//        }
//    }
//
//
//    private fun showOnLockScreen() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setShowWhenLocked(true)
//            setTurnScreenOn(true)
//        } else {
//            @Suppress("DEPRECATION")
//            window.addFlags(
//                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
//                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//            )
//        }
//
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//    }
//
//
//    private fun enableFullScreen() {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//
//        val controller = WindowInsetsControllerCompat(window, window.decorView)
//        controller.hide(WindowInsetsCompat.Type.systemBars())
//        controller.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
//    }
private lateinit var binding: ActivityIncomingAudioCallBinding

    private lateinit var channelName: String
    private lateinit var agoraToken: String
    private lateinit var appId: String
    private lateinit var callerName: String
    private lateinit var image: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showOnLockScreen()
        enableFullScreen()

        binding = ActivityIncomingAudioCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ READ CORRECT KEYS
        channelName = intent.getStringExtra(AppConstant.CHANNEL) ?: ""
        agoraToken = intent.getStringExtra(AppConstant.TOKEN) ?: ""
        appId = intent.getStringExtra(AppConstant.APPiD) ?: ""
        callerName = intent.getStringExtra(AppConstant.NAME) ?: "Unknown Caller"
        image = intent.getStringExtra(AppConstant.IMAGE) ?: ""

        binding.tvCaller.text = callerName
        binding.tvInfo.text = "Incoming Audio Call"

        Log.d(
            "IncomingCall",
            """
            channelName: $channelName
            agoraToken: $agoraToken
            appId: $appId
            callerName: $callerName
            image: $image
            """.trimIndent()
        )

        binding.btnAccept.setOnClickListener {

            val inCallIntent = Intent(this, InCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(AppConstant.CHANNEL, channelName)
                putExtra(AppConstant.TOKEN, agoraToken)
                putExtra(AppConstant.APPiD, appId)
                putExtra(AppConstant.NAME, callerName)
                putExtra(AppConstant.IMAGE, image)
                putExtra("ACTION_TYPE", "ANSWER")
            }

            startActivity(inCallIntent)

            stopService(Intent(this, IncomingAudioCallService::class.java))

            clearLockScreenFlags()
            finish()
        }

        binding.btnDecline.setOnClickListener {
            stopService(Intent(this, IncomingAudioCallService::class.java))
            finish()
        }
    }

    // ==============================
    // LOCK SCREEN SUPPORT
    // ==============================

    private fun showOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun clearLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        } else {
            @Suppress("DEPRECATION")
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun enableFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
    }

}