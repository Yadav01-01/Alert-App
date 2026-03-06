package com.alert.app.activity


import android.annotation.SuppressLint
import android.graphics.*
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alert.app.R
import com.alert.app.base.SessionManagement
import com.alert.app.di.NetworkResult
import com.alert.app.viewmodel.watchovermeviewmodel.WatchOverMeViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URL
import kotlin.math.*
@AndroidEntryPoint
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var viewModel: WatchOverMeViewModel

    private var currentMarker: Marker? = null
    private var pickupMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var currentPolyline: Polyline? = null

    private var journeyId = ""
    private var correctRoutePoints: List<LatLng> = emptyList()
    private var locationUpdateJob: Job? = null

    private val DEVIATION_THRESHOLD_METERS = 30.0

    // Store current position to detect changes
    private var lastPosition: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        journeyId = intent.getStringExtra("journey_id") ?: ""

        if (journeyId.isEmpty()) {
            Toast.makeText(this, "Invalid Journey ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[WatchOverMeViewModel::class.java]

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        fetchJourneyDetails()
    }

    // =====================================================
    // FETCH JOURNEY DATA (ONLY SELECTED JOURNEY)
    // =====================================================

    private fun fetchJourneyDetails() {

        lifecycleScope.launch {
            viewModel.getAllLiveLocation().collect { result ->

                if (result is NetworkResult.Success) {

                    val journey =
                        result.data?.data?.journeys
                            ?.find { it.journeyId.toString() == journeyId }

                    journey?.let {

                        val pickup = LatLng(
                            it.currentPickupLatitude.toDouble(),
                            it.currentPickupLongitude.toDouble()
                        )

                        val destination = LatLng(
                            it.userDestinationLatitude.toDouble(),
                            it.userDestinationLongitude.toDouble()
                        )

                        val current = LatLng(
                            it.userCurrentLatitude.toDouble(),
                            it.userCurrentLongitude.toDouble()
                        )

                        setupMarkers(pickup, destination, current)
                        fetchAndDrawRoute(pickup, destination)
                        startLiveLocationUpdates()
                    }
                }
            }
        }
    }

    // =====================================================
    // 🔴 FIXED: LIVE LOCATION UPDATE
    // =====================================================

    private fun startLiveLocationUpdates() {
        locationUpdateJob?.cancel()

        locationUpdateJob = lifecycleScope.launch {
            while (isActive) {
                try {
                    Log.d("LIVE_LOCATION", "📡 Calling API at ${System.currentTimeMillis()}")

                    viewModel.liveLocation(

                    ).collect { result ->
                        Log.d("LIVE_LOCATION", "✅ liveLocation Result ${result}")

                        when (result) {
                            is NetworkResult.Success -> {
                                // 🔴 FIXED: result.data?.data is a List
                                val journeys = result.data?.data

                                Log.d("LIVE_LOCATION", "✅ Received ${journeys?.size} journeys")

                                // Find our specific journey
                                val journeyData = journeys?.find {
                                    it.journeyId.toString() == journeyId
                                }

                                if (journeyData == null) {
                                    Log.e("LIVE_LOCATION", "❌ Journey $journeyId not found in response")
                                    Log.d("LIVE_LOCATION", "Available IDs: ${journeys?.map { it.journeyId }}")
                                    return@collect
                                }

                                // 🔴 Log all possible location fields
                                Log.d("LIVE_LOCATION", "=== JOURNEY DATA ===")
                                Log.d("LIVE_LOCATION", "journeyId: ${journeyData.journeyId}")
                                Log.d("LIVE_LOCATION", "currentLatitude: ${journeyData.currentLatitude}")
                                Log.d("LIVE_LOCATION", "currentLongitude: ${journeyData.currentLongitude}")
                                Log.d("LIVE_LOCATION", "userPickupLatitude: ${journeyData.currentLatitude}")
                                Log.d("LIVE_LOCATION", "userPickupLongitude: ${journeyData.currentLongitude}")
                                Log.d("LIVE_LOCATION", "destinationLatitude: ${journeyData.destinationLatitude}")
                                Log.d("LIVE_LOCATION", "destinationLongitude: ${journeyData.destinationLongitude}")

                                // Try to get coordinates (priority order)
                                var lat: Double? = null
                                var lng: Double? = null

                                // Priority 1: currentLatitude/currentLongitude (live location)
                                if (journeyData.currentLatitude != null &&
                                    journeyData.currentLongitude != null) {
                                    lat = journeyData.currentLatitude?.toDoubleOrNull()
                                    lng = journeyData.currentLongitude?.toDoubleOrNull()
                                    Log.d("LIVE_LOCATION", "📍 Using current location: $lat, $lng")
                                }

                                // Priority 2: pickup location as fallback
                                if ((lat == null || lng == null) &&
                                    journeyData.currentPickupLatitude != null &&
                                    journeyData.currentPickupLongitude != null) {
                                    lat = journeyData.currentPickupLatitude?.toDoubleOrNull()
                                    lng = journeyData.currentPickupLongitude?.toDoubleOrNull()
                                    Log.d("LIVE_LOCATION", "📦 Using pickup location: $lat, $lng")
                                }

                                if (lat != null && lng != null) {
                                    val newPosition = LatLng(lat, lng)

                                    // Check if position actually changed
                                    if (lastPosition == null ||
                                        distanceMeters(lastPosition!!, newPosition) > 1.0) { // 1 meter threshold

                                        Log.d("LIVE_LOCATION", "🎯 Position changed from $lastPosition to $newPosition")

                                        withContext(Dispatchers.Main) {
                                            updateCurrentMarker(newPosition)
                                            checkDeviation(newPosition)
                                        }

                                        lastPosition = newPosition
                                    } else {
                                        Log.d("LIVE_LOCATION", "⏸️ Position unchanged: $newPosition")
                                    }
                                } else {
                                    Log.e("LIVE_LOCATION", "❌ Could not parse coordinates")
                                }
                            }

                            is NetworkResult.Error -> {
                                Log.e("LIVE_LOCATION", "❌ Error: ${result.message}")
                            }

                        }
                    }

                    Log.d("LIVE_LOCATION", "⏱️ Waiting 5 seconds")
                    delay(5000)

                } catch (e: Exception) {
                    Log.e("LIVE_LOCATION", "💥 Exception: ${e.message}")
                    e.printStackTrace()
                    delay(5000)
                }
            }
        }
    }

    // =====================================================
    // UPDATE MARKER
    // =====================================================

    private fun updateCurrentMarker(newPosition: LatLng) {
        try {
            if (currentMarker == null) {
                currentMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(newPosition)
                        .icon(getCurrentIcon())
                        .anchor(0.5f, 0.5f)
                        .title("Current Location")
                )
                Log.d("LIVE_LOCATION", "✅ New marker created at $newPosition")
            } else {
                currentMarker?.position = newPosition
                Log.d("LIVE_LOCATION", "✅ Marker moved to $newPosition")
            }

            // Animate camera
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(newPosition, 16f),
                500,
                null
            )

        } catch (e: Exception) {
            Log.e("LIVE_LOCATION", "Error updating marker: ${e.message}")
        }
    }

    // =====================================================
    // SETUP MARKERS
    // =====================================================

    private fun setupMarkers(
        pickup: LatLng,
        destination: LatLng,
        current: LatLng
    ) {
        pickupMarker?.remove()
        destinationMarker?.remove()
        currentMarker?.remove()

        pickupMarker = mMap.addMarker(
            MarkerOptions()
                .position(pickup)
                .icon(createCustomMarker("Pickup Spot", "", ""))
                .anchor(0.5f, 1.5f)
                .zIndex(3f)
                .title("Pickup")
        )

        destinationMarker = mMap.addMarker(
            MarkerOptions()
                .position(destination)
                .icon(createCustomMarker("Destination", "", ""))
                .anchor(0.5f, 1.5f)
                .zIndex(3f)
                .title("Destination")
        )

        currentMarker = mMap.addMarker(
            MarkerOptions()
                .position(current)
                .icon(getCurrentIcon())
                .anchor(0.5f, 0.5f)
                .title("Current Location")
        )

        lastPosition = current

        val bounds = LatLngBounds.builder()
            .include(pickup)
            .include(destination)
            .include(current)
            .build()

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
        Log.d("LIVE_LOCATION", "Markers setup complete")
    }

    // =====================================================
    // ROUTE DRAW
    // =====================================================

    private fun fetchAndDrawRoute(origin: LatLng, dest: LatLng) {
        lifecycleScope.launch {
            val route = getRoute(origin, dest)

            if (route.isNotEmpty()) {
                correctRoutePoints = route

                currentPolyline?.remove()

                currentPolyline = mMap.addPolyline(
                    PolylineOptions()
                        .addAll(route)
                        .width(8f)
                        .color(Color.parseColor("#0F87CD"))
                        .geodesic(true)
                )
                Log.d("LIVE_LOCATION", "Route drawn with ${route.size} points")
            }
        }
    }

    // =====================================================
    // HELPER FUNCTIONS
    // =====================================================

    private suspend fun getRoute(
        origin: LatLng,
        dest: LatLng
    ): List<LatLng> = withContext(Dispatchers.IO) {
        try {
            val url =
                "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=${origin.latitude},${origin.longitude}" +
                        "&destination=${dest.latitude},${dest.longitude}" +
                        "&mode=driving" +
                        "&key=${getString(R.string.api_key)}"

            val response = OkHttpClient().newCall(
                Request.Builder().url(url).build()
            ).execute()

            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "")
                val routes = json.getJSONArray("routes")
                if (routes.length() > 0) {
                    val poly =
                        routes.getJSONObject(0)
                            .getJSONObject("overview_polyline")
                            .getString("points")
                    return@withContext decodePolyline(poly)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emptyList()
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }

    private fun checkDeviation(current: LatLng) {
        if (correctRoutePoints.isEmpty()) return

        val minDistance = correctRoutePoints.minOf { distanceMeters(current, it) }

        if (minDistance > DEVIATION_THRESHOLD_METERS) {
            currentMarker?.setIcon(
                BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_ORANGE
                )
            )
            Log.d("LIVE_LOCATION", "⚠️ Deviation detected: $minDistance meters")
        } else {
            currentMarker?.setIcon(getCurrentIcon())
            Log.d("LIVE_LOCATION", "✅ On route: $minDistance meters")
        }
    }

    private fun distanceMeters(p1: LatLng, p2: LatLng): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(p1.latitude)) *
                cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun getCurrentIcon(): BitmapDescriptor {
        val bmp = BitmapFactory.decodeResource(resources, R.drawable.ic_curretpathicon)
        return BitmapDescriptorFactory.fromBitmap(
            Bitmap.createScaledBitmap(bmp, 90, 90, false)
        )
    }

    private fun createCustomMarker(title: String, time: String = "", distance: String = ""): BitmapDescriptor? {
        return try {
            val markerView = LayoutInflater.from(this).inflate(R.layout.layout_marker_info, null)

            val tvTimeView = markerView.findViewById<TextView>(R.id.tvTime)
            val tvDistanceView = markerView.findViewById<TextView>(R.id.tvDistance)
            val tvTitleView = markerView.findViewById<TextView>(R.id.tvTitle)

            tvTimeView?.visibility = View.GONE
            tvDistanceView?.text = distance
            tvTitleView?.text = title

            tvDistanceView?.setTextColor(Color.WHITE)
            tvTitleView?.setTextColor(Color.BLACK)

            markerView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

            val bitmap = Bitmap.createBitmap(
                markerView.measuredWidth,
                markerView.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            markerView.draw(canvas)

            BitmapDescriptorFactory.fromBitmap(bitmap)
        } catch (e: Exception) {
            Log.e("MarkerError", "Error creating marker: ${e.message}")
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationUpdateJob?.cancel()
        locationUpdateJob = null
        Log.d("LIVE_LOCATION", "🛑 Location updates stopped")
    }

    override fun onResume() {
        super.onResume()
        if (::mMap.isInitialized && journeyId.isNotEmpty()) {
            startLiveLocationUpdates()
        }
    }
}


/*package com.alert.app.activity


import android.annotation.SuppressLint
import android.graphics.*
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alert.app.R
import com.alert.app.base.SessionManagement
import com.alert.app.di.NetworkResult
import com.alert.app.viewmodel.watchovermeviewmodel.WatchOverMeViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URL
import kotlin.math.*

@AndroidEntryPoint
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var viewModel: WatchOverMeViewModel

    private var currentMarker: Marker? = null
    private var pickupMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var currentPolyline: Polyline? = null

    private var journeyId = ""
    private var correctRoutePoints: List<LatLng> = emptyList()

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    private val DEVIATION_THRESHOLD_METERS = 30.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        journeyId = intent.getStringExtra("journey_id") ?: ""

        if (journeyId.isEmpty()) {
            Toast.makeText(this, "Invalid Journey ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[WatchOverMeViewModel::class.java]

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        fetchJourneyDetails()
    }

    // =====================================================
    // FETCH JOURNEY DATA (ONLY SELECTED JOURNEY)
    // =====================================================

    private fun fetchJourneyDetails() {

        lifecycleScope.launch {
            viewModel.getAllLiveLocation().collect { result ->

                if (result is NetworkResult.Success) {

                    val journey =
                        result.data?.data?.journeys
                            ?.find { it.journeyId.toString() == journeyId }

                    journey?.let {

                        val pickup = LatLng(
                            it.currentPickupLatitude.toDouble(),
                            it.currentPickupLongitude.toDouble()
                        )

                        val destination = LatLng(
                            it.userDestinationLatitude.toDouble(),
                            it.userDestinationLongitude.toDouble()
                        )

                        val current = LatLng(
                            it.userCurrentLatitude.toDouble(),
                            it.userCurrentLongitude.toDouble()
                        )

                        setupMarkers(pickup, destination, current)
                        fetchAndDrawRoute(pickup, destination)
                        startLiveLocationUpdates()
                    }
                }
            }
        }
    }

    // =====================================================
    // LIVE LOCATION UPDATE (ONLY JOURNEY BASED)
    // =====================================================

    *//*private fun startLiveLocationUpdates() {

        runnable = object : Runnable {
            override fun run() {

                lifecycleScope.launch {

                    viewModel.liveLocation(
                        SessionManagement(this@MapActivity).getUserId().toString()
                    ).collect { result ->

                        if (result is NetworkResult.Success) {

                            val locationData =
                                result.data?.data
                                    ?.find { it.journeyId.toString() == journeyId }

                            locationData?.let { data ->

                                val lat = data.currentLatitude?.toDoubleOrNull()
                                val lng = data.currentLongitude?.toDoubleOrNull()

                                if (lat != null && lng != null) {

                                    val newPosition = LatLng(lat, lng)

                                    updateCurrentMarker(newPosition)
                                    checkDeviation(newPosition)
                                }
                            }
                        }
                    }
                }

                handler.postDelayed(this, 5000)
            }
        }

        runnable?.let { handler.post(it) }
    }*//*

    *//*private fun startLiveLocationUpdates() {

        lifecycleScope.launch {

            while (isActive) {

                viewModel.liveLocation(
                    SessionManagement(this@MapActivity).getUserId().toString()
                ).collectLatest { result ->

                    if (result is NetworkResult.Success) {

                        val locationData =
                            result.data?.data
                                ?.find { it.journeyId.toString() == journeyId }

                        locationData?.let { data ->

                            val lat = if (data.currentLatitude != null) data.currentLatitude?.toDoubleOrNull() else data.currentPickupLatitude?.toDoubleOrNull()
                            val lng = if (data.currentLongitude != null) data.currentLongitude?.toDoubleOrNull()  else data.currentPickupLongitude?.toDoubleOrNull()

                            if (lat != null && lng != null) {

                                val newPosition = LatLng(lat, lng)

                                updateCurrentMarker(newPosition)
                                checkDeviation(newPosition)

                                Log.d("LIVE_LOCATION", "Marker updated: $newPosition")
                            }
                        }
                    }
                }

                delay(5000)
            }
        }
    }*//*

    private fun startLiveLocationUpdates() {
        lifecycleScope.launch {
            while (isActive) {
                viewModel.liveLocation(
                    SessionManagement(this@MapActivity).getUserId().toString()
                ).collectLatest { result ->
                    if (result is NetworkResult.Success) {
                        val locationData = result.data?.data
                            ?.find { it.journeyId.toString() == journeyId }

                        locationData?.let { data ->
                            // Log the received data for debugging
                            Log.d("LIVE_LOCATION", "Received data: $data")

                            // Always use current_latitude and current_longitude if available
                            val lat = data.currentLatitude?.toDoubleOrNull()
                            val lng = data.currentLongitude?.toDoubleOrNull()

                            if (lat != null && lng != null) {
                                val newPosition = LatLng(lat, lng)

                                // Update the current marker position
                                updateCurrentMarker(newPosition)

                                // Check if user is off route
                                checkDeviation(newPosition)

                                Log.d("LIVE_LOCATION", "Marker updated to: lat=$lat, lng=$lng")
                            } else {
                                Log.e("LIVE_LOCATION", "Invalid coordinates: lat=$lat, lng=$lng")
                            }
                        }
                    }
                }

                delay(5000) // Update every 5 seconds
            }
        }
    }

    // =====================================================
    // UPDATE MARKER SMOOTHLY
    // =====================================================

    private fun updateCurrentMarker(newPosition: LatLng) {

        if (currentMarker == null) {

            currentMarker = mMap.addMarker(
                MarkerOptions()
                    .position(newPosition)
                    .icon(getCurrentIcon())
                    .anchor(0.5f, 0.5f)
            )

        } else {

            currentMarker?.position = newPosition
        }

        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(newPosition, 16f)
        )
    }

    // =====================================================
    // SETUP MARKERS
    // =====================================================

    private fun setupMarkers(
        pickup: LatLng,
        destination: LatLng,
        current: LatLng
    ) {

      *//*  pickupMarker = mMap.addMarker(
            MarkerOptions()
                .position(pickup)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )  *//*
        pickupMarker = mMap.addMarker(
            MarkerOptions()
                .position(pickup)
                .icon(createCustomMarker("Pickup Spot", "", ""))
                .anchor(0.5f, 1.5f)
                .zIndex(3f)
        )
        pickupMarker?.tag = "PICKUP|Not started|0.0 mi"

    *//*    destinationMarker = mMap.addMarker(
            MarkerOptions()
                .position(destination)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )  *//*
        destinationMarker = mMap.addMarker(
            MarkerOptions()
                .position(destination)
                .icon(createCustomMarker("Destination", "", ""))
                .anchor(0.5f, 1.5f)
                .zIndex(3f)
        )
        destinationMarker?.tag = "DEST|-- min|-- mi"

        currentMarker = mMap.addMarker(
            MarkerOptions()
                .position(current)
                .icon(getCurrentIcon())
        )

        val bounds = LatLngBounds.builder()
            .include(pickup)
            .include(destination)
            .include(current)
            .build()

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
    }

    // =====================================================
    // ROUTE DRAW
    // =====================================================

    private fun fetchAndDrawRoute(origin: LatLng, dest: LatLng) {

        lifecycleScope.launch {

            val route = getRoute(origin, dest)

            if (route.isNotEmpty()) {

                correctRoutePoints = route

                currentPolyline?.remove()

                currentPolyline = mMap.addPolyline(
                    PolylineOptions()
                        .addAll(route)
                        .width(8f)
                        .color(Color.parseColor("#0F87CD"))
                )
            }
        }
    }

    // =====================================================
    // ROUTE DEVIATION CHECK
    // =====================================================
    private suspend fun getRoute(
        origin: LatLng,
        dest: LatLng
    ): List<LatLng> = withContext(Dispatchers.IO) {

        try {
            val url =
                "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=${origin.latitude},${origin.longitude}" +
                        "&destination=${dest.latitude},${dest.longitude}" +
                        "&mode=driving" +
                        "&key=${getString(R.string.api_key)}"

            val response = OkHttpClient().newCall(
                Request.Builder().url(url).build()
            ).execute()

            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "")
                val routes = json.getJSONArray("routes")
                if (routes.length() > 0) {
                    val poly =
                        routes.getJSONObject(0)
                            .getJSONObject("overview_polyline")
                            .getString("points")
                    return@withContext decodePolyline(poly)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emptyList()
    }

        private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }
    private fun checkDeviation(current: LatLng) {

        if (correctRoutePoints.isEmpty()) return

        val minDistance =
            correctRoutePoints.minOf { distanceMeters(current, it) }

        if (minDistance > DEVIATION_THRESHOLD_METERS) {

            currentMarker?.setIcon(
                BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_ORANGE
                )
            )

        } else {

            currentMarker?.setIcon(getCurrentIcon())
        }
    }

    private fun distanceMeters(p1: LatLng, p2: LatLng): Double {

        val R = 6371000.0

        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(p1.latitude)) *
                cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    private fun getCurrentIcon(): BitmapDescriptor {

        val bmp =
            BitmapFactory.decodeResource(resources, R.drawable.ic_curretpathicon)

        return BitmapDescriptorFactory.fromBitmap(
            Bitmap.createScaledBitmap(bmp, 90, 90, false)
        )
    }

    @SuppressLint("MissingInflatedId")
    private fun createCustomMarker(title: String, time: String = "", distance: String = ""): BitmapDescriptor? {

        return try {
            val markerView =
                LayoutInflater.from(this).inflate(R.layout.layout_marker_info, null)

            val llBlue = markerView.findViewById<LinearLayout>(R.id.llBlue)
            val tvTimeView = markerView.findViewById<TextView>(R.id.tvTime)
            val tvDistanceView = markerView.findViewById<TextView>(R.id.tvDistance)
            val tvTitleView = markerView.findViewById<TextView>(R.id.tvTitle)

            Log.d("MarkerDebug", "tvTime found: ${tvTimeView != null}")
            Log.d("MarkerDebug", "tvDistance found: ${tvDistanceView != null}")
            Log.d("MarkerDebug", "tvTitle found: ${tvTitleView != null}")
            llBlue.visibility = View.VISIBLE
            tvTimeView.visibility = View.GONE
            llBlue.visibility = View.GONE
            tvDistanceView?.text = time
            tvDistanceView?.text = distance
            tvTitleView?.text = title

            tvTimeView?.setTextColor(Color.WHITE)
            tvDistanceView?.setTextColor(Color.WHITE)
            tvTitleView?.setTextColor(Color.BLACK)

            markerView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

            val bitmap = Bitmap.createBitmap(
                markerView.measuredWidth,
                markerView.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            markerView.draw(canvas)

            return BitmapDescriptorFactory.fromBitmap(bitmap)

        } catch (e: Exception) {
            Log.e("MarkerError", "Error creating marker: ${e.message}")
            null
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        runnable?.let { handler.removeCallbacks(it) }
    }
}*/
