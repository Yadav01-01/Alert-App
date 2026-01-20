package com.alert.app.fragment.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alert.app.activity.WelcomeScreen
import com.alert.app.databinding.FragmentOnBoarding1Binding

class OnBoardingFragment1 : Fragment() {

    private var binding:FragmentOnBoarding1Binding?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnBoarding1Binding.inflate(layoutInflater, container, false)

        binding!!.rlSkipOnborading1.setOnClickListener{
            val intent = Intent(context, WelcomeScreen::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return binding!!.root

    }
}