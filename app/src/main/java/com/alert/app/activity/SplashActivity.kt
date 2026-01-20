package com.alert.app.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import com.alert.app.base.SessionManagement
import com.alert.app.commonworkutils.CommonWorkUtils
import com.alert.app.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private var binding:ActivitySplashBinding?=null
    private var commonWorkUtils: CommonWorkUtils? = null
    private lateinit var sessionManagement: SessionManagement


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySplashBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)

        sessionManagement = SessionManagement(this)


        initialize()


    }

    private fun initialize() {
        Handler().postDelayed({
            if (sessionManagement.getLoginSession()) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this@SplashActivity, OnBoardingActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 3000)
    }


}