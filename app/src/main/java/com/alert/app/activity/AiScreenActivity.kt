package com.alert.app.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alert.app.R
import com.alert.app.databinding.ActivityAiScreenBinding

class AiScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAiScreenBinding.inflate(layoutInflater)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAiAction.setOnClickListener {
            Toast.makeText(this, "AI action triggered!", Toast.LENGTH_SHORT).show()
        }


        binding.btnAiDecline.setOnClickListener {
            finish()
        }

    }


}