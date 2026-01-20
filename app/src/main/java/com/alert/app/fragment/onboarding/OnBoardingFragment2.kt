package com.alert.app.fragment.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alert.app.activity.WelcomeScreen
import com.alert.app.databinding.FragmentOnBoarding2Binding


class OnBoardingFragment2 : Fragment() {

    private var binding: FragmentOnBoarding2Binding?=null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnBoarding2Binding.inflate(layoutInflater, container, false)

        binding!!.rlSkipOnborading2.setOnClickListener{
            val intent = Intent(context, WelcomeScreen::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return binding!!.root

    }
}