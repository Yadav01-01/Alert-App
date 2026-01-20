package com.alert.app.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.alert.app.databinding.FragmentWelcomeScreenBinding

class WelcomeScreen : AppCompatActivity() {

    private var binding: FragmentWelcomeScreenBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= FragmentWelcomeScreenBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)

        binding?.apply {
            tvLogInButton.setOnClickListener { navigateToAuthScreen("Login") }
            tvRegisterButton.setOnClickListener { navigateToAuthScreen("Signup") }
        }

    }

    private fun navigateToAuthScreen(screen: String) {
        val intent = Intent(this@WelcomeScreen, AuthActivity::class.java)
        intent. putExtra("openScreen", screen)
        startActivity(intent)
        finish()
    }

}