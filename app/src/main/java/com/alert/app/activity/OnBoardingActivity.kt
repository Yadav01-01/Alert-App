package com.alert.app.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import com.alert.app.adapter.OnBoardingViewPagerAdapter
import com.alert.app.databinding.ActivityOnBoardingBinding
import com.alert.app.fragment.onboarding.OnBoardingFragment1
import com.alert.app.fragment.onboarding.OnBoardingFragment2
import com.alert.app.fragment.onboarding.OnBoardingFragment3

class OnBoardingActivity : AppCompatActivity() {
    private var binding: ActivityOnBoardingBinding? = null
    private val slideHandler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private val slideDelay: Long = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)
        initialize()
    }

    private fun initialize() {
        // Setup ViewPager with Fragments
        val fragmentList = arrayListOf(
            OnBoardingFragment1(),
            OnBoardingFragment2(),
            OnBoardingFragment3()
        )
        val adapter = OnBoardingViewPagerAdapter(fragmentList, this.supportFragmentManager, lifecycle)
        binding!!.onBoardingViewPager.adapter = adapter

        // Start auto-slide
        startAutoSlide(fragmentList.size)
    }
    private fun startAutoSlide(totalPages: Int) {
        val slideRunnable = object : Runnable {
            override fun run() {
                // Check if last page
                if (currentPage == totalPages - 1) {
                    navigateToWelcomeScreen() // Navigate to Welcome Screen
                } else {
                    // Increment page position
                    currentPage++
                    binding?.onBoardingViewPager?.currentItem = currentPage
                    slideHandler.postDelayed(this, slideDelay) // Schedule next slide
                }
            }
        }
        slideHandler.postDelayed(slideRunnable, slideDelay) // Start the first slide
    }

    private fun navigateToWelcomeScreen() {
        // Replace this with your navigation logic to Welcome Screen
        val intent = Intent(this, WelcomeScreen::class.java)
        startActivity(intent)
        finish() // Close Onboarding Screen
    }


    override fun onDestroy() {
        super.onDestroy()
        // Remove callbacks to avoid memory leaks
        slideHandler.removeCallbacksAndMessages(null)
    }
}
