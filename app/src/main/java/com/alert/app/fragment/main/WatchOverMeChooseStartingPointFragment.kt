package com.alert.app.fragment.main


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import org.json.JSONObject
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.alert.app.LocationUpdateWorker
import com.alert.app.adapter.CustomInfoWindowAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.base.SessionManagement
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.checkhistory.CheckInHistoryAlertResponse
import com.alert.app.model.watchoverme.JourneyStarted
import com.alert.app.viewmodel.checkhistory.CheckHistoryViewModel
import com.alert.app.viewmodel.watchovermeviewmodel.WatchOverMeViewModel
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.android.libraries.places.api.model.Place
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Arrays
import java.util.concurrent.TimeUnit
/*

@AndroidEntryPoint
class WatchOverMeChooseStartingPointFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentWatchovermechoosestartingpointBinding
    private lateinit var viewModel: WatchOverMeViewModel
    private lateinit var googleMap: GoogleMap
    private var shouldFollowRider = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var placesClient: PlacesClient
    private lateinit var currentLocationAdapter: PlaceAutoSuggestAdapter
    private lateinit var destinationAdapter: PlaceAutoSuggestAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var simulationRunnable: Runnable? = null

    private var riderMarker: Marker? = null
    private var pickupMarker: Marker? = null
    private var destinationMarker: Marker? = null

    // Selected places
    private var pickupLocation: LatLng? = null
    private var destinationLocation: LatLng? = null

    // Autocomplete fragments
    private lateinit var destinationAutocompleteFragment: AutocompleteSupportFragment

    private var correctRefPolyline: Polyline? = null
    private val trailSegments = mutableListOf<Polyline>()
    private var correctRoutePoints: List<LatLng> = listOf()
    private val actualPathPoints = mutableListOf<LatLng>()

    private enum class RouteChoice { NONE, CORRECT }
    private var routeChoice = RouteChoice.NONE
    private var isDelivering = false
    private var currentStepIndex = 0

    private var deliveryStartTime = 0L
    private var wrongRouteStartTime = 0L
    private var totalDistanceMiles = 0.0
    private var wrongDistanceMiles = 0.0

    private var currentSegmentIsCorrect = true
    private val currentSegmentPoints = mutableListOf<LatLng>()
    private var riderOnCorrectRoute = true

    private var deviationStartPoint: LatLng? = null
    private var wrongPathGrowingPolyline: Polyline? = null
    private val currentWrongSegmentPoints = mutableListOf<LatLng>()

    // 🔴 IMPORTANT: 30 meters threshold (0.01864 miles = ~30 meters)
    private val DEVIATION_THRESHOLD_MILES = 30.0 / 1609.34  // ~0.01864 miles (30 meters)
    private val DESTINATION_THRESHOLD_MILES = 5.0 / 1609.34  // ~0.003107 miles

    // ─── Handlers ────────────────────────────────────────────────────
    private val mainHandler = Handler(Looper.getMainLooper())
    private var uiRunnable: Runnable? = null
    private val UI_REFRESH_MS = 500L
    private val LOC_PERM_REQUEST = 100

    // 🔴 NEW: Properties for continuous location updates
    private var currentJourneyId: String = ""
    private var continuousLocationUpdateJob: Job? = null
    private var isUpdatingLocation = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWatchovermechoosestartingpointBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this)[WatchOverMeViewModel::class.java]

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.api_key))
        }
        placesClient = Places.createClient(requireContext())

        currentLocationAdapter = PlaceAutoSuggestAdapter(requireContext(), placesClient)
        destinationAdapter = PlaceAutoSuggestAdapter(requireContext(), placesClient)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupPlaceAutocomplete()
        setupButtons()
        setCurrentLocationInPickup()
        binding.map.onCreate(savedInstanceState)
        binding.map.getMapAsync(this)

        return binding.root
    }

    private fun setCurrentLocationInPickup() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(requireActivity(), Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val addressText = addresses[0].getAddressLine(0)
                            pickupLocation = LatLng(location.latitude, location.longitude)
                            binding.etPickup.setText(addressText)
                            if (::googleMap.isInitialized) updateMapForSelectedPlaces()
                            Toast.makeText(requireActivity(), "Current location set as pickup", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        pickupLocation = LatLng(location.latitude, location.longitude)
                        binding.etPickup.setText("${location.latitude}, ${location.longitude}")
                        if (::googleMap.isInitialized) updateMapForSelectedPlaces()
                    }
                } else {
                    Toast.makeText(requireActivity(), "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOC_PERM_REQUEST)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupPlaceAutocomplete() {
        destinationAutocompleteFragment = AutocompleteSupportFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.destination_autocomplete_container, destinationAutocompleteFragment)
            .commitNow()

        destinationAutocompleteFragment.setPlaceFields(
            Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
            )
        )
        destinationAutocompleteFragment.setHint("Search destination")
        destinationAutocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                destinationLocation = place.latLng
                updateMapForSelectedPlaces()
            }
            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(requireActivity(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupButtons() {
        binding.btnExplore.setOnClickListener {
            if (pickupLocation == null) {
                Toast.makeText(requireActivity(), "Please select pickup location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (destinationLocation == null) {
                Toast.makeText(requireActivity(), "Please select destination", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startJourney( pickupLocation!!.latitude.toString(),
                pickupLocation!!.longitude.toString(),
                destinationLocation!!.latitude.toString(),
                destinationLocation!!.longitude.toString())
            setupRoutesAndStart()
            startDelivery()
        }
    }

    private fun setupRoutesAndStart() {
        if (pickupLocation != null && destinationLocation != null) {
            binding.progressBar?.visibility = View.VISIBLE
            getStreetRoute(pickupLocation!!, destinationLocation!!)
            binding.btnExplore.visibility = View.GONE
        }
    }

    private fun getStreetRoute(start: LatLng, end: LatLng) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val routePoints = getRouteFromDirectionsAPI(start, end)
                withContext(Dispatchers.Main) {
                    binding.progressBar?.visibility = View.GONE
                    if (routePoints.isNotEmpty()) {
                        correctRoutePoints = routePoints
                        drawCorrectReferenceLine()
                        placeMarkers()
                        fitCamera()
                        Toast.makeText(requireActivity(), "Street-accurate route loaded!", Toast.LENGTH_SHORT).show()
                    } else {
                        generateGridRoute(start, end)
                        Toast.makeText(requireActivity(), "Using grid-based route", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar?.visibility = View.GONE
                    generateGridRoute(start, end)
                    Toast.makeText(requireActivity(), "Using grid-based route: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getRouteFromDirectionsAPI(origin: LatLng, destination: LatLng): List<LatLng> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=${origin.latitude},${origin.longitude}" +
                        "&destination=${destination.latitude},${destination.longitude}" +
                        "&mode=driving" +
                        "&key=${getString(R.string.api_key)}" +
                        "&alternatives=false"

                val client = OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "")
                    val routes = json.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        val encodedPolyline = route.getJSONObject("overview_polyline").getString("points")
                        return@withContext decodePolyline(encodedPolyline)
                    }
                }
                emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int; var shift = 0; var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0; result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(LatLng(lat.toDouble() / 1e5, lng.toDouble() / 1e5))
        }
        return poly
    }

    private fun generateGridRoute(start: LatLng, end: LatLng) {
        val points = mutableListOf<LatLng>()
        points.add(start)
        val latDiff = end.latitude - start.latitude
        val lngDiff = end.longitude - start.longitude
        val steps = 20
        for (i in 1 until steps) {
            val fraction = i.toDouble() / steps
            val offset = Math.sin(fraction * Math.PI * 4) * 0.002
            val lat = start.latitude + (latDiff * fraction) + (if (i % 4 == 0) offset else 0.0)
            val lng = start.longitude + (lngDiff * fraction) + (if (i % 4 == 2) offset else 0.0)
            points.add(LatLng(lat, lng))
        }
        points.add(end)
        correctRoutePoints = points
        drawCorrectReferenceLine()
        placeMarkers()
        fitCamera()
    }

    private fun drawCorrectReferenceLine() {
        correctRefPolyline?.remove()
        if (correctRoutePoints.isNotEmpty()) {
            correctRefPolyline = googleMap.addPolyline(
                PolylineOptions()
                    .addAll(correctRoutePoints)
                    .color(Color.parseColor("#2196F3"))
                    .width(8f)
                    .zIndex(1f)
                    .geodesic(false)
                    .clickable(true)
            )
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = false
            mapType = GoogleMap.MAP_TYPE_NORMAL
        }
        checkLocationPermission()

        // 🔴 Disable default my location icon
        try {
            googleMap.setMyLocationEnabled(false)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        if (pickupLocation != null) updateMapForSelectedPlaces()
    }

    private fun updateMapForSelectedPlaces() {
        if (pickupLocation != null && destinationLocation != null) {
            getStreetRoute(pickupLocation!!, destinationLocation!!)
            placeMarkers()
            fitCamera()
        } else if (pickupLocation != null) {
            pickupMarker?.remove()
            pickupMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(pickupLocation!!)
                    .icon(createCustomMarker("Pickup Spot", "0 Min", "0 M"))
                    .anchor(0.5f, 1.5f)
                    .zIndex(3f)
            )
            pickupMarker?.tag = "PICKUP|Not started|0.0 mi"
        } else if (destinationLocation != null) {
            destinationMarker?.remove()
            destinationMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(destinationLocation!!)
                    .icon(createCustomMarker("Destination", "3 Min", "900 M"))
                    .anchor(0.5f, 1.5f)
                    .zIndex(3f)
            )
            destinationMarker?.tag = "DEST|-- min|-- mi"
        }
    }

    private fun updateDestinationMarker(etaMin: Int, distanceStr: String) {
        if (destinationMarker == null || destinationLocation == null || !isAdded) return

        val displayDistance = when {
            distanceStr.contains("mi") -> {
                val miles = distanceStr.replace(" mi", "").toDoubleOrNull() ?: 0.0
                if (miles < 0.1) {
                    "${(miles * 1609.34).toInt()} M"
                } else {
                    "${String.format("%.1f", miles)} Mi"
                }
            }
            else -> distanceStr
        }

        val newIcon = createCustomMarker("Destination", "$etaMin Min", displayDistance)
        if (newIcon != null) {
            destinationMarker?.setIcon(newIcon)
            destinationMarker?.tag = "DEST|$etaMin min|$displayDistance"
        }
    }

    private fun placeMarkers() {
        if (pickupLocation != null) {
            pickupMarker?.remove()

            val (initialDistance, initialTime) = if (destinationLocation != null) {
                val dist = distanceMiles(pickupLocation!!, destinationLocation!!)
                val time = (dist / 15 * 60).toInt()
                Pair(formatMiles(dist), "$time Min")
            } else {
                Pair("0 M", "0 Min")
            }

            pickupMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(pickupLocation!!)
                    .icon(createCustomMarker("Pickup Spot", initialTime, initialDistance))
                    .anchor(0.5f, 1.5f)
                    .zIndex(3f)
            )
            pickupMarker?.tag = "PICKUP|Not started|0.0 mi"
        }

        if (destinationLocation != null) {
            destinationMarker?.remove()
            destinationMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(destinationLocation!!)
                    .icon(createCustomMarker("Destination", "-- Min", "-- M"))
                    .anchor(0.5f, 1.5f)
                    .zIndex(3f)
            )
            destinationMarker?.tag = "DEST|-- min|-- mi"
        }
    }

    private fun fitCamera() {
        if (pickupLocation != null && destinationLocation != null) {
            val bounds = LatLngBounds.Builder()
                .include(pickupLocation!!).include(destinationLocation!!).build()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
        } else if (pickupLocation != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation!!, 14f))
        }
    }

    private fun startDelivery() {
        routeChoice = RouteChoice.CORRECT
        isDelivering = true
        currentStepIndex = 0
        deliveryStartTime = System.currentTimeMillis()
        wrongRouteStartTime = 0L
        totalDistanceMiles = 0.0
        wrongDistanceMiles = 0.0
        riderOnCorrectRoute = true

        deviationStartPoint = null
        wrongPathGrowingPolyline?.remove()
        wrongPathGrowingPolyline = null
        currentWrongSegmentPoints.clear()

        actualPathPoints.clear()
        trailSegments.forEach { it.remove() }
        trailSegments.clear()
        currentSegmentPoints.clear()
        currentSegmentIsCorrect = true

        if (pickupLocation != null) {
            currentSegmentPoints.add(pickupLocation!!)
            actualPathPoints.add(pickupLocation!!)

            riderMarker?.remove()

            // 🔴 Always start with correct path icon
            riderMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(pickupLocation!!)
                    .icon(getCorrectPathIcon())
                    .zIndex(5f)
                    .anchor(0.5f, 0.5f)
            )
        }

        toast("✅ Delivery started! Stay on the green path")
        startRealTimeTracking()
        startUiLoop()
    }

    // 🔴 Helper function to get correct path icon
    private fun getCorrectPathIcon(): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_curretpathicon)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 90, 90, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    // 🔴 Helper function to get wrong path icon
    private fun getWrongPathIcon(): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_wrongpathicon)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 90, 90, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    private fun startRealTimeTracking() {
        if (correctRoutePoints.isEmpty()) return

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L  // 3 seconds interval - more responsive
        ).apply {
            setMinUpdateIntervalMillis(1000L)  // 1 second min interval
            setMaxUpdateDelayMillis(5000L)     // 5 seconds max delay
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentLoc = LatLng(location.latitude, location.longitude)

                    // 🔴 Process real location only - no simulation
                    processLiveLocation(currentLoc)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun processLiveLocation(currentLoc: LatLng) {
        // 1. Destination check
        if (destinationLocation != null) {
            val distToDest = distanceMiles(currentLoc, destinationLocation!!)
            if (distToDest <= DESTINATION_THRESHOLD_MILES) {
                riderMarker?.position = destinationLocation!!
                onArrived()
                stopAllUpdates()
                return
            }
        }

        // 2. Route deviation check - 🔴 Using 30 meters threshold
        val isDeviating = checkForDeviation(currentLoc)

        if (isDeviating) {
            handleWrongPath(currentLoc)
        } else {
            handleCorrectPath(currentLoc)

            // Find nearest point on route for progress tracking
            val nearestPointInfo = findNearestPointOnRoute(currentLoc)
            currentStepIndex = nearestPointInfo.second
        }

        // 3. Update UI
        updatePathStatusUI(isDeviating)

        // 🔴 Update marker icon based on deviation status
        updateRiderIconBasedOnDeviation(isDeviating)

        riderMarker?.position = currentLoc
        actualPathPoints.add(currentLoc)
        drawActualPath()

        // 4. Camera follow
        if (shouldFollowRider) {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(currentLoc, 17f),
                500,
                null
            )
        }
    }

    // 🔴 New function to update icon based on deviation
    private fun updateRiderIconBasedOnDeviation(isDeviating: Boolean) {
        val icon = if (isDeviating) getWrongPathIcon() else getCorrectPathIcon()
        riderMarker?.setIcon(icon)
    }

    private fun findNearestPointOnRoute(currentLoc: LatLng): Pair<LatLng, Int> {
        var minDistance = Double.MAX_VALUE
        var nearestPoint = correctRoutePoints.first()
        var nearestIndex = 0

        correctRoutePoints.forEachIndexed { index, point ->
            val distance = distanceMiles(currentLoc, point)
            if (distance < minDistance) {
                minDistance = distance
                nearestPoint = point
                nearestIndex = index
            }
        }

        return Pair(nearestPoint, nearestIndex)
    }

    // 🔴 Updated: Stop all updates (location tracking + continuous API calls)
    private fun stopAllUpdates() {
        stopContinuousLocationUpdates()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // 🔴 Deviation Check - 30 meters threshold
    private fun checkForDeviation(currentLocation: LatLng): Boolean {
        var minDistance = Double.MAX_VALUE
        for (point in correctRoutePoints) {
            val distance = distanceMiles(currentLocation, point)
            if (distance < minDistance) minDistance = distance
        }

        // 🔴 Return true if distance > 30 meters (0.01864 miles)
        return minDistance > DEVIATION_THRESHOLD_MILES
    }

    private fun handleWrongPath(location: LatLng) {
        if (riderOnCorrectRoute) {
            riderOnCorrectRoute = false
            currentSegmentIsCorrect = false
            wrongRouteStartTime = System.currentTimeMillis()

            deviationStartPoint = if (actualPathPoints.isNotEmpty()) {
                actualPathPoints.last()
            } else {
                location
            }

            currentWrongSegmentPoints.clear()
            deviationStartPoint?.let { currentWrongSegmentPoints.add(it) }
            currentWrongSegmentPoints.add(location)

            if (currentSegmentPoints.size > 1) {
                commitCurrentSegment(true)
            }
        } else {
            currentWrongSegmentPoints.add(location)
        }

        drawGrowingWrongPath()

        if (currentWrongSegmentPoints.size > 1) {
            val prevLoc = currentWrongSegmentPoints[currentWrongSegmentPoints.size - 2]
            wrongDistanceMiles += distanceMiles(prevLoc, location)
        }
    }

    private fun drawGrowingWrongPath() {
        if (currentWrongSegmentPoints.size < 2) return

        wrongPathGrowingPolyline?.remove()

        wrongPathGrowingPolyline = googleMap.addPolyline(
            PolylineOptions()
                .addAll(currentWrongSegmentPoints)
                .color(Color.RED)
                .width(14f)
                .pattern(listOf(Dash(30f), Gap(10f)))
                .zIndex(10f)
                .geodesic(true)
        )
    }

    private fun handleCorrectPath(location: LatLng) {
        if (!riderOnCorrectRoute) {
            riderOnCorrectRoute = true
            currentSegmentIsCorrect = true

            if (currentWrongSegmentPoints.size > 1) {
                val permanentWrongPoly = googleMap.addPolyline(
                    PolylineOptions()
                        .addAll(currentWrongSegmentPoints)
                        .color(Color.RED)
                        .width(12f)
                        .pattern(listOf(Dash(20f), Gap(8f)))
                        .zIndex(3f)
                        .geodesic(false)
                )
                trailSegments.add(permanentWrongPoly)
            }

            wrongPathGrowingPolyline?.remove()
            wrongPathGrowingPolyline = null

            deviationStartPoint = null
            currentWrongSegmentPoints.clear()

            if (currentSegmentPoints.size > 1) {
                commitCurrentSegment(false)
            }
            currentSegmentPoints.clear()
            currentSegmentPoints.add(location)
        } else {
            currentSegmentPoints.add(location)
        }

        if (currentSegmentPoints.size > 1) {
            val prevLoc = currentSegmentPoints[currentSegmentPoints.size - 2]
            totalDistanceMiles += distanceMiles(prevLoc, location)
        }
    }

    private fun drawActualPath() {
        liveSegmentPolyline?.remove()
        if (actualPathPoints.size < 2) return

        if (!riderOnCorrectRoute) return

        liveSegmentPolyline = googleMap.addPolyline(
            PolylineOptions()
                .addAll(actualPathPoints)
                .color(Color.parseColor("#1ABC9C"))
                .width(12f)
                .zIndex(4f)
                .geodesic(false)
        )
    }

    private var liveSegmentPolyline: Polyline? = null

    private fun updatePathStatusUI(isDeviating: Boolean) {
        // Optional: Update UI text if needed
    }

    private fun commitCurrentSegment(wasCorrect: Boolean) {
        if (currentSegmentPoints.size < 2) { currentSegmentPoints.clear(); return }
        val poly = googleMap.addPolyline(buildSegmentOptions(wasCorrect, currentSegmentPoints))
        trailSegments.add(poly)
        currentSegmentPoints.clear()
    }

    private fun buildSegmentOptions(isCorrect: Boolean, points: List<LatLng>): PolylineOptions {
        return if (isCorrect) {
            PolylineOptions().addAll(points).color(Color.parseColor("#1ABC9C")).width(9f).zIndex(3f).geodesic(false)
        } else {
            PolylineOptions().addAll(points).color(Color.RED).width(9f)
               /* .pattern(listOf(Dash(20f), Gap(10f)))*/.zIndex(3f).geodesic(false)
        }
    }

    private fun startUiLoop() {
        uiRunnable = object : Runnable {
            override fun run() {
                if (!isDelivering) return
                val now = System.currentTimeMillis()

                val elapsedSec = (now - deliveryStartTime) / 1000L
                val fromTimeStr = when {
                    elapsedSec < 60 -> "${elapsedSec}s"
                    else -> "${elapsedSec / 60}min ${elapsedSec % 60}s"
                }
                val fromDistStr = formatMiles(totalDistanceMiles)

                updatePickupMarker(fromTimeStr, fromDistStr)

                if (correctRoutePoints.isNotEmpty() && destinationLocation != null) {
                    val currLoc = riderMarker?.position ?: correctRoutePoints[currentStepIndex.coerceAtMost(correctRoutePoints.size - 1)]

                    val distToDest = distanceMiles(currLoc, destinationLocation!!)
                    val etaMin = (distToDest / 15 * 60).toInt()
                    val toDistStr = formatDistanceWithUnit(distToDest)

                    updateDestinationMarker(etaMin, toDistStr)
                }

                mainHandler.postDelayed(this, UI_REFRESH_MS)
            }
        }
        mainHandler.post(uiRunnable!!)
    }

    private fun updatePickupMarker(timeStr: String, distanceStr: String) {
        if (pickupMarker == null || pickupLocation == null || !isAdded) return
        val displayDistance = when {
            distanceStr.contains("mi") -> {
                val miles = distanceStr.replace(" mi", "").toDoubleOrNull() ?: 0.0
                if (miles < 0.1) {
                    "${(miles * 1609.34).toInt()} M"
                } else {
                    "${String.format("%.2f", miles)} Mi"
                }
            }
            else -> distanceStr
        }

        val newIcon = createCustomMarker("Pickup Spot", timeStr, displayDistance)
        if (newIcon != null) {
            pickupMarker?.setIcon(newIcon)
            pickupMarker?.tag = "PICKUP|$timeStr|$displayDistance"
        }
    }

    private fun formatDistanceWithUnit(distanceMiles: Double): String {
        return when {
            distanceMiles < 0.1 -> {
                val meters = (distanceMiles * 1609.34).toInt()
                "${meters} M"
            }
            distanceMiles < 1.0 -> {
                String.format("%.2f Mi", distanceMiles)
            }
            else -> {
                String.format("%.1f Mi", distanceMiles)
            }
        }
    }

    private fun onArrived() {
        isDelivering = false
        stopLoops()
        stopAllUpdates() // 🔴 Stop all updates including continuous API calls

        if (!riderOnCorrectRoute && currentWrongSegmentPoints.size > 1) {
            val finalWrongPoly = googleMap.addPolyline(
                PolylineOptions()
                    .addAll(currentWrongSegmentPoints)
                    .color(Color.RED).width(12f)
                    .pattern(listOf(Dash(20f), Gap(8f))).zIndex(3f).geodesic(false)
            )
            trailSegments.add(finalWrongPoly)
            wrongPathGrowingPolyline?.remove()
            wrongPathGrowingPolyline = null
        }

        destinationLocation?.let { riderMarker?.position = it }

        drawActualPath()

        val totalMin = (System.currentTimeMillis() - deliveryStartTime) / 60000
        val totalDist = formatMiles(totalDistanceMiles)

        val message = if (wrongDistanceMiles > 0) {
            val wrongDist = formatMiles(wrongDistanceMiles)
            "⚠️ Delivery completed!\nWrong path: $wrongDist\nTotal distance: $totalDist in ${totalMin}min"
        } else {
            "✅ Perfect delivery!\nYou stayed on correct path!\nTotal distance: $totalDist in ${totalMin}min"
        }

        toast(message)
    }

    private fun stopLoops() {
        uiRunnable?.let { mainHandler.removeCallbacks(it) }
        uiRunnable = null
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOC_PERM_REQUEST)
        } else {
            enableMyLocation()
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // 🔴 Don't enable default my location - we use custom marker
            // googleMap.isMyLocationEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOC_PERM_REQUEST
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
            setCurrentLocationInPickup()
        }
    }

    private fun distanceMiles(point1: LatLng, point2: LatLng): Double {
        val R = 3959.0
        val latDistance = Math.toRadians(point2.latitude - point1.latitude)
        val lonDistance = Math.toRadians(point2.longitude - point1.longitude)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(point1.latitude)) *
                Math.cos(Math.toRadians(point2.latitude)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun formatMiles(miles: Double): String = String.format("%.1f mi", miles)

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
        // Don't stop updates on pause, let them continue
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAllUpdates() // 🔴 Stop all updates
        stopLoops()
        simulationRunnable?.let { handler.removeCallbacks(it) }
        riderMarker = null
        pickupMarker = null
        destinationMarker = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

    private fun createCustomMarker(title: String, time: String = "", distance: String = ""): BitmapDescriptor? {
        if (!isAdded) {
            Log.e("MarkerError", "Fragment not attached, cannot create marker")
            return null
        }
        return try {
            val markerView =
                LayoutInflater.from(requireContext()).inflate(R.layout.layout_marker_info, null)

            val tvTimeView = markerView.findViewById<TextView>(R.id.tvTime)
            val tvDistanceView = markerView.findViewById<TextView>(R.id.tvDistance)
            val tvTitleView = markerView.findViewById<TextView>(R.id.tvTitle)

            Log.d("MarkerDebug", "tvTime found: ${tvTimeView != null}")
            Log.d("MarkerDebug", "tvDistance found: ${tvDistanceView != null}")
            Log.d("MarkerDebug", "tvTitle found: ${tvTitleView != null}")

            tvTimeView?.text = time
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

    private fun startJourney(
        currentLatitude : String,
        currentLongitude : String,
        destinationLatitude : String,
        destinationLongitude : String) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.startJourney(currentLatitude,currentLongitude,destinationLatitude,destinationLongitude).collect {
                    BaseApplication.dismissDialog()
                    try {
                        Log.d("@@@ addMea List ", "data :- $it")
                        if (it.data!!.code == 200 && it.data!!.status) {
                            val journeyId = it.data?.data?.journey_id
                            if (journeyId != null) {
                                currentJourneyId = journeyId.toString()
                                // 🔴 Start continuous location updates instead of periodic WorkManager
                                startContinuousLocationUpdates(currentJourneyId)
                            }
                            showAlert(it.data!!.message, false)
                        } else {
                            handleError(it.data!!.code, it.data!!.message)
                        }
                    } catch (e: Exception) {
                        showAlert(e.message, false)
                    }
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    // This is common function for show the alert box
    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(context, message, status)
    }

    private fun handleError(code: Int?, message: String?) {
        if (code == MessageClass.deactivatedUser || code == MessageClass.deletedUser) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    // 🔴 NEW: Continuous location updates using coroutine (every 5 seconds)
    private fun startContinuousLocationUpdates(journeyId: String) {
        Log.d("RetrofitLog", "🚀 Starting CONTINUOUS location updates for journey: $journeyId")

        currentJourneyId = journeyId
        isUpdatingLocation = true

        // Cancel any existing job
        continuousLocationUpdateJob?.cancel()

        // Start continuous updates using coroutine
        continuousLocationUpdateJob = lifecycleScope.launch {
            while (isUpdatingLocation) {
                try {
                    // Get current location from marker
                    val currentLocation = riderMarker?.position

                    if (currentLocation != null) {
                        Log.d("RetrofitLog", "📍 Current location: (${currentLocation.latitude}, ${currentLocation.longitude})")

                        // Call API directly using coroutine
                        updateLiveLocationDirectly(
                            journeyId = journeyId,
                            token = getAuthToken(),
                            latitude = currentLocation.latitude,
                            longitude = currentLocation.longitude
                        )
                    } else {
                        Log.w("RetrofitLog", "⚠️ Rider marker location is null")
                    }

                    // Wait for 5 seconds
                    delay(5000)

                } catch (e: Exception) {
                    Log.e("RetrofitLog", "❌ Error in continuous update loop: ${e.message}")
                    e.printStackTrace()
                    // Still wait and continue
                    delay(5000)
                }
            }
        }

        Log.d("RetrofitLog", "✅ Continuous updates coroutine started")
    }

    // Direct API call function
    private suspend fun updateLiveLocationDirectly(
        journeyId: String,
        token: String,
        latitude: Double,
        longitude: Double
    ) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("RetrofitLog", "📡 Making direct API call at ${System.currentTimeMillis()}")

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val json = JSONObject().apply {
                    put("journey_id", journeyId)
                    put("latitude", latitude.toString())
                    put("longitude", longitude.toString())
                }

                val request = Request.Builder()
                    .url("https://alertapp.tgastaging.com/api/update_live_location")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()

                Log.d("RetrofitLog", "📥 Response Code: ${response.code}")

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("RetrofitLog", "✅ API Success: $responseBody")
                } else {
                    Log.e("RetrofitLog", "❌ API Failed: ${response.code}")
                }

            } catch (e: Exception) {
                Log.e("RetrofitLog", "❌ Network Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Stop function for continuous updates
    private fun stopContinuousLocationUpdates() {
        Log.d("RetrofitLog", "🛑 Stopping continuous location updates")
        isUpdatingLocation = false
        continuousLocationUpdateJob?.cancel()
        continuousLocationUpdateJob = null
    }

    // Function to get auth token
    private fun getAuthToken(): String {
        return SessionManagement(requireActivity()).getUserToken().toString()
    }
}
 */

@AndroidEntryPoint
class WatchOverMeChooseStartingPointFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentWatchovermechoosestartingpointBinding
    private lateinit var viewModel: WatchOverMeViewModel
    private lateinit var googleMap: GoogleMap
    private var shouldFollowRider = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var placesClient: PlacesClient
    private lateinit var currentLocationAdapter: PlaceAutoSuggestAdapter
    private lateinit var destinationAdapter: PlaceAutoSuggestAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var simulationRunnable: Runnable? = null

    private var riderMarker: Marker? = null
    private var pickupMarker: Marker? = null
    private var destinationMarker: Marker? = null

    // Selected places
    private var pickupLocation: LatLng? = null
    private var destinationLocation: LatLng? = null

    // Autocomplete fragments
    private lateinit var destinationAutocompleteFragment: AutocompleteSupportFragment

    private var correctRefPolyline: Polyline? = null
    private val correctTrailSegments = mutableListOf<Polyline>() // Only correct path segments
    private val wrongTrailSegments = mutableListOf<Polyline>() // Only wrong path segments (permanent)
    private var correctRoutePoints: List<LatLng> = listOf()

    // Separate lists for correct and wrong path points
    private val correctPathPoints = mutableListOf<LatLng>()
    private val wrongPathPoints = mutableListOf<LatLng>()

    private enum class RouteChoice { NONE, CORRECT }
    private var routeChoice = RouteChoice.NONE
    private var isDelivering = false
    private var currentStepIndex = 0

    private var deliveryStartTime = 0L
    private var wrongRouteStartTime = 0L
    private var totalDistanceMiles = 0.0
    private var wrongDistanceMiles = 0.0

    private var currentSegmentIsCorrect = true
    private val currentSegmentPoints = mutableListOf<LatLng>()
    private var riderOnCorrectRoute = true

    private var deviationStartPoint: LatLng? = null
    private var wrongPathGrowingPolyline: Polyline? = null
    private val currentWrongSegmentPoints = mutableListOf<LatLng>()

    // 🔴 NEW: Store all wrong path segments permanently
    private val permanentWrongPaths = mutableListOf<Polyline>()

    // 🔴 IMPORTANT: 30 meters threshold (0.01864 miles = ~30 meters)
    private val DEVIATION_THRESHOLD_MILES = 30.0 / 1609.34  // ~0.01864 miles (30 meters)
    private var wrongPathAlertSent = false
    private val DESTINATION_THRESHOLD_MILES = 5.0 / 1609.34  // ~0.003107 miles

    // ─── Handlers ────────────────────────────────────────────────────
    private val mainHandler = Handler(Looper.getMainLooper())
    private var uiRunnable: Runnable? = null
    private val UI_REFRESH_MS = 500L
    private val LOC_PERM_REQUEST = 100

    // 🔴 NEW: Properties for continuous location updates
    private var currentJourneyId: String = ""
    private var continuousLocationUpdateJob: Job? = null
    private var isUpdatingLocation = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWatchovermechoosestartingpointBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this)[WatchOverMeViewModel::class.java]

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.api_key))
        }
        placesClient = Places.createClient(requireContext())

        currentLocationAdapter = PlaceAutoSuggestAdapter(requireContext(), placesClient)
        destinationAdapter = PlaceAutoSuggestAdapter(requireContext(), placesClient)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupPlaceAutocomplete()
        setupButtons()
        setCurrentLocationInPickup()
        binding.map.onCreate(savedInstanceState)
        binding.map.getMapAsync(this)

        return binding.root
    }

    private fun setCurrentLocationInPickup() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(requireActivity(), Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val addressText = addresses[0].getAddressLine(0)
                            pickupLocation = LatLng(location.latitude, location.longitude)
                            binding.etPickup.setText(addressText)
                            if (::googleMap.isInitialized) updateMapForSelectedPlaces()
                            Toast.makeText(requireActivity(), "Current location set as pickup", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        pickupLocation = LatLng(location.latitude, location.longitude)
                        binding.etPickup.setText("${location.latitude}, ${location.longitude}")
                        if (::googleMap.isInitialized) updateMapForSelectedPlaces()
                    }
                } else {
                    Toast.makeText(requireActivity(), "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOC_PERM_REQUEST)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupPlaceAutocomplete() {
        destinationAutocompleteFragment = AutocompleteSupportFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.destination_autocomplete_container, destinationAutocompleteFragment)
            .commitNow()

        destinationAutocompleteFragment.setPlaceFields(
            Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
            )
        )
        destinationAutocompleteFragment.setHint("Search destination")
        destinationAutocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                destinationLocation = place.latLng
                updateMapForSelectedPlaces()
            }
            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(requireActivity(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupButtons() {
        binding.btnExplore.setOnClickListener {
            if (pickupLocation == null) {
                Toast.makeText(requireActivity(), "Please select pickup location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (destinationLocation == null) {
                Toast.makeText(requireActivity(), "Please select destination", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startJourney( pickupLocation!!.latitude.toString(),
                pickupLocation!!.longitude.toString(),
                destinationLocation!!.latitude.toString(),
                destinationLocation!!.longitude.toString())
            setupRoutesAndStart()
            startDelivery()
        }
    }

    private fun setupRoutesAndStart() {
        if (pickupLocation != null && destinationLocation != null) {
            binding.progressBar?.visibility = View.VISIBLE
            getStreetRoute(pickupLocation!!, destinationLocation!!)
            binding.btnExplore.visibility = View.GONE
        }
    }

    private fun getStreetRoute(start: LatLng, end: LatLng) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val routePoints = getRouteFromDirectionsAPI(start, end)
                withContext(Dispatchers.Main) {
                    binding.progressBar?.visibility = View.GONE
                    if (routePoints.isNotEmpty()) {
                        correctRoutePoints = routePoints
                        drawCorrectReferenceLine()
                        placeMarkers()
                        fitCamera()
                        Toast.makeText(requireActivity(), "Street-accurate route loaded!", Toast.LENGTH_SHORT).show()
                    } else {
                        generateGridRoute(start, end)
                        Toast.makeText(requireActivity(), "Using grid-based route", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar?.visibility = View.GONE
                    generateGridRoute(start, end)
                    Toast.makeText(requireActivity(), "Using grid-based route: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getRouteFromDirectionsAPI(origin: LatLng, destination: LatLng): List<LatLng> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=${origin.latitude},${origin.longitude}" +
                        "&destination=${destination.latitude},${destination.longitude}" +
                        "&mode=driving" +
                        "&key=${getString(R.string.api_key)}" +
                        "&alternatives=false"

                val client = OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "")
                    val routes = json.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        val encodedPolyline = route.getJSONObject("overview_polyline").getString("points")
                        return@withContext decodePolyline(encodedPolyline)
                    }
                }
                emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int; var shift = 0; var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0; result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(LatLng(lat.toDouble() / 1e5, lng.toDouble() / 1e5))
        }
        return poly
    }

    private fun generateGridRoute(start: LatLng, end: LatLng) {
        val points = mutableListOf<LatLng>()
        points.add(start)
        val latDiff = end.latitude - start.latitude
        val lngDiff = end.longitude - start.longitude
        val steps = 20
        for (i in 1 until steps) {
            val fraction = i.toDouble() / steps
            val offset = Math.sin(fraction * Math.PI * 4) * 0.002
            val lat = start.latitude + (latDiff * fraction) + (if (i % 4 == 0) offset else 0.0)
            val lng = start.longitude + (lngDiff * fraction) + (if (i % 4 == 2) offset else 0.0)
            points.add(LatLng(lat, lng))
        }
        points.add(end)
        correctRoutePoints = points
        drawCorrectReferenceLine()
        placeMarkers()
        fitCamera()
    }

    private fun drawCorrectReferenceLine() {
        correctRefPolyline?.remove()
        if (correctRoutePoints.isNotEmpty()) {
            correctRefPolyline = googleMap.addPolyline(
                PolylineOptions()
                    .addAll(correctRoutePoints)
                    .color(Color.parseColor("#2196F3"))
                    .width(8f)
                    .zIndex(1f)
                    .geodesic(false)
                    .clickable(true)
            )
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = false
            mapType = GoogleMap.MAP_TYPE_NORMAL
        }
        checkLocationPermission()

        // 🔴 Disable default my location icon
        try {
            googleMap.setMyLocationEnabled(false)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        if (pickupLocation != null) updateMapForSelectedPlaces()
    }

    private fun updateMapForSelectedPlaces() {
        if (pickupLocation != null && destinationLocation != null) {
            getStreetRoute(pickupLocation!!, destinationLocation!!)
            placeMarkers()
            fitCamera()
        } else if (pickupLocation != null) {
            pickupMarker?.remove()
            pickupMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(pickupLocation!!)
                    .icon(createCustomMarker("Pickup Spot", "0 Min", "0 M"))
                    .anchor(0.5f, 1.5f)
                    .zIndex(3f)
            )
            pickupMarker?.tag = "PICKUP|Not started|0.0 mi"
        } else if (destinationLocation != null) {
            destinationMarker?.remove()
            destinationMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(destinationLocation!!)
                    .icon(createCustomMarker("Destination", "3 Min", "900 M"))
                    .anchor(0.5f, 1.5f)
                    .zIndex(3f)
            )
            destinationMarker?.tag = "DEST|-- min|-- mi"
        }
    }

    private fun updateDestinationMarker(etaMin: Int, distanceStr: String) {
        if (destinationMarker == null || destinationLocation == null || !isAdded) return

        val displayDistance = when {
            distanceStr.contains("mi") -> {
                val miles = distanceStr.replace(" mi", "").toDoubleOrNull() ?: 0.0
                if (miles < 0.1) {
                    "${(miles * 1609.34).toInt()} M"
                } else {
                    "${String.format("%.1f", miles)} Mi"
                }
            }
            else -> distanceStr
        }

        val newIcon = createCustomMarker("Destination", "$etaMin Min", displayDistance)
        if (newIcon != null) {
            destinationMarker?.setIcon(newIcon)
            destinationMarker?.tag = "DEST|$etaMin min|$displayDistance"
        }
    }

    private fun placeMarkers() {
        if (pickupLocation != null) {
            pickupMarker?.remove()

            val (initialDistance, initialTime) = if (destinationLocation != null) {
                val dist = distanceMiles(pickupLocation!!, destinationLocation!!)
                val time = (dist / 15 * 60).toInt()
                Pair(formatMiles(dist), "$time Min")
            } else {
                Pair("0 M", "0 Min")
            }

            pickupMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(pickupLocation!!)
                    .icon(createCustomMarker("Pickup Spot", initialTime, initialDistance))
                    .anchor(0.5f, 1.5f)
                    .zIndex(3f)
            )
            pickupMarker?.tag = "PICKUP|Not started|0.0 mi"
        }

        if (destinationLocation != null) {
            destinationMarker?.remove()
            destinationMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(destinationLocation!!)
                    .icon(createCustomMarker("Destination", "-- Min", "-- M"))
                    .anchor(0.5f, 1.5f)
                    .zIndex(3f)
            )
            destinationMarker?.tag = "DEST|-- min|-- mi"
        }
    }

    private fun fitCamera() {
        if (pickupLocation != null && destinationLocation != null) {
            val bounds = LatLngBounds.Builder()
                .include(pickupLocation!!).include(destinationLocation!!).build()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
        } else if (pickupLocation != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation!!, 14f))
        }
    }

    private fun startDelivery() {
        routeChoice = RouteChoice.CORRECT
        isDelivering = true
        currentStepIndex = 0
        deliveryStartTime = System.currentTimeMillis()
        wrongRouteStartTime = 0L
        totalDistanceMiles = 0.0
        wrongDistanceMiles = 0.0
        riderOnCorrectRoute = true

        // Clear all previous paths
        correctPathPoints.clear()
        wrongPathPoints.clear()
        permanentWrongPaths.forEach { it.remove() }
        permanentWrongPaths.clear()
        correctTrailSegments.forEach { it.remove() }
        correctTrailSegments.clear()
        wrongTrailSegments.forEach { it.remove() }
        wrongTrailSegments.clear()

        deviationStartPoint = null
        wrongPathGrowingPolyline?.remove()
        wrongPathGrowingPolyline = null
        currentWrongSegmentPoints.clear()
        currentSegmentPoints.clear()
        currentSegmentIsCorrect = true

        if (pickupLocation != null) {
            currentSegmentPoints.add(pickupLocation!!)
            correctPathPoints.add(pickupLocation!!) // Add to correct path initially

            riderMarker?.remove()

            // 🔴 Always start with correct path icon
            riderMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(pickupLocation!!)
                    .icon(getCorrectPathIcon())
                    .zIndex(5f)
                    .anchor(0.5f, 0.5f)
            )
        }

        toast("✅ Delivery started! Stay on the green path")
        startRealTimeTracking()
        startUiLoop()
    }

    // 🔴 Helper function to get correct path icon
    private fun getCorrectPathIcon(): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_curretpathicon)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 90, 90, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    // 🔴 Helper function to get wrong path icon
    private fun getWrongPathIcon(): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_wrongpathicon)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 90, 90, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    private fun startRealTimeTracking() {
        if (correctRoutePoints.isEmpty()) return

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L  // 3 seconds interval - more responsive
        ).apply {
            setMinUpdateIntervalMillis(1000L)  // 1 second min interval
            setMaxUpdateDelayMillis(5000L)     // 5 seconds max delay
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentLoc = LatLng(location.latitude, location.longitude)

                    // 🔴 Process real location only - no simulation
                    processLiveLocation(currentLoc)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun processLiveLocation(currentLoc: LatLng) {
        // 1. Destination check
        if (destinationLocation != null) {
            val distToDest = distanceMiles(currentLoc, destinationLocation!!)
            if (distToDest <= DESTINATION_THRESHOLD_MILES) {
                riderMarker?.position = destinationLocation!!
                onArrived()
                stopAllUpdates()
                return
            }
        }

        // 2. Route deviation check - 🔴 Using 30 meters threshold
        val isDeviating = checkForDeviation(currentLoc)

        if (isDeviating) {
            handleWrongPath(currentLoc)
            // 🔴 Call API only once when deviation starts
            if (!wrongPathAlertSent && currentJourneyId.isNotEmpty()) {
                wrongPathAlertSent = true
                callWrongPathApi()
            }
        } else {
            handleCorrectPath(currentLoc)
            // 🔴 Reset flag when rider comes back to correct path
            wrongPathAlertSent = false
            // Find nearest point on route for progress tracking
            val nearestPointInfo = findNearestPointOnRoute(currentLoc)
            currentStepIndex = nearestPointInfo.second
        }

        // 3. Update UI
        updatePathStatusUI(isDeviating)

        // 🔴 Update marker icon based on deviation status
        updateRiderIconBasedOnDeviation(isDeviating)

        riderMarker?.position = currentLoc

        // 🔴 Draw separate paths for correct and wrong segments
        drawPaths()

        // 4. Camera follow
        if (shouldFollowRider) {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(currentLoc, 17f),
                500,
                null
            )
        }
    }

    // 🔴 New function to update icon based on deviation
    private fun updateRiderIconBasedOnDeviation(isDeviating: Boolean) {
        val icon = if (isDeviating) getWrongPathIcon() else getCorrectPathIcon()
        riderMarker?.setIcon(icon)
    }

    private fun findNearestPointOnRoute(currentLoc: LatLng): Pair<LatLng, Int> {
        var minDistance = Double.MAX_VALUE
        var nearestPoint = correctRoutePoints.first()
        var nearestIndex = 0

        correctRoutePoints.forEachIndexed { index, point ->
            val distance = distanceMiles(currentLoc, point)
            if (distance < minDistance) {
                minDistance = distance
                nearestPoint = point
                nearestIndex = index
            }
        }

        return Pair(nearestPoint, nearestIndex)
    }

    // 🔴 Updated: Stop all updates (location tracking + continuous API calls)
    private fun stopAllUpdates() {
        stopContinuousLocationUpdates()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // 🔴 Deviation Check - 30 meters threshold
    private fun checkForDeviation(currentLocation: LatLng): Boolean {
        var minDistance = Double.MAX_VALUE
        for (point in correctRoutePoints) {
            val distance = distanceMiles(currentLocation, point)
            if (distance < minDistance) minDistance = distance
        }

        // 🔴 Return true if distance > 30 meters (0.01864 miles)
        return minDistance > DEVIATION_THRESHOLD_MILES
    }

    private fun handleWrongPath(location: LatLng) {
        if (riderOnCorrectRoute) {
            // Just deviated from correct path
            riderOnCorrectRoute = false
            currentSegmentIsCorrect = false
            wrongRouteStartTime = System.currentTimeMillis()

            // Save the point where deviation started
            deviationStartPoint = if (correctPathPoints.isNotEmpty()) {
                correctPathPoints.last()
            } else {
                location
            }

            // Start new wrong segment
            currentWrongSegmentPoints.clear()
            deviationStartPoint?.let {
                currentWrongSegmentPoints.add(it)
                wrongPathPoints.add(it) // Add to wrong path points
            }
            currentWrongSegmentPoints.add(location)
            wrongPathPoints.add(location)

            // Commit the last correct segment
            if (currentSegmentPoints.size > 1) {
                commitCurrentSegment(true)
            }
            currentSegmentPoints.clear()
        } else {
            // Continue on wrong path
            currentWrongSegmentPoints.add(location)
            wrongPathPoints.add(location)
        }

        // Draw the growing wrong path in RED with dashed pattern
        drawGrowingWrongPath()

        // Update wrong distance
        if (currentWrongSegmentPoints.size > 1) {
            val prevLoc = currentWrongSegmentPoints[currentWrongSegmentPoints.size - 2]
            wrongDistanceMiles += distanceMiles(prevLoc, location)
        }
    }

    private fun drawGrowingWrongPath() {
        if (currentWrongSegmentPoints.size < 2) return

        // Remove previous growing polyline
        wrongPathGrowingPolyline?.remove()

        // Create new dashed red line for current wrong segment
        wrongPathGrowingPolyline = googleMap.addPolyline(
            PolylineOptions()
                .addAll(currentWrongSegmentPoints)
                .color(Color.RED)
                .width(14f)
                .pattern(listOf(Dash(30f), Gap(10f))) // Dashed pattern for wrong path
                .zIndex(10f) // Higher z-index to show above other lines
                .geodesic(true)
                .clickable(false)
        )
    }

    private fun handleCorrectPath(location: LatLng) {
        if (!riderOnCorrectRoute) {
            // Just returned to correct path
            riderOnCorrectRoute = true
            currentSegmentIsCorrect = true

            // Save the wrong segment permanently
            if (currentWrongSegmentPoints.size > 1) {
                val permanentWrongPoly = googleMap.addPolyline(
                    PolylineOptions()
                        .addAll(currentWrongSegmentPoints)
                        .color(Color.RED)
                        .width(12f)
                        .pattern(listOf(Dash(20f), Gap(8f))) // Dashed pattern for permanent wrong path
                        .zIndex(3f)
                        .geodesic(false)
                )
                wrongTrailSegments.add(permanentWrongPoly)
                permanentWrongPaths.add(permanentWrongPoly) // Store for later cleanup
            }

            // Clear temporary wrong path
            wrongPathGrowingPolyline?.remove()
            wrongPathGrowingPolyline = null
            deviationStartPoint = null
            currentWrongSegmentPoints.clear()

            // Start new correct segment
            if (currentSegmentPoints.size > 1) {
                commitCurrentSegment(false)
            }
            currentSegmentPoints.clear()
            currentSegmentPoints.add(location)
            correctPathPoints.add(location) // Add to correct path points
        } else {
            // Continue on correct path
            currentSegmentPoints.add(location)
            correctPathPoints.add(location) // Add to correct path points
        }

        // Update total distance
        if (currentSegmentPoints.size > 1) {
            val prevLoc = currentSegmentPoints[currentSegmentPoints.size - 2]
            totalDistanceMiles += distanceMiles(prevLoc, location)
        }
    }

    // 🔴 NEW: Draw both correct and wrong paths separately
    private fun drawPaths() {
        // Draw correct path in green (if we have at least 2 points)
        if (correctPathPoints.size >= 2) {
            // Remove old correct path polyline if exists
            correctTrailSegments.forEach { it.remove() }
            correctTrailSegments.clear()

            // Draw the entire correct path
            val correctPathPolyline = googleMap.addPolyline(
                PolylineOptions()
                    .addAll(correctPathPoints)
                    .color(Color.parseColor("#1ABC9C"))
                    .width(12f)
                    .zIndex(4f)
                    .geodesic(false)
            )
            correctTrailSegments.add(correctPathPolyline)
        }

        // Draw wrong path in red (if we have at least 2 points)
        if (wrongPathPoints.size >= 2) {
            // Remove old wrong path polylines
            wrongTrailSegments.forEach { it.remove() }
            wrongTrailSegments.clear()

            // Draw the entire wrong path with dashed pattern
            val wrongPathPolyline = googleMap.addPolyline(
                PolylineOptions()
                    .addAll(wrongPathPoints)
                    .color(Color.RED)
                    .width(12f)
                    .pattern(listOf(Dash(20f), Gap(8f)))
                    .zIndex(3f)
                    .geodesic(false)
            )
            wrongTrailSegments.add(wrongPathPolyline)
        }
    }

    private var liveSegmentPolyline: Polyline? = null

    private fun updatePathStatusUI(isDeviating: Boolean) {
        // Optional: Update UI text if needed
    }

    private fun commitCurrentSegment(wasCorrect: Boolean) {
        if (currentSegmentPoints.size < 2) {
            currentSegmentPoints.clear()
            return
        }

        if (wasCorrect) {
            // Add to correct path
            correctPathPoints.addAll(currentSegmentPoints)
        }

        currentSegmentPoints.clear()
    }

    private fun buildSegmentOptions(isCorrect: Boolean, points: List<LatLng>): PolylineOptions {
        return if (isCorrect) {
            PolylineOptions()
                .addAll(points)
                .color(Color.parseColor("#1ABC9C"))
                .width(9f)
                .zIndex(3f)
                .geodesic(false)
        } else {
            PolylineOptions()
                .addAll(points)
                .color(Color.RED)
                .width(9f)
                .pattern(listOf(Dash(20f), Gap(10f))) // Dashed pattern for wrong path
                .zIndex(3f)
                .geodesic(false)
        }
    }

    private fun startUiLoop() {
        uiRunnable = object : Runnable {
            override fun run() {
                if (!isDelivering) return
                val now = System.currentTimeMillis()

                val elapsedSec = (now - deliveryStartTime) / 1000L
                val fromTimeStr = when {
                    elapsedSec < 60 -> "${elapsedSec}s"
                    else -> "${elapsedSec / 60}min ${elapsedSec % 60}s"
                }
                val fromDistStr = formatMiles(totalDistanceMiles)

                updatePickupMarker(fromTimeStr, fromDistStr)

                if (correctRoutePoints.isNotEmpty() && destinationLocation != null) {
                    val currLoc = riderMarker?.position ?: correctRoutePoints[currentStepIndex.coerceAtMost(correctRoutePoints.size - 1)]

                    val distToDest = distanceMiles(currLoc, destinationLocation!!)
                    val etaMin = (distToDest / 15 * 60).toInt()
                    val toDistStr = formatDistanceWithUnit(distToDest)

                    updateDestinationMarker(etaMin, toDistStr)
                }

                mainHandler.postDelayed(this, UI_REFRESH_MS)
            }
        }
        mainHandler.post(uiRunnable!!)
    }

    private fun updatePickupMarker(timeStr: String, distanceStr: String) {
        if (pickupMarker == null || pickupLocation == null || !isAdded) return
        val displayDistance = when {
            distanceStr.contains("mi") -> {
                val miles = distanceStr.replace(" mi", "").toDoubleOrNull() ?: 0.0
                if (miles < 0.1) {
                    "${(miles * 1609.34).toInt()} M"
                } else {
                    "${String.format("%.2f", miles)} Mi"
                }
            }
            else -> distanceStr
        }

        val newIcon = createCustomMarker("Pickup Spot", timeStr, displayDistance)
        if (newIcon != null) {
            pickupMarker?.setIcon(newIcon)
            pickupMarker?.tag = "PICKUP|$timeStr|$displayDistance"
        }
    }

    private fun formatDistanceWithUnit(distanceMiles: Double): String {
        return when {
            distanceMiles < 0.1 -> {
                val meters = (distanceMiles * 1609.34).toInt()
                "${meters} M"
            }
            distanceMiles < 1.0 -> {
                String.format("%.2f Mi", distanceMiles)
            }
            else -> {
                String.format("%.1f Mi", distanceMiles)
            }
        }
    }

    private fun onArrived() {
        isDelivering = false
        stopLoops()
        stopAllUpdates() // 🔴 Stop all updates including continuous API calls

        // Save the final wrong segment if user is still on wrong path
        if (!riderOnCorrectRoute && currentWrongSegmentPoints.size > 1) {
            val finalWrongPoly = googleMap.addPolyline(
                PolylineOptions()
                    .addAll(currentWrongSegmentPoints)
                    .color(Color.RED)
                    .width(12f)
                    .pattern(listOf(Dash(20f), Gap(8f)))
                    .zIndex(3f)
                    .geodesic(false)
            )
            wrongTrailSegments.add(finalWrongPoly)
            permanentWrongPaths.add(finalWrongPoly)
            wrongPathPoints.addAll(currentWrongSegmentPoints)
            wrongPathGrowingPolyline?.remove()
            wrongPathGrowingPolyline = null
        }

        destinationLocation?.let { riderMarker?.position = it }

        // Final draw of paths
        drawPaths()

        val totalMin = (System.currentTimeMillis() - deliveryStartTime) / 60000
        val totalDist = formatMiles(totalDistanceMiles)

        val message = if (wrongDistanceMiles > 0) {
            val wrongDist = formatMiles(wrongDistanceMiles)
            "⚠️ Delivery completed!\nWrong path: $wrongDist\nTotal distance: $totalDist in ${totalMin}min"
        } else {
            "✅ Perfect delivery!\nYou stayed on correct path!\nTotal distance: $totalDist in ${totalMin}min"
        }

        toast(message)
    }

    private fun stopLoops() {
        uiRunnable?.let { mainHandler.removeCallbacks(it) }
        uiRunnable = null
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOC_PERM_REQUEST)
        } else {
            enableMyLocation()
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // 🔴 Don't enable default my location - we use custom marker
            // googleMap.isMyLocationEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOC_PERM_REQUEST
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
            setCurrentLocationInPickup()
        }
    }

    private fun distanceMiles(point1: LatLng, point2: LatLng): Double {
        val R = 3959.0
        val latDistance = Math.toRadians(point2.latitude - point1.latitude)
        val lonDistance = Math.toRadians(point2.longitude - point1.longitude)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(point1.latitude)) *
                Math.cos(Math.toRadians(point2.latitude)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun formatMiles(miles: Double): String = String.format("%.1f mi", miles)

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
        // Don't stop updates on pause, let them continue
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAllUpdates() // 🔴 Stop all updates
        stopLoops()
        simulationRunnable?.let { handler.removeCallbacks(it) }

        // Clean up all polylines
        permanentWrongPaths.forEach { it.remove() }
        permanentWrongPaths.clear()
        correctTrailSegments.forEach { it.remove() }
        correctTrailSegments.clear()
        wrongTrailSegments.forEach { it.remove() }
        wrongTrailSegments.clear()

        riderMarker = null
        pickupMarker = null
        destinationMarker = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

    private fun createCustomMarker(title: String, time: String = "", distance: String = ""): BitmapDescriptor? {
        if (!isAdded) {
            Log.e("MarkerError", "Fragment not attached, cannot create marker")
            return null
        }
        return try {
            val markerView =
                LayoutInflater.from(requireContext()).inflate(R.layout.layout_marker_info, null)

            val tvTimeView = markerView.findViewById<TextView>(R.id.tvTime)
            val tvDistanceView = markerView.findViewById<TextView>(R.id.tvDistance)
            val tvTitleView = markerView.findViewById<TextView>(R.id.tvTitle)

            Log.d("MarkerDebug", "tvTime found: ${tvTimeView != null}")
            Log.d("MarkerDebug", "tvDistance found: ${tvDistanceView != null}")
            Log.d("MarkerDebug", "tvTitle found: ${tvTitleView != null}")

            tvTimeView?.text = time
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

    private fun startJourney(
        currentLatitude : String,
        currentLongitude : String,
        destinationLatitude : String,
        destinationLongitude : String) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.startJourney(currentLatitude,currentLongitude,destinationLatitude,destinationLongitude).collect {
                    BaseApplication.dismissDialog()
                    try {
                        Log.d("@@@ addMea List ", "data :- $it")
                        if (it.data!!.code == 200 && it.data!!.status) {
                            val journeyId = it.data?.data?.journey_id
                            if (journeyId != null) {
                                currentJourneyId = journeyId.toString()
                                // 🔴 Start continuous location updates instead of periodic WorkManager
                                startContinuousLocationUpdates(currentJourneyId)
                            }
                            showAlert(it.data!!.message, false)
                        } else {
                            handleError(it.data!!.code, it.data!!.message)
                        }
                    } catch (e: Exception) {
                        showAlert(e.message, false)
                    }
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    // This is common function for show the alert box
    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(context, message, status)
    }

    private fun handleError(code: Int?, message: String?) {
        if (code == MessageClass.deactivatedUser || code == MessageClass.deletedUser) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    // 🔴 NEW: Continuous location updates using coroutine (every 5 seconds)
    private fun startContinuousLocationUpdates(journeyId: String) {
        Log.d("RetrofitLog", "🚀 Starting CONTINUOUS location updates for journey: $journeyId")

        currentJourneyId = journeyId
        isUpdatingLocation = true

        // Cancel any existing job
        continuousLocationUpdateJob?.cancel()

        // Start continuous updates using coroutine
        continuousLocationUpdateJob = lifecycleScope.launch {
            while (isUpdatingLocation) {
                try {
                    // Get current location from marker
                    val currentLocation = riderMarker?.position

                    if (currentLocation != null) {
                        Log.d("RetrofitLog", "📍 Current location: (${currentLocation.latitude}, ${currentLocation.longitude})")

                        // Call API directly using coroutine
                        updateLiveLocationDirectly(
                            journeyId = journeyId,
                            token = getAuthToken(),
                            latitude = currentLocation.latitude,
                            longitude = currentLocation.longitude
                        )
                    } else {
                        Log.w("RetrofitLog", "⚠️ Rider marker location is null")
                    }

                    // Wait for 5 seconds
                    delay(5000)

                } catch (e: Exception) {
                    Log.e("RetrofitLog", "❌ Error in continuous update loop: ${e.message}")
                    e.printStackTrace()
                    // Still wait and continue
                    delay(5000)
                }
            }
        }

        Log.d("RetrofitLog", "✅ Continuous updates coroutine started")
    }

    // Direct API call function
    private suspend fun updateLiveLocationDirectly(
        journeyId: String,
        token: String,
        latitude: Double,
        longitude: Double
    ) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("RetrofitLog", "📡 Making direct API call at ${System.currentTimeMillis()}")

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val json = JSONObject().apply {
                    put("journey_id", journeyId)
                    put("latitude", latitude.toString())
                    put("longitude", longitude.toString())
                }

                val request = Request.Builder()
                    .url("https://alertapp.tgastaging.com/api/update_live_location")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()

                Log.d("RetrofitLog", "📥 Response Code: ${response.code}")

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("RetrofitLog", "✅ API Success: $responseBody")
                } else {
                    Log.e("RetrofitLog", "❌ API Failed: ${response.code}")
                }

            } catch (e: Exception) {
                Log.e("RetrofitLog", "❌ Network Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Stop function for continuous updates
    private fun stopContinuousLocationUpdates() {
        Log.d("RetrofitLog", "🛑 Stopping continuous location updates")
        isUpdatingLocation = false
        continuousLocationUpdateJob?.cancel()
        continuousLocationUpdateJob = null
    }

    // Function to get auth token
    private fun getAuthToken(): String {
        return SessionManagement(requireActivity()).getUserToken().toString()
    }

    private fun callWrongPathApi() {
        lifecycleScope.launch {
            viewModel.setAlertWrongPath(currentJourneyId).collect { result ->

                when (result) {
                    is NetworkResult.Success -> {
                        Log.d("WrongPathAPI", "✅ Wrong path alert sent")
                    }

                    is NetworkResult.Error -> {
                        Log.e("WrongPathAPI", "❌ Error: ${result.message}")
                    }


                }
            }
        }
    }
}
/*

@AndroidEntryPoint
class WatchOverMeChooseStartingPointFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentWatchovermechoosestartingpointBinding
    private lateinit var viewModel: WatchOverMeViewModel
    private lateinit var googleMap: GoogleMap
    private var shouldFollowRider = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var placesClient: PlacesClient
    private lateinit var currentLocationAdapter: PlaceAutoSuggestAdapter
    private lateinit var destinationAdapter: PlaceAutoSuggestAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var simulationRunnable: Runnable? = null

    private var riderMarker: Marker? = null
    private var pickupMarker: Marker? = null
    private var destinationMarker: Marker? = null

    // Selected places
    private var pickupLocation: LatLng? = null
    private var destinationLocation: LatLng? = null

    // Autocomplete fragments
    private lateinit var destinationAutocompleteFragment: AutocompleteSupportFragment

    private var correctRefPolyline: Polyline? = null
    private val trailSegments = mutableListOf<Polyline>()
    private var correctRoutePoints: List<LatLng> = listOf()
    private val actualPathPoints = mutableListOf<LatLng>()

    private enum class RouteChoice { NONE, CORRECT }
    private var routeChoice = RouteChoice.NONE
    private var isDelivering = false
    private var currentStepIndex = 0

    private var deliveryStartTime = 0L
    private var wrongRouteStartTime = 0L
    private var totalDistanceMiles = 0.0
    private var wrongDistanceMiles = 0.0

    private var currentSegmentIsCorrect = true
    private val currentSegmentPoints = mutableListOf<LatLng>()
    private var riderOnCorrectRoute = true

    private var deviationStartPoint: LatLng? = null
    private var wrongPathGrowingPolyline: Polyline? = null
    private val currentWrongSegmentPoints = mutableListOf<LatLng>()

    // 🔴 IMPORTANT: 30 meters threshold (0.01864 miles = ~30 meters)
    private val DEVIATION_THRESHOLD_MILES = 30.0 / 1609.34  // ~0.01864 miles (30 meters)
    private val DESTINATION_THRESHOLD_MILES = 5.0 / 1609.34  // ~0.003107 miles

    // ─── Handlers ────────────────────────────────────────────────────
    private val mainHandler = Handler(Looper.getMainLooper())
    private var uiRunnable: Runnable? = null
    private val UI_REFRESH_MS = 500L
    private val LOC_PERM_REQUEST = 100

    // 🔴 NEW: Properties for continuous location updates
    private var currentJourneyId: String = ""
    private var continuousLocationUpdateJob: Job? = null
    private var isUpdatingLocation = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWatchovermechoosestartingpointBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this)[WatchOverMeViewModel::class.java]

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.api_key))
        }
        placesClient = Places.createClient(requireContext())

        currentLocationAdapter = PlaceAutoSuggestAdapter(requireContext(), placesClient)
        destinationAdapter = PlaceAutoSuggestAdapter(requireContext(), placesClient)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupPlaceAutocomplete()
        setupButtons()
        setCurrentLocationInPickup()
        binding.map.onCreate(savedInstanceState)
        binding.map.getMapAsync(this)

        return binding.root
    }

    private fun setCurrentLocationInPickup() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(requireActivity(), Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val addressText = addresses[0].getAddressLine(0)
                            pickupLocation = LatLng(location.latitude, location.longitude)
                            binding.etPickup.setText(addressText)
                            if (::googleMap.isInitialized) updateMapForSelectedPlaces()
                            Toast.makeText(requireActivity(), "Current location set as pickup", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        pickupLocation = LatLng(location.latitude, location.longitude)
                        binding.etPickup.setText("${location.latitude}, ${location.longitude}")
                        if (::googleMap.isInitialized) updateMapForSelectedPlaces()
                    }
                } else {
                    Toast.makeText(requireActivity(), "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOC_PERM_REQUEST)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupPlaceAutocomplete() {
        destinationAutocompleteFragment = AutocompleteSupportFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.destination_autocomplete_container, destinationAutocompleteFragment)
            .commitNow()

        destinationAutocompleteFragment.setPlaceFields(
            Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
            )
        )
        destinationAutocompleteFragment.setHint("Search destination")
        destinationAutocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                destinationLocation = place.latLng
                updateMapForSelectedPlaces()
            }
            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(requireActivity(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupButtons() {
        binding.btnExplore.setOnClickListener {
            if (pickupLocation == null) {
                Toast.makeText(requireActivity(), "Please select pickup location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (destinationLocation == null) {
                Toast.makeText(requireActivity(), "Please select destination", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startJourney( pickupLocation!!.latitude.toString(),
                pickupLocation!!.longitude.toString(),
                destinationLocation!!.latitude.toString(),
                destinationLocation!!.longitude.toString())
            setupRoutesAndStart()
            startDelivery()
        }
    }

    private fun setupRoutesAndStart() {
        if (pickupLocation != null && destinationLocation != null) {
            binding.progressBar?.visibility = View.VISIBLE
            getStreetRoute(pickupLocation!!, destinationLocation!!)
            binding.btnExplore.visibility = View.GONE
        }
    }

    private fun getStreetRoute(start: LatLng, end: LatLng) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val routePoints = getRouteFromDirectionsAPI(start, end)
                withContext(Dispatchers.Main) {
                    binding.progressBar?.visibility = View.GONE
                    if (routePoints.isNotEmpty()) {
                        correctRoutePoints = routePoints
                        drawCorrectReferenceLine()
                        placeMarkers()
                        fitCamera()
                        Toast.makeText(requireActivity(), "Street-accurate route loaded!", Toast.LENGTH_SHORT).show()
                    } else {
                        generateGridRoute(start, end)
                        Toast.makeText(requireActivity(), "Using grid-based route", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar?.visibility = View.GONE
                    generateGridRoute(start, end)
                    Toast.makeText(requireActivity(), "Using grid-based route: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getRouteFromDirectionsAPI(origin: LatLng, destination: LatLng): List<LatLng> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=${origin.latitude},${origin.longitude}" +
                        "&destination=${destination.latitude},${destination.longitude}" +
                        "&mode=driving" +
                        "&key=${getString(R.string.api_key)}" +
                        "&alternatives=false"

                val client = OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "")
                    val routes = json.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        val encodedPolyline = route.getJSONObject("overview_polyline").getString("points")
                        return@withContext decodePolyline(encodedPolyline)
                    }
                }
                emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int; var shift = 0; var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0; result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(LatLng(lat.toDouble() / 1e5, lng.toDouble() / 1e5))
        }
        return poly
    }

    private fun generateGridRoute(start: LatLng, end: LatLng) {
        val points = mutableListOf<LatLng>()
        points.add(start)
        val latDiff = end.latitude - start.latitude
        val lngDiff = end.longitude - start.longitude
        val steps = 20
        for (i in 1 until steps) {
            val fraction = i.toDouble() / steps
            val offset = Math.sin(fraction * Math.PI * 4) * 0.002
            val lat = start.latitude + (latDiff * fraction) + (if (i % 4 == 0) offset else 0.0)
            val lng = start.longitude + (lngDiff * fraction) + (if (i % 4 == 2) offset else 0.0)
            points.add(LatLng(lat, lng))
        }
        points.add(end)
        correctRoutePoints = points
        drawCorrectReferenceLine()
        placeMarkers()
        fitCamera()
    }

    private fun drawCorrectReferenceLine() {
        correctRefPolyline?.remove()
        if (correctRoutePoints.isNotEmpty()) {
            correctRefPolyline = googleMap.addPolyline(
                PolylineOptions()
                    .addAll(correctRoutePoints)
                    .color(Color.parseColor("#2196F3"))
                    .width(8f)
                    .zIndex(1f)
                    .geodesic(false)
                    .clickable(true)
            )
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = false
            mapType = GoogleMap.MAP_TYPE_NORMAL
        }
        checkLocationPermission()

        // 🔴 Disable default my location icon
        try {
            googleMap.setMyLocationEnabled(false)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        if (pickupLocation != null) updateMapForSelectedPlaces()
    }

    private fun updateMapForSelectedPlaces() {
        if (pickupLocation != null && destinationLocation != null) {
            getStreetRoute(pickupLocation!!, destinationLocation!!)
            placeMarkers()
            fitCamera()
        } else if (pickupLocation != null) {
            pickupMarker?.remove()
            pickupMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(pickupLocation!!)
                    .icon(createCustomMarker("Pickup Spot", "0 Min", "0 M"))
                    .anchor(0.5f, 1.5f)
                    .zIndex(3f)
            )
            pickupMarker?.tag = "PICKUP|Not started|0.0 mi"
        } else if (destinationLocation != null) {
            destinationMarker?.remove()
            destinationMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(destinationLocation!!)
                    .icon(createCustomMarker("Destination", "3 Min", "900 M"))
                    .anchor(0.5f, 1.5f)
                    .zIndex(3f)
            )
            destinationMarker?.tag = "DEST|-- min|-- mi"
        }
    }

    private fun updateDestinationMarker(etaMin: Int, distanceStr: String) {
        if (destinationMarker == null || destinationLocation == null || !isAdded) return

        val displayDistance = when {
            distanceStr.contains("mi") -> {
                val miles = distanceStr.replace(" mi", "").toDoubleOrNull() ?: 0.0
                if (miles < 0.1) {
                    "${(miles * 1609.34).toInt()} M"
                } else {
                    "${String.format("%.1f", miles)} Mi"
                }
            }
            else -> distanceStr
        }

        val newIcon = createCustomMarker("Destination", "$etaMin Min", displayDistance)
        if (newIcon != null) {
            destinationMarker?.setIcon(newIcon)
            destinationMarker?.tag = "DEST|$etaMin min|$displayDistance"
        }
    }

    private fun placeMarkers() {
        if (pickupLocation != null) {
            pickupMarker?.remove()

            val (initialDistance, initialTime) = if (destinationLocation != null) {
                val dist = distanceMiles(pickupLocation!!, destinationLocation!!)
                val time = (dist / 15 * 60).toInt()
                Pair(formatMiles(dist), "$time Min")
            } else {
                Pair("0 M", "0 Min")
            }

            pickupMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(pickupLocation!!)
                    .icon(createCustomMarker("Pickup Spot", initialTime, initialDistance))
                    .anchor(0.5f, 1.5f)
                    .zIndex(3f)
            )
            pickupMarker?.tag = "PICKUP|Not started|0.0 mi"
        }

        if (destinationLocation != null) {
            destinationMarker?.remove()
            destinationMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(destinationLocation!!)
                    .icon(createCustomMarker("Destination", "-- Min", "-- M"))
                    .anchor(0.5f, 1.5f)
                    .zIndex(3f)
            )
            destinationMarker?.tag = "DEST|-- min|-- mi"
        }
    }

    private fun fitCamera() {
        if (pickupLocation != null && destinationLocation != null) {
            val bounds = LatLngBounds.Builder()
                .include(pickupLocation!!).include(destinationLocation!!).build()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
        } else if (pickupLocation != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation!!, 14f))
        }
    }

    private fun startDelivery() {
        routeChoice = RouteChoice.CORRECT
        isDelivering = true
        currentStepIndex = 0
        deliveryStartTime = System.currentTimeMillis()
        wrongRouteStartTime = 0L
        totalDistanceMiles = 0.0
        wrongDistanceMiles = 0.0
        riderOnCorrectRoute = true

        deviationStartPoint = null
        wrongPathGrowingPolyline?.remove()
        wrongPathGrowingPolyline = null
        currentWrongSegmentPoints.clear()

        actualPathPoints.clear()
        trailSegments.forEach { it.remove() }
        trailSegments.clear()
        currentSegmentPoints.clear()
        currentSegmentIsCorrect = true

        if (pickupLocation != null) {
            currentSegmentPoints.add(pickupLocation!!)
            actualPathPoints.add(pickupLocation!!)

            riderMarker?.remove()

            // 🔴 Always start with correct path icon
            riderMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(pickupLocation!!)
                    .icon(getCorrectPathIcon())
                    .zIndex(5f)
                    .anchor(0.5f, 0.5f)
            )
        }

        toast("✅ Delivery started! Stay on the green path")
        startRealTimeTracking()
        startUiLoop()
    }

    // 🔴 Helper function to get correct path icon
    private fun getCorrectPathIcon(): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_curretpathicon)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 90, 90, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    // 🔴 Helper function to get wrong path icon
    private fun getWrongPathIcon(): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_wrongpathicon)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 90, 90, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    private fun startRealTimeTracking() {
        if (correctRoutePoints.isEmpty()) return

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L  // 3 seconds interval - more responsive
        ).apply {
            setMinUpdateIntervalMillis(1000L)  // 1 second min interval
            setMaxUpdateDelayMillis(5000L)     // 5 seconds max delay
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentLoc = LatLng(location.latitude, location.longitude)

                    // 🔴 Process real location only - no simulation
                    processLiveLocation(currentLoc)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun processLiveLocation(currentLoc: LatLng) {
        // 1. Destination check
        if (destinationLocation != null) {
            val distToDest = distanceMiles(currentLoc, destinationLocation!!)
            if (distToDest <= DESTINATION_THRESHOLD_MILES) {
                riderMarker?.position = destinationLocation!!
                onArrived()
                stopAllUpdates()
                return
            }
        }

        // 2. Route deviation check - 🔴 Using 30 meters threshold
        val isDeviating = checkForDeviation(currentLoc)

        if (isDeviating) {
            handleWrongPath(currentLoc)
        } else {
            handleCorrectPath(currentLoc)

            // Find nearest point on route for progress tracking
            val nearestPointInfo = findNearestPointOnRoute(currentLoc)
            currentStepIndex = nearestPointInfo.second
        }

        // 3. Update UI
        updatePathStatusUI(isDeviating)

        // 🔴 Update marker icon based on deviation status
        updateRiderIconBasedOnDeviation(isDeviating)

        riderMarker?.position = currentLoc
        actualPathPoints.add(currentLoc)
        drawActualPath()

        // 4. Camera follow
        if (shouldFollowRider) {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(currentLoc, 17f),
                500,
                null
            )
        }
    }

    // 🔴 New function to update icon based on deviation
    private fun updateRiderIconBasedOnDeviation(isDeviating: Boolean) {
        val icon = if (isDeviating) getWrongPathIcon() else getCorrectPathIcon()
        riderMarker?.setIcon(icon)
    }

    private fun findNearestPointOnRoute(currentLoc: LatLng): Pair<LatLng, Int> {
        var minDistance = Double.MAX_VALUE
        var nearestPoint = correctRoutePoints.first()
        var nearestIndex = 0

        correctRoutePoints.forEachIndexed { index, point ->
            val distance = distanceMiles(currentLoc, point)
            if (distance < minDistance) {
                minDistance = distance
                nearestPoint = point
                nearestIndex = index
            }
        }

        return Pair(nearestPoint, nearestIndex)
    }

    // 🔴 Updated: Stop all updates (location tracking + continuous API calls)
    private fun stopAllUpdates() {
        stopContinuousLocationUpdates()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // 🔴 Deviation Check - 30 meters threshold
    private fun checkForDeviation(currentLocation: LatLng): Boolean {
        var minDistance = Double.MAX_VALUE
        for (point in correctRoutePoints) {
            val distance = distanceMiles(currentLocation, point)
            if (distance < minDistance) minDistance = distance
        }

        // 🔴 Return true if distance > 30 meters (0.01864 miles)
        return minDistance > DEVIATION_THRESHOLD_MILES
    }

    private fun handleWrongPath(location: LatLng) {
        if (riderOnCorrectRoute) {
            riderOnCorrectRoute = false
            currentSegmentIsCorrect = false
            wrongRouteStartTime = System.currentTimeMillis()

            deviationStartPoint = if (actualPathPoints.isNotEmpty()) {
                actualPathPoints.last()
            } else {
                location
            }

            currentWrongSegmentPoints.clear()
            deviationStartPoint?.let { currentWrongSegmentPoints.add(it) }
            currentWrongSegmentPoints.add(location)

            if (currentSegmentPoints.size > 1) {
                commitCurrentSegment(true)
            }
        } else {
            currentWrongSegmentPoints.add(location)
        }

        drawGrowingWrongPath()

        if (currentWrongSegmentPoints.size > 1) {
            val prevLoc = currentWrongSegmentPoints[currentWrongSegmentPoints.size - 2]
            wrongDistanceMiles += distanceMiles(prevLoc, location)
        }
    }

    private fun drawGrowingWrongPath() {
        if (currentWrongSegmentPoints.size < 2) return

        wrongPathGrowingPolyline?.remove()

        wrongPathGrowingPolyline = googleMap.addPolyline(
            PolylineOptions()
                .addAll(currentWrongSegmentPoints)
                .color(Color.RED)
                .width(14f)
                .pattern(listOf(Dash(30f), Gap(10f)))
                .zIndex(10f)
                .geodesic(true)
        )
    }

    private fun handleCorrectPath(location: LatLng) {
        if (!riderOnCorrectRoute) {
            riderOnCorrectRoute = true
            currentSegmentIsCorrect = true

            if (currentWrongSegmentPoints.size > 1) {
                val permanentWrongPoly = googleMap.addPolyline(
                    PolylineOptions()
                        .addAll(currentWrongSegmentPoints)
                        .color(Color.RED)
                        .width(12f)
                        .pattern(listOf(Dash(20f), Gap(8f)))
                        .zIndex(3f)
                        .geodesic(false)
                )
                trailSegments.add(permanentWrongPoly)
            }

            wrongPathGrowingPolyline?.remove()
            wrongPathGrowingPolyline = null

            deviationStartPoint = null
            currentWrongSegmentPoints.clear()

            if (currentSegmentPoints.size > 1) {
                commitCurrentSegment(false)
            }
            currentSegmentPoints.clear()
            currentSegmentPoints.add(location)
        } else {
            currentSegmentPoints.add(location)
        }

        if (currentSegmentPoints.size > 1) {
            val prevLoc = currentSegmentPoints[currentSegmentPoints.size - 2]
            totalDistanceMiles += distanceMiles(prevLoc, location)
        }
    }

    private fun drawActualPath() {
        liveSegmentPolyline?.remove()
        if (actualPathPoints.size < 2) return

        if (!riderOnCorrectRoute) return

        liveSegmentPolyline = googleMap.addPolyline(
            PolylineOptions()
                .addAll(actualPathPoints)
                .color(Color.parseColor("#1ABC9C"))
                .width(12f)
                .zIndex(4f)
                .geodesic(false)
        )
    }

    private var liveSegmentPolyline: Polyline? = null

    private fun updatePathStatusUI(isDeviating: Boolean) {
        // Optional: Update UI text if needed
    }

    private fun commitCurrentSegment(wasCorrect: Boolean) {
        if (currentSegmentPoints.size < 2) { currentSegmentPoints.clear(); return }
        val poly = googleMap.addPolyline(buildSegmentOptions(wasCorrect, currentSegmentPoints))
        trailSegments.add(poly)
        currentSegmentPoints.clear()
    }

    private fun buildSegmentOptions(isCorrect: Boolean, points: List<LatLng>): PolylineOptions {
        return if (isCorrect) {
            PolylineOptions().addAll(points).color(Color.parseColor("#1ABC9C")).width(9f).zIndex(3f).geodesic(false)
        } else {
            PolylineOptions().addAll(points).color(Color.RED).width(9f)
               /* .pattern(listOf(Dash(20f), Gap(10f)))*/.zIndex(3f).geodesic(false)
        }
    }

    private fun startUiLoop() {
        uiRunnable = object : Runnable {
            override fun run() {
                if (!isDelivering) return
                val now = System.currentTimeMillis()

                val elapsedSec = (now - deliveryStartTime) / 1000L
                val fromTimeStr = when {
                    elapsedSec < 60 -> "${elapsedSec}s"
                    else -> "${elapsedSec / 60}min ${elapsedSec % 60}s"
                }
                val fromDistStr = formatMiles(totalDistanceMiles)

                updatePickupMarker(fromTimeStr, fromDistStr)

                if (correctRoutePoints.isNotEmpty() && destinationLocation != null) {
                    val currLoc = riderMarker?.position ?: correctRoutePoints[currentStepIndex.coerceAtMost(correctRoutePoints.size - 1)]

                    val distToDest = distanceMiles(currLoc, destinationLocation!!)
                    val etaMin = (distToDest / 15 * 60).toInt()
                    val toDistStr = formatDistanceWithUnit(distToDest)

                    updateDestinationMarker(etaMin, toDistStr)
                }

                mainHandler.postDelayed(this, UI_REFRESH_MS)
            }
        }
        mainHandler.post(uiRunnable!!)
    }

    private fun updatePickupMarker(timeStr: String, distanceStr: String) {
        if (pickupMarker == null || pickupLocation == null || !isAdded) return
        val displayDistance = when {
            distanceStr.contains("mi") -> {
                val miles = distanceStr.replace(" mi", "").toDoubleOrNull() ?: 0.0
                if (miles < 0.1) {
                    "${(miles * 1609.34).toInt()} M"
                } else {
                    "${String.format("%.2f", miles)} Mi"
                }
            }
            else -> distanceStr
        }

        val newIcon = createCustomMarker("Pickup Spot", timeStr, displayDistance)
        if (newIcon != null) {
            pickupMarker?.setIcon(newIcon)
            pickupMarker?.tag = "PICKUP|$timeStr|$displayDistance"
        }
    }

    private fun formatDistanceWithUnit(distanceMiles: Double): String {
        return when {
            distanceMiles < 0.1 -> {
                val meters = (distanceMiles * 1609.34).toInt()
                "${meters} M"
            }
            distanceMiles < 1.0 -> {
                String.format("%.2f Mi", distanceMiles)
            }
            else -> {
                String.format("%.1f Mi", distanceMiles)
            }
        }
    }

    private fun onArrived() {
        isDelivering = false
        stopLoops()
        stopAllUpdates() // 🔴 Stop all updates including continuous API calls

        if (!riderOnCorrectRoute && currentWrongSegmentPoints.size > 1) {
            val finalWrongPoly = googleMap.addPolyline(
                PolylineOptions()
                    .addAll(currentWrongSegmentPoints)
                    .color(Color.RED).width(12f)
                    .pattern(listOf(Dash(20f), Gap(8f))).zIndex(3f).geodesic(false)
            )
            trailSegments.add(finalWrongPoly)
            wrongPathGrowingPolyline?.remove()
            wrongPathGrowingPolyline = null
        }

        destinationLocation?.let { riderMarker?.position = it }

        drawActualPath()

        val totalMin = (System.currentTimeMillis() - deliveryStartTime) / 60000
        val totalDist = formatMiles(totalDistanceMiles)

        val message = if (wrongDistanceMiles > 0) {
            val wrongDist = formatMiles(wrongDistanceMiles)
            "⚠️ Delivery completed!\nWrong path: $wrongDist\nTotal distance: $totalDist in ${totalMin}min"
        } else {
            "✅ Perfect delivery!\nYou stayed on correct path!\nTotal distance: $totalDist in ${totalMin}min"
        }

        toast(message)
    }

    private fun stopLoops() {
        uiRunnable?.let { mainHandler.removeCallbacks(it) }
        uiRunnable = null
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOC_PERM_REQUEST)
        } else {
            enableMyLocation()
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // 🔴 Don't enable default my location - we use custom marker
            // googleMap.isMyLocationEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOC_PERM_REQUEST
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
            setCurrentLocationInPickup()
        }
    }

    private fun distanceMiles(point1: LatLng, point2: LatLng): Double {
        val R = 3959.0
        val latDistance = Math.toRadians(point2.latitude - point1.latitude)
        val lonDistance = Math.toRadians(point2.longitude - point1.longitude)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(point1.latitude)) *
                Math.cos(Math.toRadians(point2.latitude)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun formatMiles(miles: Double): String = String.format("%.1f mi", miles)

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
        // Don't stop updates on pause, let them continue
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAllUpdates() // 🔴 Stop all updates
        stopLoops()
        simulationRunnable?.let { handler.removeCallbacks(it) }
        riderMarker = null
        pickupMarker = null
        destinationMarker = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

    private fun createCustomMarker(title: String, time: String = "", distance: String = ""): BitmapDescriptor? {
        if (!isAdded) {
            Log.e("MarkerError", "Fragment not attached, cannot create marker")
            return null
        }
        return try {
            val markerView =
                LayoutInflater.from(requireContext()).inflate(R.layout.layout_marker_info, null)

            val tvTimeView = markerView.findViewById<TextView>(R.id.tvTime)
            val tvDistanceView = markerView.findViewById<TextView>(R.id.tvDistance)
            val tvTitleView = markerView.findViewById<TextView>(R.id.tvTitle)

            Log.d("MarkerDebug", "tvTime found: ${tvTimeView != null}")
            Log.d("MarkerDebug", "tvDistance found: ${tvDistanceView != null}")
            Log.d("MarkerDebug", "tvTitle found: ${tvTitleView != null}")

            tvTimeView?.text = time
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

    private fun startJourney(
        currentLatitude : String,
        currentLongitude : String,
        destinationLatitude : String,
        destinationLongitude : String) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.startJourney(currentLatitude,currentLongitude,destinationLatitude,destinationLongitude).collect {
                    BaseApplication.dismissDialog()
                    try {
                        Log.d("@@@ addMea List ", "data :- $it")
                        if (it.data!!.code == 200 && it.data!!.status) {
                            val journeyId = it.data?.data?.journey_id
                            if (journeyId != null) {
                                currentJourneyId = journeyId.toString()
                                // 🔴 Start continuous location updates instead of periodic WorkManager
                                startContinuousLocationUpdates(currentJourneyId)
                            }
                            showAlert(it.data!!.message, false)
                        } else {
                            handleError(it.data!!.code, it.data!!.message)
                        }
                    } catch (e: Exception) {
                        showAlert(e.message, false)
                    }
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    // This is common function for show the alert box
    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(context, message, status)
    }

    private fun handleError(code: Int?, message: String?) {
        if (code == MessageClass.deactivatedUser || code == MessageClass.deletedUser) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    // 🔴 NEW: Continuous location updates using coroutine (every 5 seconds)
    private fun startContinuousLocationUpdates(journeyId: String) {
        Log.d("RetrofitLog", "🚀 Starting CONTINUOUS location updates for journey: $journeyId")

        currentJourneyId = journeyId
        isUpdatingLocation = true

        // Cancel any existing job
        continuousLocationUpdateJob?.cancel()

        // Start continuous updates using coroutine
        continuousLocationUpdateJob = lifecycleScope.launch {
            while (isUpdatingLocation) {
                try {
                    // Get current location from marker
                    val currentLocation = riderMarker?.position

                    if (currentLocation != null) {
                        Log.d("RetrofitLog", "📍 Current location: (${currentLocation.latitude}, ${currentLocation.longitude})")

                        // Call API directly using coroutine
                        updateLiveLocationDirectly(
                            journeyId = journeyId,
                            token = getAuthToken(),
                            latitude = currentLocation.latitude,
                            longitude = currentLocation.longitude
                        )
                    } else {
                        Log.w("RetrofitLog", "⚠️ Rider marker location is null")
                    }

                    // Wait for 5 seconds
                    delay(5000)

                } catch (e: Exception) {
                    Log.e("RetrofitLog", "❌ Error in continuous update loop: ${e.message}")
                    e.printStackTrace()
                    // Still wait and continue
                    delay(5000)
                }
            }
        }

        Log.d("RetrofitLog", "✅ Continuous updates coroutine started")
    }

    // Direct API call function
    private suspend fun updateLiveLocationDirectly(
        journeyId: String,
        token: String,
        latitude: Double,
        longitude: Double
    ) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("RetrofitLog", "📡 Making direct API call at ${System.currentTimeMillis()}")

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val json = JSONObject().apply {
                    put("journey_id", journeyId)
                    put("latitude", latitude.toString())
                    put("longitude", longitude.toString())
                }

                val request = Request.Builder()
                    .url("https://alertapp.tgastaging.com/api/update_live_location")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()

                Log.d("RetrofitLog", "📥 Response Code: ${response.code}")

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("RetrofitLog", "✅ API Success: $responseBody")
                } else {
                    Log.e("RetrofitLog", "❌ API Failed: ${response.code}")
                }

            } catch (e: Exception) {
                Log.e("RetrofitLog", "❌ Network Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Stop function for continuous updates
    private fun stopContinuousLocationUpdates() {
        Log.d("RetrofitLog", "🛑 Stopping continuous location updates")
        isUpdatingLocation = false
        continuousLocationUpdateJob?.cancel()
        continuousLocationUpdateJob = null
    }

    // Function to get auth token
    private fun getAuthToken(): String {
        return SessionManagement(requireActivity()).getUserToken().toString()
    }
}
 */