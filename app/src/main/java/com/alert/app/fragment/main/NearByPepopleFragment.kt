package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentNearByPepopleBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.mapView.NearbyUsersResponse
import com.alert.app.viewmodel.mapViewviewmodel.MapViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch

class NearByPepopleFragment : Fragment() {

    private lateinit var binding: FragmentNearByPepopleBinding
    private lateinit var mainActivity: MainActivity
    private val viewModel: MapViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNearByPepopleBinding.inflate(inflater, container, false)
        mainActivity = requireActivity() as MainActivity  // Get reference to MainActivity once
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
//        getNearbyUser()
    }

    private fun setupUI() {
        // Set footer text and visibility for UI elements
        mainActivity.setFooter("nearByPeople")
        mainActivity.setImageShowTv()?.visibility = View.GONE
        mainActivity.setImgChatBoot().visibility = View.VISIBLE

        // Setup RecyclerView Adapter
     //   binding.rcyData.adapter = NearByPepoleAdapter(requireContext())


    }

    private fun setupListeners() {
        // Toggle the drawer
        binding.threeLine.setOnClickListener {
            val drawerLayout = mainActivity.getDrawerLayout()
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Navigation to message fragment
        binding.layMessage.setOnClickListener {
            navigateTo(R.id.messageFragment)
        }

        // Navigation to notification fragment
        binding.imgNotification.setOnClickListener {
            navigateTo(R.id.notificationFragment)
        }
    }

    // Helper function for navigation
    private fun navigateTo(destinationId: Int) {
        findNavController().navigate(destinationId)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getNearbyUser(latitude: String, longitude: String) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getNearbyUser(latitude,longitude).collect { it ->
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val nearbyUsersResponse = Gson().fromJson(it, NearbyUsersResponse::class.java)
                                if (nearbyUsersResponse.code==200) {
                                    val users = nearbyUsersResponse.data
                                }else{
                                    Toast.makeText(
                                        requireContext(),
                                        nearbyUsersResponse.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }else{
            showAlert(requireContext(), MessageClass.networkError,false)
        }
    }
}
