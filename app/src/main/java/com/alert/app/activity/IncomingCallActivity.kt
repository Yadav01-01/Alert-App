package com.alert.app.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.alert.app.R
import com.alert.app.calling.IncomingCallService

class IncomingCallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOnLockScreen()
        setContentView(R.layout.activity_incoming_call)
        enableEdgeToEdge()

        // Fullscreen after layout
        enableFullScreen()

        val callerNameTv = findViewById<TextView>(R.id.tv_caller)
        val acceptBtn = findViewById<Button>(R.id.btn_accept)
        val declineBtn = findViewById<Button>(R.id.btn_decline)

        // Get caller data
        val callerName = intent.getStringExtra("CALLER_NAME") ?: "Unknown Caller"
        val sessionId = intent.getStringExtra("CALL_SESSION_ID") ?: "123"

        callerNameTv.text = "Incoming Alert"

        acceptBtn.setOnClickListener {
            stopService(Intent(this, IncomingCallService::class.java))

            clearLockScreenFlags()

//            startActivity(
//                Intent(this, InCallActivity::class.java).apply {
//                    putExtra("CALL_SESSION_ID", sessionId)
//                    putExtra("CALLER_NAME", callerName)
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                }
//            )
            finish()
        }

        declineBtn.setOnClickListener {
            stopService(Intent(this, IncomingCallService::class.java))
            finish()
        }
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

        // Optional: Force Keyguard to reappear
        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            km.requestDismissKeyguard(this, null)
        }
    }


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


    private fun enableFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
    }
}