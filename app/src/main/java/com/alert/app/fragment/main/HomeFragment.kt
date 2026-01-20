package com.alert.app.fragment.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentHomeBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.User
import com.alert.app.model.emergencycontact.GetEmergencyContactModel
import com.alert.app.model.homemodel.HomeModel
import com.alert.app.viewmodel.homeviewmodel.HomeViewModel
import com.alert.app.viewmodel.mainactivitymodel.MainActivityViewModel
import com.alert.app.viewmodel.verificationviewmodel.VerificationOtpViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel : MainActivityViewModel
    private lateinit var viewModel: HomeViewModel


    // Constants for repeated resources
    private val locationImage = R.drawable.image_location_2
    private val yesImage = R.drawable.cuate
    private val noImage = R.drawable.image_no
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        mapView = binding.map
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = requireActivity() as MainActivity
        mainViewModel = ViewModelProvider(mainActivity)[MainActivityViewModel::class.java]
//        BaseApplication.openDialog()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setupUI()
        handleBackPress()
        setupListeners()

        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI() {
        mainActivity.setFooter("home")
        mainActivity.setImageShowTv()?.visibility = View.VISIBLE
        mainActivity.setImgChatBoot().visibility = View.VISIBLE

        val text = Html.fromHtml(
            "<font color='#6E6E6E'>If you are Okay, and feel safe around you!</font><br/>" +
                    "<font color='#6E6E6E'>Then HIT ON</font><font color='#000000'> YES</font>. <br/>" +
                    "<font color='#6E6E6E'>Or if anything is wrong - Clicking on </font><font color='#000000'>NO</font> <br/><font color='#6E6E6E'> will send the alert to your loved ones.</font>",
            Html.FROM_HTML_MODE_LEGACY
        )
        binding.tvText.text = text
    }

    private fun handleBackPress() {
        // Handle system back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.homeProfileFragment)
                }
            })
    }

    private fun setupListeners() {
        binding.viewMap.setOnClickListener {
            findNavController().navigate(R.id.mapFullScreenFragment)
        }
        binding.imgNotification.setOnClickListener {
            findNavController().navigate(R.id.notificationFragment)
        }

        binding.layMessage.setOnClickListener {
            findNavController().navigate(R.id.messageFragment)
        }

        binding.threeLine.setOnClickListener {
            val drawer = mainActivity.getDrawerLayout()
            if (drawer.isDrawerVisible(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            } else {
                drawer.openDrawer(GravityCompat.START)
            }
        }

        (requireActivity() as MainActivity).setImageShowTv()?.setVisibilityOnClick {

            binding.rlMapView.visibility = View.VISIBLE
            binding.viewScroll.post {
                binding.viewScroll.smoothScrollTo(0, binding.rlMapView.top)
            }
           /* MainScope().launch {
                delay(3000) // Delay for 3 seconds
                binding.imgLocation.setImageResource(locationImage) // Update the image after the delay
            }*/
            mainActivity.setImageShowTv()?.visibility = View.GONE
        }

        binding.tvOK.setOnClickListener {
            mainViewModel.setResponse(MainActivityViewModel.CheckInResponse.YES)
            updateUIForYesResponse()
        }

        binding.tvNo.setOnClickListener {
            mainViewModel.setResponse(MainActivityViewModel.CheckInResponse.NO)
            sendEmergencyMessageRequest()
            /*updateUIForNoResponse()*/
        }

    }

    private fun sendEmergencyMessageRequest() {
        Log.d("Emergency", "sendEmergencyMessageRequest called")
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.sendEmergencyMessageRequest().collect {
                    BaseApplication.dismissDialog()
                    handleApiResponse(it)
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun handleApiResponse(it: NetworkResult<JsonObject>) {
        when (it) {
            is NetworkResult.Success -> handleSuccessApiResponse(it.data.toString())
            is NetworkResult.Error ->{
                Log.d("EmergencyError", "sendEmergencyMessageRequest called1")
                showAlert(it.message, false)
            }
            else -> showAlert(it.message, false)
        }
    }

    // This is common function for show the alert box
    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(context, message,status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessApiResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, HomeModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.status == true) {
                updateUIForNoResponse()
             /*   if (apiModel.data != null) {
                    showDataInUI(apiModel.data)
                }*/
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int?, message: String?) {
        if (code== MessageClass.deactivatedUser || code== MessageClass.deletedUser){
            Log.d("EmergencyError", "sendEmergencyMessageRequest called2")
            showAlert(message, true)
        }else{
            showAlert(message, false)
        }
    }


    private fun updateUIForYesResponse() {
        binding.lay1.visibility = View.GONE
        binding.layYes.visibility = View.VISIBLE
        binding.tvSubtitleYes.visibility = View.GONE
        binding.imgYes.setImageResource(yesImage)
        binding.tvHeadingYes.text = getString(R.string.brb)
        binding.tvTitleYes.text = getString(R.string.we_are_here_for_you)
    }

    private fun updateUIForNoResponse() {
        binding.lay1.visibility = View.GONE
        binding.layYes.visibility = View.VISIBLE
        binding.tvSubtitleYes.visibility = View.VISIBLE
        binding.imgYes.setImageResource(noImage)
        binding.tvHeadingYes.text = getString(R.string.sos)
        binding.tvTitleYes.text = getString(R.string.alerted_your_emergency_contact)
    }

    private fun View.setVisibilityOnClick(action: () -> Unit) {
        this.setOnClickListener { action() }
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        // Optional: Automatically ask permission if not already granted
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    private fun enableMyLocation() {
        if (::googleMap.isInitialized) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                     googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f)) // 15f is zoom level

                    // Add marker
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("You are here")
                    )
                } else {
                    Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
