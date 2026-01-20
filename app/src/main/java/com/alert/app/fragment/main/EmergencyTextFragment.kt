package com.alert.app.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.databinding.FragmentEmergencyTextBinding

class EmergencyTextFragment : Fragment() {

    private var _binding: FragmentEmergencyTextBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEmergencyTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupBackButton()
        setupListeners()
    }

    private fun setupUI() {
        (requireActivity() as? MainActivity)?.apply {
            setImageShowTv()?.visibility = View.GONE
            setImgChatBoot().visibility = View.GONE
        }
    }

    private fun setupBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun setupListeners() {
        binding.btnOkay.setOnClickListener {
            findNavController().navigate(R.id.emergencyTextSubmitFragment)
        }

//        binding.backBtn.setOnClickListener {
//            setupBackButton()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
