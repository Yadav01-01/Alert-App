package com.alert.app.fragment.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.databinding.FragmentTutorialVideoBinding


class TutorialVideoFragment : Fragment() {
    private lateinit var binding: FragmentTutorialVideoBinding
    lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTutorialVideoBinding.inflate(layoutInflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.imgBack.setOnClickListener {
            navController.navigateUp()
        }


        binding.btnSkip.setOnClickListener {
            findNavController().navigate(R.id.setAlertFragment)
        }



    }


}