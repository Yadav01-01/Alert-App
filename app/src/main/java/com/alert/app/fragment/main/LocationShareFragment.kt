package com.alert.app.fragment.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.databinding.FragmentLocationShareBinding

class LocationShareFragment : Fragment() {

    private lateinit var binding: FragmentLocationShareBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocationShareBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setFooter("locationShare")
        (requireActivity() as MainActivity).setImageShowTv()?.visibility=View.GONE
        (requireActivity() as MainActivity).setImgChatBoot().visibility =View.GONE

        binding.btnAlertNow.setOnClickListener {
           findNavController().navigate(R.id.contactListFragment)
        }
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}