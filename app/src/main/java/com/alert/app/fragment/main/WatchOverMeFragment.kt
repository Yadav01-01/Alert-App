package com.alert.app.fragment.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.databinding.FragmentWatchOverMeBinding

class WatchOverMeFragment : Fragment() {


    private lateinit var binding: FragmentWatchOverMeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWatchOverMeBinding.inflate(layoutInflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        (requireActivity() as MainActivity).setBottomLayout()?.visibility=View.GONE
        (requireActivity() as MainActivity).setImageShowTv()?.visibility=View.GONE
        (requireActivity() as MainActivity).setImgChatBoot().visibility =View.GONE

        // This line use for system back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })


        binding.btnExplore.setOnClickListener {
            findNavController().navigate(R.id.watchovermechoosestartingpointFragment)
        }

    }


}