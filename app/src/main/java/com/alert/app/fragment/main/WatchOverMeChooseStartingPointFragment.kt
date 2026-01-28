package com.alert.app.fragment.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.PlaceAutoSuggestAdapter
import com.alert.app.databinding.FragmentWatchovermechoosestartingpointBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

class WatchOverMeChooseStartingPointFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentWatchovermechoosestartingpointBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var placesClient: PlacesClient
    private lateinit var currentLocationAdapter: PlaceAutoSuggestAdapter
    private lateinit var destinationAdapter: PlaceAutoSuggestAdapter

    private var destinationLatLng: LatLng? = null
    private var currentLatLng: LatLng? = null
    private var pickupSpotLatLng: LatLng? = null // Starting point
    private var routePoints: List<LatLng> = listOf()
    private var userPathPoints: MutableList<LatLng> = mutableListOf()
    private var deviationPathPoints: MutableList<LatLng> = mutableListOf()

    private var optimalRoutePolyline: Polyline? = null
    private var userRoutePolyline: Polyline? = null
    private var deviationRoutePolyline: Polyline? = null
    private var userMarker: Marker? = null
    private var pickupMarker: Marker? = null
    private var destinationMarker: Marker? = null

    private var isDeviated = false
    private var deviationStartPoint: LatLng? = null
    private var lastCorrectPoint: LatLng? = null

    // Testing points - added for easier testing
    private val testCorrectPathPoints = mutableListOf<Double>()

    private val testWrongPathPoints = mutableListOf<Double>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWatchovermechoosestartingpointBinding.inflate(layoutInflater, container, false)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.api_key))
        }
        placesClient = Places.createClient(requireContext())

        currentLocationAdapter = PlaceAutoSuggestAdapter(requireContext(), placesClient)
        destinationAdapter = PlaceAutoSuggestAdapter(requireContext(), placesClient)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding.map.onCreate(savedInstanceState)
        binding.map.getMapAsync(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.edtCurrentAddress.setAdapter(currentLocationAdapter)
        binding.edtDestinationAddress.setAdapter(destinationAdapter)

        // Set test locations


        currentLatLng = pickupSpotLatLng



        // Draw initial route
        drawRouteToDestination()


    }

    // New method to simulate entire path
    private fun simulatePath(points: List<LatLng>) {
        // Reset all tracking variables
        userPathPoints.clear()
        deviationPathPoints.clear()
        isDeviated = false
        deviationStartPoint = null
        lastCorrectPoint = null

        // Clear previous polylines
        userRoutePolyline?.remove()
        deviationRoutePolyline?.remove()
        userMarker?.remove()

        points.forEachIndexed { index, latLng ->
            binding.root.postDelayed({
                updateUserLocation(latLng, true)
            }, index * 2000L) // 2 second delay between points
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    updateUserLocation(LatLng(location.latitude, location.longitude), false)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun updateUserLocation(newLatLng: LatLng, isSimulated: Boolean) {
        this.currentLatLng = newLatLng

        // Check if user is on correct route
        val isOnRoute = PolyUtil.isLocationOnPath(newLatLng, routePoints, true, 100.0)

        if (isOnRoute) {
            // User is on correct route
            if (isDeviated) {
                // User was deviated but now back on route
                handleBackOnRoute()
            } else {
                // User continues on correct route
                userPathPoints.add(newLatLng)
                lastCorrectPoint = newLatLng
            }
        } else {
            // User is off route
            if (!isDeviated) {
                // Start of deviation
                handleStartDeviation(newLatLng)
            } else {
                // Continue deviation
                deviationPathPoints.add(newLatLng)
            }
        }

        drawUserPosition(newLatLng, isOnRoute)
        updateUiStatus(isOnRoute)

        if (!isSimulated) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15f))
        }
    }

    private fun handleStartDeviation(deviationPoint: LatLng) {
        isDeviated = true
        deviationStartPoint = lastCorrectPoint ?: pickupSpotLatLng
        deviationPathPoints.clear()
        deviationPathPoints.add(deviationStartPoint!!)
        deviationPathPoints.add(deviationPoint)

        Toast.makeText(requireContext(), "Route deviation detected!", Toast.LENGTH_LONG).show()

        // In real app, notify emergency contacts here
        // sendEmergencyAlert()
    }

    private fun handleBackOnRoute() {
        isDeviated = false
        deviationPathPoints.clear()
        deviationRoutePolyline?.remove()
        deviationRoutePolyline = null

        Toast.makeText(requireContext(), "Back on correct route", Toast.LENGTH_SHORT).show()

        // Update last correct point
        lastCorrectPoint = currentLatLng
        userPathPoints.add(currentLatLng!!)
    }

    private fun drawUserPosition(position: LatLng, isOnRoute: Boolean) {
        // Update user marker
        userMarker?.remove()
        userMarker = mMap.addMarker(
            MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.track_image)) // Custom user icon
                .title(if (isOnRoute) "On Route" else "Off Route")
                .snippet("Current Location")
        )

        // Draw user path (blue line when on route)
        if (userPathPoints.size > 1) {
            userRoutePolyline?.remove()
            userRoutePolyline = mMap.addPolyline(
                PolylineOptions()
                    .addAll(userPathPoints)
                    .color(Color.BLUE)
                    .width(8f)
                    .zIndex(3f)
            )
        }

        // Draw deviation path (red line when off route)
        if (isDeviated && deviationPathPoints.size > 1) {
            deviationRoutePolyline?.remove()
            deviationRoutePolyline = mMap.addPolyline(
                PolylineOptions()
                    .addAll(deviationPathPoints)
                    .color(Color.RED)
                    .width(8f)
                    .zIndex(4f)
            )
        }
    }

    private fun updateUiStatus(isOnRoute: Boolean) {
        val mainActivity = requireActivity() as MainActivity
        if (isOnRoute) {
            mainActivity.setImgseetimer().visibility = View.GONE
            mainActivity.setHeading().text = calculateETAAndDistance()
            mainActivity.setTitle().text = "On Correct Path"
            mainActivity.setSubTitle().text = "Continue following this route"
        } else {
            mainActivity.setImgseetimer().visibility = View.VISIBLE
            mainActivity.setHeading().text = "Off Route - ${calculateDistanceFromRoute()} away"
            mainActivity.setTitle().text = "Warning!"
            mainActivity.setSubTitle().text = "You have deviated from the planned route"
        }
    }

    private fun calculateETAAndDistance(): String {
        if (routePoints.isEmpty() || currentLatLng == null) return "Calculating route..."

        val nearestPoint = routePoints.minByOrNull {
            SphericalUtil.computeDistanceBetween(currentLatLng!!, it)
        } ?: return "Route not available"

        val nearestPointIndex = routePoints.indexOf(nearestPoint)
        val remainingRoute = routePoints.drop(nearestPointIndex)

        val remainingDistance = if (remainingRoute.size > 1) {
            SphericalUtil.computeLength(remainingRoute)
        } else {
            SphericalUtil.computeDistanceBetween(currentLatLng!!, destinationLatLng!!)
        }

        val etaMinutes = (remainingDistance / 83.33).toInt() // Assuming 5km/h walking speed
        return "${etaMinutes} min (${"%.1f".format(remainingDistance / 1000)} km) to destination"
    }

    private fun calculateDistanceFromRoute(): String {
        if (routePoints.isEmpty() || currentLatLng == null) return "Unknown"

        val nearestPoint = routePoints.minByOrNull {
            SphericalUtil.computeDistanceBetween(currentLatLng!!, it)
        } ?: return "Unknown"

        val distance = SphericalUtil.computeDistanceBetween(currentLatLng!!, nearestPoint)
        return if (distance < 1000) "${distance.toInt()} meters" else "%.1f km".format(distance / 1000)
    }

    private fun drawRouteToDestination() {
        if (pickupSpotLatLng == null || destinationLatLng == null) return

        val apiKey = getString(R.string.api_key)
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${pickupSpotLatLng!!.latitude},${pickupSpotLatLng!!.longitude}" +
                "&destination=${destinationLatLng!!.latitude},${destinationLatLng!!.longitude}" +
                "&mode=walking&key=$apiKey"

        thread {
            try {
                val result = URL(url).readText()
                val jsonObject = JSONObject(result)
                processDirectionsResponse(jsonObject)
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    // Fallback to straight line for testing
//                    routePoints = listOf(pickupSpotLatLng!!, destinationLatLng!!)
//                    drawMapRoute()
                }
            }
        }
    }

    private fun processDirectionsResponse(jsonObject: JSONObject) {
        val routes = jsonObject.getJSONArray("routes")
        if (routes.length() > 0) {
            val points = routes.getJSONObject(0)
                .getJSONObject("overview_polyline")
                .getString("points")

            routePoints = PolyUtil.decode(points)

            requireActivity().runOnUiThread {
                drawMapRoute()
            }
        }
    }

    private fun drawMapRoute() {
        mMap.clear()
        userPathPoints.clear()
        deviationPathPoints.clear()

        // Draw optimal route (gray/light blue color)
        optimalRoutePolyline = mMap.addPolyline(
            PolylineOptions()
                .addAll(routePoints)
                .color(Color.parseColor("#87CEEB")) // Light blue color
                .width(12f)
                .zIndex(1f)
        )

        // Add pickup spot marker
        pickupMarker = mMap.addMarker(
            MarkerOptions()
                .position(pickupSpotLatLng!!)
                .title("Pickup Spot")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        // Add destination marker
        destinationMarker = mMap.addMarker(
            MarkerOptions()
                .position(destinationLatLng!!)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        // Initialize user path with pickup spot
        userPathPoints.add(pickupSpotLatLng!!)
        lastCorrectPoint = pickupSpotLatLng

        // Move camera to show entire route
        val bounds = routePoints.fold(LatLngBounds.Builder()) { builder, latLng ->
            builder.include(latLng)
            builder
        }.build()

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.map.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }
}
//package com.alert.app.fragment.main
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.Dialog
//import android.content.pm.PackageManager
//import android.graphics.Color
//import android.graphics.drawable.ColorDrawable
//import android.location.Geocoder
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.WindowManager
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.activity.OnBackPressedCallback
//import androidx.core.app.ActivityCompat
//import androidx.navigation.fragment.findNavController
//import com.alert.app.R
//import com.alert.app.activity.MainActivity
//import com.alert.app.adapter.PlaceAutoSuggestAdapter
//import com.alert.app.databinding.FragmentWatchovermechoosestartingpointBinding
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationCallback
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.MarkerOptions
//import com.google.android.gms.maps.model.Polyline
//import com.google.android.gms.maps.model.PolylineOptions
//import com.google.android.libraries.places.api.Places
//import com.google.android.libraries.places.api.model.AutocompletePrediction
//import com.google.android.libraries.places.api.model.Place
//import com.google.android.libraries.places.api.net.FetchPlaceRequest
//import com.google.android.libraries.places.api.net.PlacesClient
//import com.google.android.libraries.places.widget.Autocomplete
//import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
//import com.google.maps.android.PolyUtil
//import org.json.JSONObject
//import java.net.URL
//import kotlin.concurrent.thread
//
//
//class WatchOverMeChooseStartingPointFragment : Fragment(), OnMapReadyCallback {
//
//    private lateinit var binding: FragmentWatchovermechoosestartingpointBinding
//    private var chooseType = "Top"
//
//    private lateinit var mMap: GoogleMap
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//    private var routePoints: List<LatLng> = listOf()
//    private var destinationLatLng: LatLng? = null
//    private var polyline: Polyline? = null
//    private lateinit var placesClient: PlacesClient
//    private lateinit var currentLocationAdapter: PlaceAutoSuggestAdapter
//    private lateinit var destinationAdapter: PlaceAutoSuggestAdapter
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        binding = FragmentWatchovermechoosestartingpointBinding.inflate(layoutInflater, container, false)
//        // Initialize Places API
//        val apiKey = getString(R.string.api_key)
//        if (!Places.isInitialized()) {
//            Places.initialize(requireContext(), apiKey)
//        }
//        placesClient = Places.createClient(requireContext())
//
//        // Set up adapters
//        currentLocationAdapter = PlaceAutoSuggestAdapter(requireContext(), placesClient)
//        destinationAdapter = PlaceAutoSuggestAdapter(requireContext(), placesClient)
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
//
//
//        binding.map.onCreate(savedInstanceState)
////        binding.map.getMapAsync { map ->
//////            googleMap = map
////
////        }
//        binding.map.getMapAsync { googleMap ->
//            mMap = googleMap
//            onMapReady(googleMap) // Optional, to call your onMapReady logic
//        }
//
//
//        /*     val mapFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//             mapFragment.getMapAsync(this)*/
//
//      //  setupPlacesAutoComplete()
//
//  /*      binding.map = binding.map
//        binding.map.onCreate(savedInstanceState)
//        binding.map.getMapAsync(this)*/
//        return binding.root
//    }
//
//
//    @SuppressLint("SetTextI18n")
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding.edtCurrentAddress.setAdapter(currentLocationAdapter)
//        binding.edtDestinationAddress.setAdapter(destinationAdapter)
//// Set Noida Special Economic Zone as Start
//        binding.edtCurrentAddress.setText("Noida Special Economic Zone")
//        val startLatLng = LatLng(28.5687, 77.3507) // SEZ
//
//// Set Noida City Center as Destination
//        binding.edtDestinationAddress.setText("Noida City Center")
//        destinationLatLng = LatLng(28.5708, 77.3211) // City Center
//
//// Draw route directly
//        drawRouteToDestination()
//
//        // Handle item clicks
//        binding.edtCurrentAddress.setOnItemClickListener { _, _, position, _ ->
//            val item = currentLocationAdapter.getItem(position)
//            item?.let { selectPlace(it) }
//        }
//
//        binding.edtDestinationAddress.setOnItemClickListener { _, _, position, _ ->
//            val item = destinationAdapter.getItem(position)
//            item?.let { selectPlace(it) }
//        }
//        (requireActivity() as MainActivity).setBottomLayout()?.visibility=View.GONE
//        (requireActivity() as MainActivity).setImageShowTv()?.visibility=View.GONE
//        (requireActivity() as MainActivity).setImgChatBoot().visibility =View.GONE
//
//        // This line use for system back button
//        requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    findNavController().navigateUp()
//                }
//            })
//
//        binding.btnExplore.setOnClickListener {
//            if (destinationLatLng != null) {
//                drawRouteToDestination()
//            } else {
//                Toast.makeText(requireActivity(), "Please select a destination", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        (requireActivity() as MainActivity).setImgLocation().setOnClickListener {
//            (requireActivity() as MainActivity).setImgseetimer().visibility=View.VISIBLE
//            (requireActivity() as MainActivity).setHeading().text="5 min (4 miles) away"
//            (requireActivity() as MainActivity).setTitle().text="Oooopss!!!!"
//            (requireActivity() as MainActivity).setSubTitle().text="Seems like you have distracted from the actual\npath. Your emergency contact has been notified."
//        }
//
//        (requireActivity() as MainActivity).setImgseetimer().setOnClickListener {
//             alertBoxTimerShow()
//        }
//
//        binding.imageBack.setOnClickListener {
//            findNavController().navigateUp()
//        }
//        binding.btnCorrectPath.setOnClickListener {
//            simulateCorrectPathLocation()
//        }
//
//        binding.btnWrongPath.setOnClickListener {
//            simulateWrongPathLocation()
//        }
//
//
//     /*   binding.layCurrent.setOnClickListener {
//            binding.tvCurrent.text = "123,any street, US"
//            binding.edSearch.hint = "Choose starting point"
//            binding.imgBottom.setImageResource(R.drawable.circle_inactive_image)
//            binding.imgTop.setImageResource(R.drawable.color_circle_active)
//        }*/
//
//     /*   binding.layDestination.setOnClickListener {
//            binding.tvDestination.text = "67, Ar road, US"
//            binding.edSearch.hint = "Choose destination point"
//            binding.imgTop.setImageResource(R.drawable.circle_inactive_image)
//            binding.imgBottom.setImageResource(R.drawable.color_circle_active)
//        }*/
//
//       val apiKey = getString(R.string.api_key)
//        // Initialize Places API if not already initialized
//        if (!Places.isInitialized()) {
//            Places.initialize(requireActivity(), apiKey)
//        }
//        binding.imgBottom.setOnClickListener {
//            changeIcon("Bottom")
//        }
//        binding.imgTop.setOnClickListener {
//            changeIcon("Top")
//        }
//        binding.laySearch.setOnClickListener {
//            startLocationPicker()
//        }
//
//    }
//    private fun simulateCorrectPathLocation() {
//        // 🚦 A location near Noida Special Economic Zone on correct path
//        val simulatedLocation = LatLng(28.5735, 77.3542) // Sector 82 area
//        checkIfOnPath(simulatedLocation)
//    }
//
//    private fun simulateWrongPathLocation() {
//        // 🚫 A location far from the route - wrong path
//        val simulatedLocation = LatLng(28.5900, 77.4000) // Far off in Sector 62
//        checkIfOnPath(simulatedLocation)
//    }
//    private fun checkIfOnPath(simulatedLocation: LatLng) {
//        val isOnRoute = PolyUtil.isLocationOnPath(simulatedLocation, routePoints, true, 50.0)
//
//        polyline?.color = if (isOnRoute) Color.BLUE else Color.RED
//
//        val mainActivity = requireActivity() as MainActivity
//
//        if (isOnRoute) {
//            mainActivity.setImgseetimer().visibility = View.GONE
//            mainActivity.setHeading().text = "3 min (3 miles) to reach"
//            mainActivity.setTitle().text = "Kudoos!!!"
//            mainActivity.setSubTitle().text = "You are on Right path. Go on with same path."
//        } else {
//            mainActivity.setImgseetimer().visibility = View.VISIBLE
//            mainActivity.setHeading().text = "5 min (4 miles) away"
//            mainActivity.setTitle().text = "Oooopss!!!"
//            mainActivity.setSubTitle().text = "Seems like you have distracted from the actual path. Your emergency contact has been notified."
//        }
//
//        // Move camera to that simulated location
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(simulatedLocation, 14f))
//
//        // Marker (optional)
//        mMap.addMarker(
//            MarkerOptions().position(simulatedLocation).title(if (isOnRoute) "Correct Location" else "Wrong Location")
//        )
//    }
//
//    private fun selectPlace(prediction: AutocompletePrediction) {
//        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
//        val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()
//
//        placesClient.fetchPlace(request).addOnSuccessListener { response ->
//            val place = response.place
//            // Update UI with selected place
//            if (binding.edtCurrentAddress.hasFocus()) {
//                binding.edtCurrentAddress.setText(place.address ?: place.name)
//                // Store latLng for current location
//            } else if (binding.edtDestinationAddress.hasFocus()) {
//                binding.edtDestinationAddress.setText(place.address ?: place.name)
//                // Store latLng for destination
//                destinationLatLng = place.latLng
//            }
//        }.addOnFailureListener { exception ->
//            Toast.makeText(context, "Place not found: ${exception.message}", Toast.LENGTH_SHORT).show()
//        }
//    }
//
////    private fun setupPlacesAutoComplete() {
////        val adapter = PlaceAutoSuggestAdapter(requireContext(), android.R.layout.simple_list_item_1)
////        destinationInput.setAdapter(adapter)
////
////        binding.edtCurrentAddress.setOnItemClickListener { _, _, _, _ ->
////            val geocoder = Geocoder(requireContext())
////            val addressList = geocoder.getFromLocationName(destinationInput.text.toString(), 1)
////            if (!addressList.isNullOrEmpty()) {
////                val address = addressList[0]
////                destinationLatLng = LatLng(address.latitude, address.longitude)
////            }
////        }
////    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
//            return
//        }
//        mMap.isMyLocationEnabled = true
//    }
///*
//    private fun drawRouteToDestination() {
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//            location?.let {
//                val origin = LatLng(it.latitude, it.longitude)
//                val dest = destinationLatLng ?: return@addOnSuccessListener
//
//                val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&key=YOUR_API_KEY"
//
//                thread {
//                    val json = URL(url).readText()
//                    val jsonObject = JSONObject(json)
//                    val points = jsonObject.getJSONArray("routes")
//                        .getJSONObject(0).getJSONObject("overview_polyline").getString("points")
//
//                    routePoints = PolyUtil.decode(points)
//
//                    requireActivity().runOnUiThread {
//                        polyline?.remove()
//                        polyline = mMap.addPolyline(
//                            PolylineOptions().addAll(routePoints).color(Color.BLUE).width(10f)
//                        )
//                        startTrackingUser(routePoints)
//                    }
//                }
//            }
//        }
//    }
//
// */
//private fun drawRouteToDestination() {
//    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//        != PackageManager.PERMISSION_GRANTED) {
//        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
//        return
//    }
//
//    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//        location?.let {
//            val origin = LatLng(it.latitude, it.longitude)
//            val dest = destinationLatLng ?: return@addOnSuccessListener
//
//            val apiKey = getString(R.string.api_key) // make sure Directions API is enabled
//            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
//                    "origin=${origin.latitude},${origin.longitude}" +
//                    "&destination=${dest.latitude},${dest.longitude}" +
//                    "&mode=driving" +
//                    "&key=$apiKey"
//
//            thread {
//                val result = URL(url).readText()
//                val jsonObject = JSONObject(result)
//                val routes = jsonObject.getJSONArray("routes")
//
//                if (routes.length() > 0) {
//                    val route = routes.getJSONObject(0)
//                    val legs = route.getJSONArray("legs").getJSONObject(0)
//
//                    val distanceText = legs.getJSONObject("distance").getString("text")
//                    val durationText = legs.getJSONObject("duration").getString("text")
//
//                    val points = route.getJSONObject("overview_polyline").getString("points")
//                    routePoints = PolyUtil.decode(points)
//                    startTrackingUser(routePoints)
//
//                    requireActivity().runOnUiThread {
//                        mMap.clear()
//
//                        // Draw polyline
//                        polyline = mMap.addPolyline(
//                            PolylineOptions().addAll(routePoints).color(Color.BLUE).width(10f)
//                        )
//
//                        // Add pickup and destination markers
//                        mMap.addMarker(com.google.android.gms.maps.model.MarkerOptions().position(origin).title("Pickup Spot"))
//                        mMap.addMarker(com.google.android.gms.maps.model.MarkerOptions().position(dest).title("Destination"))
//
//                        // Animate camera to show the route
//                        mMap.animateCamera(
//                            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(origin, 13f)
//                        )
//
//                        // Update UI
//                        (requireActivity() as MainActivity).setHeading().text = "$durationText ($distanceText) to reach"
//                        (requireActivity() as MainActivity).setTitle().text = "Kudoos!!!"
//                        (requireActivity() as MainActivity).setSubTitle().text = "You are on Right path. Go on with same path."
//                    }
//                }
//            }
//        }
//    }
//}
//
///*
//    private fun startTrackingUser(path: List<LatLng>) {
//     /*   val locationRequest = LocationRequest.create().apply {
//            interval = 3000
//            fastestInterval = 2000
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                val loc = result.lastLocation ?: return
//                val currentLatLng = LatLng(loc.latitude, loc.longitude)
//
//                val isOnRoute = PolyUtil.isLocationOnPath(currentLatLng, path, false, 50.0)
//                polyline?.color = if (isOnRoute) Color.BLUE else Color.RED
//                tvStatus.text = if (isOnRoute) {
//                    "✅ You are on the right path."
//                } else {
//                    "🚨 You’ve deviated. Your emergency contact has been notified!"
//                }
//            }
//        }
//
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())*/
//    }
//
// */
//@SuppressLint("MissingPermission")
//private fun startTrackingUser(path: List<LatLng>) {
//    val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
//        interval = 5000 // 5 seconds
//        fastestInterval = 3000
//        priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
//    }
//
//    locationCallback = object : LocationCallback() {
//        override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
//            val loc = result.lastLocation ?: return
//            val currentLatLng = LatLng(loc.latitude, loc.longitude)
//
//            // 🔍 Check if user is still on path
//            val isOnRoute = PolyUtil.isLocationOnPath(currentLatLng, path, true, 50.0)
//
//            polyline?.color = if (isOnRoute) Color.BLUE else Color.RED
//
//            // 🚨 Show alert if off-path
//            val mainActivity = requireActivity() as MainActivity
//            if (isOnRoute) {
//                mainActivity.setImgseetimer().visibility = View.GONE
//                mainActivity.setHeading().text = "3 min (3 miles) to reach"
//                mainActivity.setTitle().text = "Kudoos!!!"
//                mainActivity.setSubTitle().text = "You are on Right path. Go on with same path."
//            } else {
//                mainActivity.setImgseetimer().visibility = View.VISIBLE
//                mainActivity.setHeading().text = "5 min (4 miles) away"
//                mainActivity.setTitle().text = "Oooopss!!!"
//                mainActivity.setSubTitle().text = "Seems like you have distracted from the actual path. Your emergency contact has been notified."
//                // 🚨 TODO: Notify emergency contact via Firebase or SMS here
//            }
//        }
//    }
//
//    fusedLocationClient.requestLocationUpdates(
//        locationRequest,
//        locationCallback,
//        requireActivity().mainLooper
//    )
//}
//
//
//    private fun changeIcon(type:String){
//        chooseType = type
//        if (type=="Top"){
//            binding.imgTop.setImageResource(R.drawable.color_circle_active)
//            binding.imgBottom.setImageResource(R.drawable.circle_inactive_image)
//            binding.edSearch.hint = getString(R.string.choose_starting_point)
//        }else{
//            binding.imgTop.setImageResource(R.drawable.circle_inactive_image)
//            binding.imgBottom.setImageResource(R.drawable.color_circle_active)
//            binding.edSearch.hint = getString(R.string.choose_end_point)
//        }
//    }
//
//    // Function to start the location picker using Autocomplete
//    private fun startLocationPicker() {
//        val fields = listOf(
//            Place.Field.ID,
//            Place.Field.NAME,
//            Place.Field.LAT_LNG,   // Needed to get coordinates
//            Place.Field.ADDRESS    // Optional, for full address
//        )
//        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
//            .build(requireContext())
////        startAutocomplete.launch(intent)
//    }
///*
//    // For handling the result of the Autocomplete Activity
//    private val startAutocomplete = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                result.data?.let { intent ->
//                    val place = Autocomplete.getPlaceFromIntent(intent)
//                    val name = place.name
//                    val latLng = place.latLng
//                    if (chooseType=="Top"){
//                        binding.tvCurrent.text = name
//                        startLat = latLng
//                    }else{
//                        binding.tvDestination.text = name
//                        endLat = latLng
//                    }
//
//                }
//            } else if (result.resultCode == Activity.RESULT_CANCELED) {
//                Log.i("******", "User canceled autocomplete")
//            }
//        }*/
//
//    @SuppressLint("SetTextI18n")
//    private fun alertBoxTimerShow() {
//        val dialog = Dialog(requireContext())
//        dialog.setContentView(R.layout.dialog_timer)
//        dialog.setCancelable(false)
//        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        val layoutParams = WindowManager.LayoutParams()
//        layoutParams.copyFrom(dialog.window!!.attributes)
//        dialog.window!!.attributes = layoutParams
//        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
//        dialog.show()
//        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
//
//
//        imgClose.setOnClickListener {
//            dialog.dismiss()
//            (requireActivity() as MainActivity).setImgseetimer().visibility=View.GONE
//            (requireActivity() as MainActivity).setHeading().text="3 min (3 miles) to reach"
//            (requireActivity() as MainActivity).setTitle().text="Well Done!!!"
//            (requireActivity() as MainActivity).setSubTitle().text="You are on Right path. Go on with same path."
//        }
//
//
//
//
//
//    }
//
///*    override fun onMapReady(p0: GoogleMap) {
//        googleMap = p0
//        if (::googleMap.isInitialized) {
//            val user1 = User("28.5429026","77.3972842")
//            val user2 = User("28.5425751","77.3976865")
//            val users = listOf(user1, user2) // each has lat, lng, profileImage
//            users.forEach { user ->
//                createMarkerFromView(requireContext(), "") { icon ->
//                    googleMap.addMarker(
//                        MarkerOptions()
//                            .position(LatLng(user.lat.toDouble(), user.lng.toDouble()))
//                            .icon(icon)
//                            .anchor(0.5f, 1f))
//                    // Move camera to current location
//                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(user.lat.toDouble(), user.lng.toDouble())
//                        , 16f))
//                }
//            }
//        }
//    }*/
///*
//    private fun createMarkerFromView(context: Context, imageUrl: String, callback: (BitmapDescriptor) -> Unit) {
//        val markerView = LayoutInflater.from(context).inflate(R.layout.custom_marker, null)
//
//        val profileImage = markerView.findViewById<CircleImageView>(R.id.imgProfile)
//
//        Glide.with(context)
//            .asBitmap()
//            .load(R.drawable.marker_demmy_pic)
//            .placeholder(R.drawable.marker_demmy_pic)
//            .into(object : CustomTarget<Bitmap>() {
//                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    profileImage.setImageBitmap(resource)
//
//                    // Convert the view to bitmap
//                    val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//                    markerView.measure(measureSpec, measureSpec)
//                    markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
//                    val bitmap = Bitmap.createBitmap(
//                        markerView.measuredWidth, markerView.measuredHeight,
//                        Bitmap.Config.ARGB_8888
//                    )
//                    val canvas = Canvas(bitmap)
//                    markerView.draw(canvas)
//
//                    callback(BitmapDescriptorFactory.fromBitmap(bitmap))
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {}
//            })
//    }
//
//    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {
//        val originStr = "origin=${origin.latitude},${origin.longitude}"
//        val destStr = "destination=${dest.latitude},${dest.longitude}"
//        val mode = "mode=driving"
//        val key = getString(R.string.mapapi_key) // 🔑 Make sure it's enabled for Directions API
//        return "https://maps.googleapis.com/maps/api/directions/json?$originStr&$destStr&$mode&key=$key"
//    }
//
//    private fun fetchRoute(origin: LatLng, dest: LatLng, callback: (List<LatLng>) -> Unit) {
//        val url = getDirectionsUrl(origin, dest)
//
//        Log.d("******",url)
//
//        val client = OkHttpClient()
//        val request = Request.Builder().url(url).build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {}
//
//            override fun onResponse(call: Call, response: Response) {
//                val json = response.body?.string()
//                val jsonObject = JSONObject(json)
//                val routes = jsonObject.getJSONArray("routes")
//                if (routes.length() > 0) {
//                    val overviewPolyline = routes.getJSONObject(0)
//                        .getJSONObject("overview_polyline")
//                        .getString("points")
//
//                    val points = PolyUtil.decode(overviewPolyline) // Provided by Maps Utils
//                    callback(points)
//                }
//            }
//        })
//    }
//*/
//
//
//    override fun onPause() {
//        super.onPause()
//        binding.map.onPause()
//    }
//
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding.map.onDestroy()
//    }
//    override fun onResume() {
//        super.onResume()
//        binding.map.onResume()
//    }
//    override fun onLowMemory() {
//        super.onLowMemory()
//        binding.map.onLowMemory()
//    }
//
//}