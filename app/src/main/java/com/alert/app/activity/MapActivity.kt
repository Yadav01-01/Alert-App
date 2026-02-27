package com.alert.app.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alert.app.DirectionsApiService
import com.alert.app.DirectionsResponse
import com.alert.app.R
import com.alert.app.di.NetworkResult
import com.alert.app.viewmodel.watchovermeviewmodel.WatchOverMeViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var viewModel: WatchOverMeViewModel
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var currentMarker: Marker? = null
    private var pickupMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var currentPolyline: Polyline? = null

    private var currentLat = 0.0
    private var currentLng = 0.0
    private var pickupLat = 0.0
    private var pickupLng = 0.0
    private var destLat = 0.0
    private var destLng = 0.0
    private var journeyId = 0
    private var routeDrawn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Get journeyId from intent
        journeyId = intent.getStringExtra("journey_id")?.toIntOrNull() ?: 0

        if (journeyId == 0) {
            Toast.makeText(this, "Invalid Journey ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("MapActivity", "Journey ID: $journeyId")

        viewModel = ViewModelProvider(this)[WatchOverMeViewModel::class.java]
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Fetch journey details
        fetchJourneyDetails()
    }

    private fun fetchJourneyDetails() {
        lifecycleScope.launch {
            viewModel.getAllLiveLocation().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val journeys = result.data?.data?.journeys
                        if (!journeys.isNullOrEmpty()) {
                            // Find the specific journey by ID
                            val journey = journeys.find { it.journeyId == journeyId }

                            journey?.let {
                                pickupLat = it.currentPickupLatitude.toDouble()
                                pickupLng = it.currentPickupLongitude.toDouble()
                                currentLat = it.userCurrentLatitude.toDouble()
                                currentLng = it.userCurrentLongitude.toDouble()
                                destLat = it.userDestinationLatitude.toDouble()
                                destLng = it.userDestinationLongitude.toDouble()

                                runOnUiThread {
                                    setupMapWithLocations()
                                    startLiveLocationUpdates()
                                }
                            } ?: run {
                                Toast.makeText(this@MapActivity, "Journey not found", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    }
                    is NetworkResult.Error -> {
                        Toast.makeText(this@MapActivity, result.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupMapWithLocations() {
        val pickup = LatLng(pickupLat, pickupLng)
        val destination = LatLng(destLat, destLng)
        val current = LatLng(currentLat, currentLng)

        // Add markers
        pickupMarker = mMap.addMarker(
            MarkerOptions()
                .position(pickup)
                .title("Pickup Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )

        destinationMarker = mMap.addMarker(
            MarkerOptions()
                .position(destination)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        currentMarker = mMap.addMarker(
            MarkerOptions()
                .position(current)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        // Zoom to show entire route
        val boundsBuilder = LatLngBounds.builder()
        boundsBuilder.include(pickup)
        boundsBuilder.include(destination)
        boundsBuilder.include(current)

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))

        // Draw route from pickup to destination
        fetchAndDrawRoute(pickup, destination)
    }

    private fun fetchAndDrawRoute(origin: LatLng, dest: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&key=YOUR_GOOGLE_MAPS_API_KEY" // Replace with your API key

        val request = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DirectionsApiService::class.java)
            .getDirections(url)

        request.enqueue(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                if (response.isSuccessful) {
                    val points = response.body()?.routes?.firstOrNull()?.overviewPolyline?.points
                    points?.let {
                        val decodedPath = PolyUtil.decode(it)

                        // Remove old polyline if exists
                        currentPolyline?.remove()

                        // Draw new route
                        currentPolyline = mMap.addPolyline(
                            PolylineOptions()
                                .addAll(decodedPath)
                                .color(0xFF2196F3.toInt()) // Blue color
                                .width(8f)
                        )

                        routeDrawn = true
                    }
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Log.e("MapActivity", "Route fetch failed", t)
                Toast.makeText(this@MapActivity, "Failed to load route", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startLiveLocationUpdates() {
        runnable = object : Runnable {
            override fun run() {
                lifecycleScope.launch {
                    viewModel.liveLocation().collect { result ->
                        when (result) {
                            is NetworkResult.Success -> {
                                val locationList = result.data?.data

                                locationList?.let { list ->
                                    // Find the location data for our journey
                                    val locationData = list.find { it.journeyId == journeyId }

                                    locationData?.let { data ->
                                        val lat = data.currentLatitude?.toDoubleOrNull()
                                        val lng = data.currentLongitude?.toDoubleOrNull()

                                        if (lat != null && lng != null) {
                                            val newPos = LatLng(lat, lng)

                                            runOnUiThread {
                                                // Update current location marker
                                                if (currentMarker == null) {
                                                    currentMarker = mMap.addMarker(
                                                        MarkerOptions()
                                                            .position(newPos)
                                                            .title("Current Location")
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                                    )
                                                } else {
                                                    currentMarker?.position = newPos
                                                }

                                                // Animate camera to follow user
                                                mMap.animateCamera(
                                                    CameraUpdateFactory.newLatLngZoom(newPos, 16f)
                                                )

                                                // Optional: Show route status if available
                                                data.routeStatus?.let { status ->
                                                    Log.d("MapActivity", "Route Status: $status")
                                                    // You can show this status in a TextView if needed
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            is NetworkResult.Error -> {
                                Log.e("MapActivity", "Live location error: ${result.message}")
                            }
                            else -> {}
                        }
                    }
                }
                handler.postDelayed(this, 5000) // Update every 5 seconds
            }
        }
        handler.post(runnable)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable zoom controls
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}

/*
@AndroidEntryPoint
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var viewModel: WatchOverMeViewModel
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var currentMarker: Marker? = null
    private var pickupMarker: Marker? = null
    private var destinationMarker: Marker? = null

    private var currentLat = 0.0
    private var currentLng = 0.0
    private var destLat = 0.0
    private var destLng = 0.0
    private var journeyId = 0
    private val token = "your_api_token_here"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val journeyId = intent.getStringExtra("journey_id")

        if (!journeyId.isNullOrEmpty()) {
            Log.d("MapActivity", "Journey ID: $journeyId")

            // Yaha se API call karo ya journey start logic likho
        }
        viewModel = ViewModelProvider(this)[WatchOverMeViewModel::class.java]
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



       // apiService = retrofit.create(ApiService::class.java)

        fetchInitialJourney()
    }

    private fun fetchInitialJourney() {
        lifecycleScope.launch {
            viewModel.getAllLiveLocation().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val journeys = result.data?.data?.journeys
                        if (!journeys.isNullOrEmpty()) {
                            // You might want to select specific journey
                            // For now taking first active journey
                            val journey = journeys.firstOrNull()
                            journey?.let {
                                currentLat = it.userCurrentLatitude.toDouble()
                                currentLng = it.userCurrentLongitude.toDouble()
                                destLat = it.userDestinationLatitude.toDouble()
                                destLng = it.userDestinationLongitude.toDouble()
                                journeyId = it.journeyId

                                runOnUiThread {
                                    setupMapWithPickupAndDestination()
                                    startLiveLocationUpdates()
                                }
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        // Handle error
                        Toast.makeText(this@MapActivity, result.message, Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }
    private fun setupMapWithPickupAndDestination() {
        val pickup = LatLng(currentLat, currentLng)
        val destination = LatLng(destLat, destLng)

        pickupMarker = mMap.addMarker(MarkerOptions().position(pickup).title("Pickup Location"))
        destinationMarker = mMap.addMarker(MarkerOptions().position(destination).title("Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 12f))

        fetchAndDrawRoute(pickup, destination)
    }

    private fun fetchAndDrawRoute(origin: LatLng, dest: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&key=YOUR_GOOGLE_MAPS_API_KEY"

        val request = retrofit2.Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DirectionsApiService::class.java)
            .getDirections(url)

        request.enqueue(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                if (response.isSuccessful) {
                    val points = response.body()?.routes?.firstOrNull()?.overviewPolyline?.points
                    points?.let {
                        val decodedPath = PolyUtil.decode(it)
                        mMap.addPolyline(PolylineOptions().addAll(decodedPath).color(0xFF0000FF.toInt()).width(8f))
                    }
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun startLiveLocationUpdates() {

        runnable = object : Runnable {
            override fun run() {

                lifecycleScope.launch {

                    viewModel.liveLocation().collect { result ->

                        when (result) {

                            is NetworkResult.Success -> {

                                val locationList = result.data?.data

                                locationList?.let { list ->

                                    // Find correct journey
                                    val locationData = list.find {
                                        it.journeyId == journeyId
                                    }

                                    locationData?.let { data ->

                                        val lat = data.currentLatitude?.toDoubleOrNull()
                                        val lng = data.currentLongitude?.toDoubleOrNull()

                                        if (lat != null && lng != null) {

                                            val newPos = LatLng(lat, lng)

                                            if (currentMarker == null) {
                                                currentMarker = mMap.addMarker(
                                                    MarkerOptions()
                                                        .position(newPos)
                                                        .title("Current Location")
                                                        .icon(
                                                            BitmapDescriptorFactory
                                                                .defaultMarker(
                                                                    BitmapDescriptorFactory.HUE_RED
                                                                )
                                                        )
                                                )
                                            } else {
                                                currentMarker?.position = newPos
                                            }

                                            mMap.animateCamera(
                                                CameraUpdateFactory
                                                    .newLatLngZoom(newPos, 16f)
                                            )
                                        }
                                    }
                                }
                            }

                            is NetworkResult.Error -> {
                                // Optional error handling
                            }
                        }
                    }
                }

                handler.postDelayed(this, 5000)
            }
        }

        handler.post(runnable)
    }

    */
/*  private fun startLiveLocationUpdates() {
        runnable = object : Runnable {
            override fun run() {
               *//*

    */
/* apiService.getLiveLocation(token).enqueue(object : Callback<LiveLocationResponse> {
                    override fun onResponse(call: Call<LiveLocationResponse>, response: Response<LiveLocationResponse>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data
                            data?.let {
                                val newLat = it.current_latitude.toDouble()
                                val newLng = it.current_longitude.toDouble()
                                val newPos = LatLng(newLat, newLng)

                                runOnUiThread {
                                    if (currentMarker == null) {
                                        currentMarker = mMap.addMarker(MarkerOptions().position(newPos).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                                    } else {
                                        currentMarker?.position = newPos
                                    }
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPos, 14f))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<LiveLocationResponse>, t: Throwable) {
                        t.printStackTrace()
                    }
                })*//*
*/
/*
                lifecycleScope.launch {
                    viewModel.liveLocation().collect { result ->
                        when (result) {
                            is NetworkResult.Success -> {
                                val locationData = result.data?.data
                                locationData?.let {
                                    // Check if this update is for our journey
                                    if (it.journeyId == journeyId) {
                                        val newLat = it.currentLatitude.toDouble()
                                        val newLng = it.currentLongitude.toDouble()
                                        val newPos = LatLng(newLat, newLng)

                                        runOnUiThread {
                                            if (currentMarker == null) {
                                                currentMarker = mMap.addMarker(
                                                    MarkerOptions()
                                                        .position(newPos)
                                                        .title("Current Location")
                                                        .icon(
                                                            BitmapDescriptorFactory.defaultMarker(
                                                                BitmapDescriptorFactory.HUE_RED
                                                            )
                                                        )
                                                )
                                            } else {
                                                currentMarker?.position = newPos
                                            }
                                            mMap.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    newPos,
                                                    16f
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            is NetworkResult.Error -> {
                                // Handle error silently or show toast
                            }

                        }
                handler.postDelayed(this@MapActivity, 5000)
            }
        }
        handler.post(runnable)
    }
        }}*//*


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}*/
