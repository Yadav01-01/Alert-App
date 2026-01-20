package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.BuildConfig
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.NearByPepoleAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentMapFullScreenBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.mapView.NearbyUsersResponse
import com.alert.app.model.mapView.UserData
import com.alert.app.viewmodel.mapViewviewmodel.MapViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapFullScreenFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentMapFullScreenBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null
    private val viewModel: MapViewModel by viewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapFullScreenBinding.inflate(inflater, container, false)
        mapView = binding.map
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Store MainActivity reference
        mainActivity = requireActivity() as MainActivity

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setupUI()
        setupListeners()
        setupBackPressedHandler()

        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI() {
        mainActivity.apply {
            setFooter("map")
            setImageShowTv()?.visibility = View.GONE
            setImgChatBoot().visibility = View.GONE
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getNearbyUser(latitude: String, longitude: String) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getNearbyUser(latitude,longitude).collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val nearbyUsersResponse = Gson().fromJson(it, NearbyUsersResponse::class.java)
                                if (nearbyUsersResponse.code==200) {
                                  val users = nearbyUsersResponse.data
                                    // Setup RecyclerView Adapter
                                    binding.rcyData.adapter = NearByPepoleAdapter(requireContext(),users)
                                    users.forEach { user ->
                                        user.latitude?.let { lat->
                                            user.longitude?.let { lon->
                                                createMarkerFromView(requireContext(), BuildConfig.BASE_URL+user.profile_pic)
                                                { icon ->
                                                    val marker =    googleMap.addMarker(
                                                        MarkerOptions()
                                                            .position(LatLng(lat.toDouble(),lon.toDouble()))
                                                            .icon(icon)
                                                            .anchor(0.5f, 1f))
                                                    marker?.tag = user
                                                }
                                            }
                                        }
                                    }
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

    private fun setupListeners() {
        binding.apply {
            imgNotification.setOnClickListener {
                findNavController().navigate(R.id.notificationFragment)
            }
            btnSeeAllNearbyPeople.setOnClickListener {
                binding.btnSeeAllNearbyPeople.visibility = View.GONE
                binding.rlMapView.visibility = View.GONE
                binding.rcyData.visibility = View.VISIBLE
              //  findNavController().navigate(R.id.nearByPepopleFragment)
            }
            threeLine.setOnClickListener {
                val drawerLayout = mainActivity.getDrawerLayout()
                if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }
            layMessage.setOnClickListener {
                findNavController().navigate(R.id.messageFragment)
            }
        }
    }

    private fun setupBackPressedHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.rcyData.visibility == View.VISIBLE){
                    binding.btnSeeAllNearbyPeople.visibility = View.VISIBLE
                    binding.rlMapView.visibility = View.VISIBLE
                    binding.rcyData.visibility = View.GONE
                }else {
                    findNavController().navigate(R.id.homeProfileFragment)
                }
            }
        })
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

    @SuppressLint("PotentialBehaviorOverride")
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
            googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                override fun getInfoContents(marker: Marker): View? {
                    return null
                }

                override fun getInfoWindow(marker: Marker): View? {
                    val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
                    val tvSnippet = view.findViewById<TextView>(R.id.tvSnippet)
                    val user = marker.tag as? UserData
                    if (user!=null){
                        tvSnippet.text = "Connect with ${user.name}"
                    }else{
                        return null
                    }
                    return view
                }
            })
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    getNearbyUser(location.latitude.toString(), location.longitude.toString())
                    val latLng = LatLng(location.latitude, location.longitude)
                    showCustomLocationMarker(latLng)
                   // googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f)) // 15f is zoom level
                } else {
                    Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCustomLocationMarker(latLng:LatLng) {
                // Remove previous marker if exists
                currentLocationMarker?.remove()
                // Add your custom marker icon
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .icon(getBitmapDescriptor(R.drawable.your_location)) // use your uploaded image here
                    .anchor(0.5f, 0.5f) // center the icon
                currentLocationMarker = googleMap.addMarker(markerOptions)
                // Move camera to current location
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }

    private fun getBitmapDescriptor(@DrawableRes id: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(requireContext(), id)!!
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    fun createMarkerFromView(context: Context, imageUrl: String, callback: (BitmapDescriptor) -> Unit) {
        val markerView = LayoutInflater.from(context).inflate(R.layout.custom_marker, null)

        val profileImage = markerView.findViewById<CircleImageView>(R.id.imgProfile)

        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .placeholder(R.drawable.marker_demmy_pic)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    profileImage.setImageBitmap(resource)

                    // Convert the view to bitmap
                    val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    markerView.measure(measureSpec, measureSpec)
                    markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
                    val bitmap = Bitmap.createBitmap(
                        markerView.measuredWidth, markerView.measuredHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    markerView.draw(canvas)

                    callback(BitmapDescriptorFactory.fromBitmap(bitmap))
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }





    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
