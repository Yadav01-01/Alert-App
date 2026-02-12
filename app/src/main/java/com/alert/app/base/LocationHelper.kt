package com.alert.app.base


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.util.Locale

class LocationHelper (
    private val activity: Activity
) {
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(activity)

    private var locationCallback: ((Double, Double, String) -> Unit)? = null

    companion object {
        const val LOCATION_PERMISSION_REQUEST = 1001
    }

    fun getCurrentLocation(callback: (Double, Double, String) -> Unit) {
        this.locationCallback = callback

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        fetchLocation()
    }

    private fun fetchLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                handleLocation(it.latitude, it.longitude)
            } ?: requestNewLocation()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun requestNewLocation() {

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000
        ).setMaxUpdates(1).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation
                    handleLocation(location?.latitude?:0.0, location?.longitude?:0.0)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            },
            activity.mainLooper
        )
    }

    private fun handleLocation(lat: Double, lng: Double) {

        val addressName = try {
            val geocoder = Geocoder(activity, Locale.getDefault())
            val list = geocoder.getFromLocation(lat, lng, 1)
            list?.get(0)?.getAddressLine(0) ?: ""
        } catch (e: Exception) {
            ""
        }

        locationCallback?.invoke(lat, lng, addressName)
    }
}