package com.alert.app.fragment.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText

import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.BuildConfig

import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.base.BaseApplication
import com.alert.app.base.BaseApplication.alertError
import com.alert.app.databinding.FragmentFromMapBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.OnClickEventDropDownType

import com.alert.app.model.TimeModel
import com.alert.app.model.contact.AddContactResponse
import com.alert.app.model.contact.AlertsResponse
import com.alert.app.model.contact.RelationResponse
import com.alert.app.model.contact.UserContactRequest
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch


import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.adapter.TimeArrayCustomListAdapter

import com.alert.app.base.BaseApplication.alertError
import com.alert.app.errormessage.AlertUtils

import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.model.contact.AddContactResponse1
import com.alert.app.model.helpingneighbormodel.AddNeighborModel
import com.alert.app.model.helpingneighbormodel.CreateHelpingNeighbor

import com.alert.app.model.map.UserLocationResponse
import com.google.gson.JsonObject
import com.hbb20.CountryCodePicker
import kotlinx.coroutines.launch

/*@AndroidEntryPoint
class FromMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentFromMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    private val viewModel: MapViewModel by viewModels()
    private var currentLocationMarker: Marker? = null
    private var type: String = ""

    private var selectedAlertId = -1
    private var selectedRelationId = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFromMapBinding.inflate(inflater, container, false)

        mapView = binding.map
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        initView()
        return binding.root
    }

    private fun initView() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) enableMyLocation()
                else Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }

        type = arguments?.getString("type", "") ?: ""

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )
    }

    // ================= MAP READY ==================

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        loadUsersOnMap()

        googleMap.setOnInfoWindowClickListener { marker ->
            val user = marker.tag as? UserData
            addAlert(user)
        }
    }

    // ================= LOCATION ==================

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        googleMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                showCurrentLocationMarker(latLng)
            }
        }
    }

    private fun showCurrentLocationMarker(latLng: LatLng) {
        currentLocationMarker?.remove()

        currentLocationMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(getBitmapDescriptor(R.drawable.your_location))
                .anchor(0.5f, 0.5f)
        )

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    // ================= API → MAP ==================

    private fun loadUsersOnMap() {
        if (!BaseApplication.isOnline(requireContext())) {
            showAlert(requireContext(), MessageClass.networkError, false)
            return
        }

        BaseApplication.openDialog()

        lifecycleScope.launch {
            viewModel.mapLocations().collect { result ->
                BaseApplication.dismissDialog()

                when (result) {
                    is NetworkResult.Success -> {
                        val response = result.data
                        if (response?.status == true) {
                            response.data.forEach { user ->
                                if (user.latitude != "0" && user.longitude != "0") {

                                    val latLng = LatLng(
                                        user.latitude?.toDouble()?:0.000,
                                        user.longitude?.toDouble()?:0.000
                                    )

                                    createMarkerFromView(
                                        requireContext(),
                                        BuildConfig.BASE_URL + user.profilePic
                                    ) { icon ->
                                        val marker = googleMap.addMarker(
                                            MarkerOptions()
                                                .position(latLng)
                                                .icon(icon)
                                                .anchor(0.5f, 1f)
                                        )
                                        marker?.tag = user
                                    }
                                }
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // ================= CUSTOM MARKER ==================

    private fun getBitmapDescriptor(@DrawableRes id: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(requireContext(), id)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun createMarkerFromView(
        context: Context,
        imageUrl: String,
        callback: (BitmapDescriptor) -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_marker, null)
        val img = view.findViewById<CircleImageView>(R.id.imgProfile)

        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .placeholder(R.drawable.marker_demmy_pic)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    img.setImageBitmap(resource)

                    view.measure(
                        View.MeasureSpec.UNSPECIFIED,
                        View.MeasureSpec.UNSPECIFIED
                    )
                    view.layout(0, 0, view.measuredWidth, view.measuredHeight)

                    val bitmap = Bitmap.createBitmap(
                        view.measuredWidth,
                        view.measuredHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    view.draw(canvas)

                    callback(BitmapDescriptorFactory.fromBitmap(bitmap))
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    // ================= ADD ALERT ==================

    private fun addAlert(user: UserData?) {
        if (user == null) return

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_addalert)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val btnOkay = dialog.findViewById<TextView>(R.id.btnokay)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)

        btnOkay.setOnClickListener {
            if (user.email.isNullOrEmpty()) {
                alertError(requireContext(), MessageClass.emailError, false)
                return@setOnClickListener
            }
            dialog.dismiss()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    // ================= MAPVIEW LIFECYCLE ==================

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}*/

/*
@AndroidEntryPoint
class FromMapFragment : Fragment(), OnClickEventDropDownType, OnMapReadyCallback {


    private lateinit var binding: FragmentFromMapBinding
    private var type:String=""

    val data: MutableList<TimeModel> = mutableListOf()

    lateinit var popupWindow: PopupWindow
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null
    private val viewModel: MapViewModel by viewModels()

    private var selectedAlertId = -1
    private var selectedRelationId = -1



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFromMapBinding.inflate(layoutInflater, container, false)
        mapView = binding.map
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        initView()
        return binding.root
    }

    private fun initView() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        type=arguments?.getString("type","").toString()

        val mainActivity = requireActivity() as? MainActivity
        if (type.equals("helpingNeighbors", true) || type.equals("addContact", true)) {
            mainActivity?.setImageShowTv()?.visibility = View.GONE
            mainActivity?.setImgChatBoot()?.visibility = View.GONE
            binding.shadow.root.visibility = View.VISIBLE
        }

        // This line use for system back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

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


    private fun showCustomLocationMarker(latLng: LatLng) {
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



    private fun addAlert(user: UserData?) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_addalert)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams
        val btnOkay = dialog.findViewById<TextView>(R.id.btnokay)
        val tvRelation = dialog.findViewById<MaterialAutoCompleteTextView>(R.id.tvRelation)
        val tvAlerts = dialog.findViewById<MaterialAutoCompleteTextView>(R.id.tvAlerts)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
/*
  val dataAdapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, data)
        tvRelation.setAdapter<ArrayAdapter<String>>(dataAdapter)
*/
        val stringList = data.map { it.toString() } // or use a specific property like it.timeText

        val dataAdapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, stringList)
        tvRelation.setAdapter(dataAdapter)

tvRelation.setOnClickListener {
            val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = inflater?.inflate(R.layout.item_select_layout, null)
            popupWindow = PopupWindow(popupView, tvRelation.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAsDropDown(tvRelation,  0, 0, Gravity.CENTER)

            // Access views inside the inflated layout using findViewById
            val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)

            rcyData?.adapter= TimeArrayCustomListAdapter(requireContext(),data,this,"time")


            tvRelation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.left_arrow_top, 0)
            // Set the dismiss listener
            popupWindow.setOnDismissListener {
                tvRelation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_bottom, 0)
            }


        }




        btnOkay.setOnClickListener {
            user?.let {
                if (it.email.isNullOrEmpty()){
                    alertError(requireContext(), MessageClass.emailError,false)
                }
                else if (selectedRelationId==-1){
                    alertError(requireContext(), MessageClass.relation,false)
                }
                else if (selectedAlertId==-1){
                    alertError(requireContext(), MessageClass.alert,false)
                }else{
                    val userContactRequest = UserContactRequest(it.name,
                        "",
                        it.email,
                        it.phone_number,
                        selectedRelationId,
                        selectedAlertId,
                        "map",
                        "map")
                    addContact(userContactRequest,dialog)
                }
            }


        }



        imgClose.setOnClickListener {
            dialog.dismiss()
            if (type.equals("helpingNeighbors",true) || type.equals("addContact",true) ){
                findNavController().navigateUp()

            }else{
                findNavController().navigate(R.id.contactFragment)

            }
        }

        getRelation(tvRelation)
        getAllAlerts(tvAlerts)
    }

    private fun addContact(userContactRequest: UserContactRequest, dialog: Dialog) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.addContact(userContactRequest).collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val addContactResponse =
                                    Gson().fromJson(it, AddContactResponse::class.java)
                                if (addContactResponse.code==200) {
                                    dialog.dismiss()
                                    if (type.equals("helpingNeighbors",true) || type.equals("addContact",true)){
                                        findNavController().navigateUp()
                                    }else{
                                        findNavController().navigate(R.id.contactFragment)
                                    }
                                    alertBoxSuccess()
                                }else{
                                    Toast.makeText(
                                        requireContext(),
                                        addContactResponse.message,
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

    private fun getRelation(tv_relation: MaterialAutoCompleteTextView) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getRelation().collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val relationResponse =
                                    Gson().fromJson(it, RelationResponse::class.java)
                                if (relationResponse.code==200) {
                                    val relationList = relationResponse.data
                                    // Extract names
                                    val relationNames = relationList.map { it.name }
                                    // Set to dropdown
                                    val adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_dropdown_item_1line,
                                        relationNames
                                    )
                                    tv_relation.setAdapter(adapter)
                                    // Handle click
                                    tv_relation.setOnItemClickListener { parent, view, position, id ->
                                        val selectedAlert = relationList[position]
                                        selectedRelationId = selectedAlert.id
                                    }
                                }else{
                                    Toast.makeText(
                                        requireContext(),
                                        relationResponse.message,
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

    private fun getAllAlerts(tvAllAlerts: MaterialAutoCompleteTextView) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getAllAlerts().collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val alertsResponse =
                                    Gson().fromJson(it, AlertsResponse::class.java)
                                if (alertsResponse.code==200) {
                                    val alerts = alertsResponse.data
                                    // Extract names
                                    val alertsTitle = alerts.map { it.title }
                                    // Set to dropdown
                                    val adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_dropdown_item_1line,
                                        alertsTitle
                                    )
                                    tvAllAlerts.setAdapter(adapter)
                                    // Handle click
                                    tvAllAlerts.setOnItemClickListener { parent, view, position, id ->
                                        val selectedAlert = alerts[position]
                                        selectedAlertId = selectedAlert.id
                                    }
                                }else{
                                    Toast.makeText(
                                        requireContext(),
                                        alertsResponse.message,
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

    private fun alertBoxSuccess(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_success)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams

        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        val tvOK = dialog.findViewById<TextView>(R.id.tvOK)
        val text = dialog.findViewById<TextView>(R.id.tv_text)

        text.text = "Your Contact has been added\nSuccessfully."

        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvOK.setOnClickListener {
            dialog.dismiss()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun onClickDropDown(pos: String?, type: String?) {
        if (type.equals("time")){
            for (i in data.indices) {
                val item = data[i].copy() // Create a copy to avoid modifying the reference at `position`
                item.name = data[i].name ?: ""
                // Set the status based on position
                item.status = i == pos?.toInt()
                // Update the item in the list
                data[i] = item
            }
            // Set the text of the category at 'position'
           // tvRelation.text = data[pos?.toInt()!!].name
        }


        popupWindow.dismiss()
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        // Optional: Automatically ask permission if not already granted
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()

        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        googleMap.setOnInfoWindowClickListener { marker ->
            // Handle click for the whole InfoWindow
            val user = marker.tag as? UserData
            addAlert(user)
        }
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
}*/
@AndroidEntryPoint
class FromMapFragment : Fragment(), OnClickEventDropDownType, OnMapReadyCallback {

    private lateinit var binding: FragmentFromMapBinding
    private var type: String = ""

    val data: MutableList<TimeModel> = mutableListOf()

    lateinit var popupWindow: PopupWindow
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null
    private val viewModel: MapViewModel by viewModels()

    private var selectedAlertId = -1
    private var selectedRelationId = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFromMapBinding.inflate(layoutInflater, container, false)
        mapView = binding.map
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        initView()
        return binding.root
    }

    private fun initView() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        type = arguments?.getString("type", "").toString()

        val mainActivity = requireActivity() as? MainActivity
        if (type.equals("helpingNeighbors", true) || type.equals("addContact", true)) {
            mainActivity?.setImageShowTv()?.visibility = View.GONE
            mainActivity?.setImgChatBoot()?.visibility = View.GONE
            binding.shadow.root.visibility = View.VISIBLE
        }

        // This line use for system back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
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
                    if (user != null) {
                        tvSnippet.text = "Connect with ${user.name}"
                    } else {
                        return null
                    }
                    return view
                }
            })

            // Set marker click listener
            googleMap.setOnMarkerClickListener { marker ->
                val user = marker.tag as? UserData
                if (user != null) {
                    // Show toast with user details
                    val userDetails = """
                        Name: ${user.name}
                        Email: ${user.email}
                        Phone: ${user.phone_number}
                        Distance: ${user.distance} km
                    """.trimIndent()

                    Toast.makeText(requireContext(), userDetails, Toast.LENGTH_LONG).show()

                    // Log user details
                    Log.d("UserMarkerClick", "User ID: ${user.id}")
                    Log.d("UserMarkerClick", "Name: ${user.name}")
                    Log.d("UserMarkerClick", "Email: ${user.email}")
                    Log.d("UserMarkerClick", "Phone: ${user.phone_number}")
                    Log.d("UserMarkerClick", "Latitude: ${user.latitude}")
                    Log.d("UserMarkerClick", "Longitude: ${user.longitude}")
                    Log.d("UserMarkerClick", "Distance: ${user.distance} km")

                    Log.d("UserMarkerClick", "Address: ${user.address}")
                }
                false
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    getNearbyUser(location.latitude.toString(), location.longitude.toString())
                    val latLng = LatLng(location.latitude, location.longitude)
                    showCustomLocationMarker(latLng)
                } else {
                    Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getNearbyUser(latitude: String, longitude: String) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getNearbyUser(latitude, longitude).collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val nearbyUsersResponse = Gson().fromJson(it, NearbyUsersResponse::class.java)
                                if (nearbyUsersResponse.code == 200) {
                                    val users = nearbyUsersResponse.data
                                    // Clear existing markers (except current location)
                                    googleMap.clear()
                                    // Re-add current location marker
                                    if (ActivityCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        // TODO: Consider calling
                                        //    ActivityCompat#requestPermissions
                                        // here to request the missing permissions, and then overriding
                                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                        //                                          int[] grantResults)
                                        // to handle the case where the user grants the permission. See the documentation
                                        // for ActivityCompat#requestPermissions for more details.
                                        null
                                    }
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        if (location != null) {
                                            showCustomLocationMarker(LatLng(location.latitude, location.longitude))
                                        }
                                    }

                                    users.forEach { user ->
                                        user.latitude?.let { lat ->
                                            user.longitude?.let { lon ->
                                                // Create marker for each user based on latitude and longitude
                                                createMarkerFromView(
                                                    requireContext(),
                                                    if (!user.profile_image.isNullOrEmpty()) {
                                                        if (user.profile_image.startsWith("http")) {
                                                            user.profile_image
                                                        } else {
                                                            BuildConfig.Media_URL +  user.profile_image
                                                        }
                                                    } else null,

                                                    { icon ->
                                                        val marker = googleMap.addMarker(
                                                            MarkerOptions()
                                                                .position(LatLng(lat.toDouble(), lon.toDouble()))
                                                                .icon(icon)
                                                                .anchor(0.5f, 1f)
                                                                .title(user.name) // Set title for marker
                                                              //  .snippet("Distance: ${String.format("%.2f", user.distance)} km") // Set snippet
                                                                .snippet("Distance: ${String.format("%.2f", user.distance?.toDoubleOrNull() ?: 0.0)} km")
                                                        )
                                                        marker?.tag = user

                                                        // Log user marker placement
                                                        Log.d("UserMarker", "Marker placed for user: ${user.name}")
                                                        Log.d("UserMarker", "Location: ($lat, $lon)")
                                                        Log.d("UserMarker", "Distance: ${user.distance} km")
                                                        Log.d("UserMarker", "user name: ${user.name}")
                                                        Log.d("UserMarker", "email: ${user.email}")
                                                        Log.d("UserMarker", "image: ${BuildConfig.BASE_URL + user.profile_image}")
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else {
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
        } else {
            showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun showCustomLocationMarker(latLng: LatLng) {
        // Remove previous marker if exists
        currentLocationMarker?.remove()
        // Add your custom marker icon
        val markerOptions = MarkerOptions()
            .position(latLng)
            .icon(getBitmapDescriptor(R.drawable.your_location))
            .anchor(0.5f, 0.5f)
            .title("Your Location")
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

    fun createMarkerFromView(context: Context, imageUrl: String?, callback: (BitmapDescriptor) -> Unit) {
        val markerView = LayoutInflater.from(context).inflate(R.layout.custom_marker, null)
        val profileImage = markerView.findViewById<CircleImageView>(R.id.imgProfile)

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .placeholder(R.drawable.marker_demmy_pic)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        profileImage.setImageBitmap(resource)
                        convertViewToBitmap(markerView, callback)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        profileImage.setImageResource(R.drawable.marker_demmy_pic)
                        convertViewToBitmap(markerView, callback)
                    }
                })
        } else {
            profileImage.setImageResource(R.drawable.marker_demmy_pic)
            convertViewToBitmap(markerView, callback)
        }
    }

    private fun convertViewToBitmap(view: View, callback: (BitmapDescriptor) -> Unit) {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(measureSpec, measureSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth, view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        callback(BitmapDescriptorFactory.fromBitmap(bitmap))
    }

    private fun addAlert(user: UserData?) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_addalert)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams
        val btnOkay = dialog.findViewById<TextView>(R.id.btnokay)
        val tvRelation = dialog.findViewById<MaterialAutoCompleteTextView>(R.id.tvRelation)
        val tvAlerts = dialog.findViewById<MaterialAutoCompleteTextView>(R.id.tvAlerts)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        val stringList = data.map { it.toString() }
        val dataAdapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, stringList)
        tvRelation.setAdapter(dataAdapter)

        tvRelation.setOnClickListener {
            val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = inflater?.inflate(R.layout.item_select_layout, null)
            popupWindow = PopupWindow(popupView, tvRelation.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAsDropDown(tvRelation, 0, 0, Gravity.CENTER)

            val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)
            rcyData?.adapter = TimeArrayCustomListAdapter(requireContext(), data, this, "time")

            tvRelation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.left_arrow_top, 0)
            popupWindow.setOnDismissListener {
                tvRelation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_bottom, 0)
            }
        }

        btnOkay.setOnClickListener {
            user?.let {
                if (it.email.isNullOrEmpty()) {
                    alertError(requireContext(), MessageClass.emailError, false)
                } else if (selectedRelationId == -1) {
                    alertError(requireContext(), MessageClass.relation, false)
                } else if (selectedAlertId == -1) {
                    alertError(requireContext(), MessageClass.alert, false)
                } else  if (!hasCountryCode(it.phone_number.toString())) {
                    // ❗ country code missing
                    showCountryCodeDialog(requireContext(), it.phone_number.toString()) { updatedNumber ->
                        it.phone_number = updatedNumber
                        if (type== "addEmergency"){
                            val fullName = it.name?.trim().orEmpty()

                            val nameParts = fullName.split("\\s+".toRegex(), limit = 2)

                            val firstName = nameParts.getOrNull(0) ?: ""
                            val lastName = nameParts.getOrNull(1) ?: ""
                            val createHelpingNeighbor = CreateHelpingNeighbor(firstName,
                                lastName,
                                it.email?:"",
                                toValidNumberWithCountryCode(it.phone_number),
                                selectedRelationId.toString(),
                                selectedAlertId.toString(),
                                "device")
                            Log.d("createHelpingNeighbor","$createHelpingNeighbor")
                            addContact1(createHelpingNeighbor,dialog)
                        } else if (type== "helpingNeighbors"){
                            val fullName = it.name?.trim().orEmpty()

                            val nameParts = fullName.split("\\s+".toRegex(), limit = 2)

                            val firstName = nameParts.getOrNull(0) ?: ""
                            val lastName = nameParts.getOrNull(1) ?: ""
                            val createHelpingNeighbor = CreateHelpingNeighbor(firstName,
                                lastName,
                                it.email?:"",
                                toValidNumberWithCountryCode(it.phone_number),
                                selectedRelationId.toString(),
                                selectedAlertId.toString(),
                                "device")
                            Log.d("createHelpingNeighbor","$createHelpingNeighbor")
                            addHelpingNeighbor(createHelpingNeighbor,dialog)
                        }
                        else{
                            val fullName = it.name?.trim().orEmpty()

                            val nameParts = fullName.split("\\s+".toRegex(), limit = 2)

                            val firstName = nameParts.getOrNull(0) ?: ""
                            val lastName = nameParts.getOrNull(1) ?: ""
                            val userContactRequest = UserContactRequest(
                                first_name = firstName?:"",
                                last_name= lastName?:"",
                                email= it.email?:"",
                                phone= toValidNumberWithCountryCode(it.phone_number.toString()),
                                relation_id=  selectedRelationId,
                                alert_id=  selectedAlertId,
                                type= "contact",
                                contact_type= "device")
                            addContact(userContactRequest,dialog)
                        }

                    }
                    return@setOnClickListener
                }
                else{
                    if (type== "addEmergency"){
                        val fullName = it.name?.trim().orEmpty()

                        val nameParts = fullName.split("\\s+".toRegex(), limit = 2)

                        val firstName = nameParts.getOrNull(0) ?: ""
                        val lastName = nameParts.getOrNull(1) ?: ""
                        val createHelpingNeighbor = CreateHelpingNeighbor(firstName,
                            lastName,
                            it.email?:"",
                            toValidNumberWithCountryCode(it.phone_number),
                            selectedRelationId.toString(),
                            selectedAlertId.toString(),
                            "device")
                        Log.d("createHelpingNeighbor","$createHelpingNeighbor")
                        addContact1(createHelpingNeighbor,dialog)
                    }else if (type== "helpingNeighbors"){
                        val fullName = it.name?.trim().orEmpty()

                        val nameParts = fullName.split("\\s+".toRegex(), limit = 2)

                        val firstName = nameParts.getOrNull(0) ?: ""
                        val lastName = nameParts.getOrNull(1) ?: ""
                        val createHelpingNeighbor = CreateHelpingNeighbor(firstName,
                            lastName,
                            it.email?:"",
                            toValidNumberWithCountryCode(it.phone_number),
                            selectedRelationId.toString(),
                            selectedAlertId.toString(),
                            "device")
                        Log.d("createHelpingNeighbor","$createHelpingNeighbor")
                        addHelpingNeighbor(createHelpingNeighbor,dialog)
                    }else{
                        val fullName = it.name?.trim().orEmpty()

                        val nameParts = fullName.split("\\s+".toRegex(), limit = 2)

                        val firstName = nameParts.getOrNull(0) ?: ""
                        val lastName = nameParts.getOrNull(1) ?: ""
                        val userContactRequest = UserContactRequest(
                            /*    it.name?:"",
                                "",
                                it.email?:"",
                                it.number!!,
                                selectedRelationId,
                                selectedAlertId,
                                "device"*/
                            first_name = firstName?:"",
                            last_name= lastName?:"",
                            email= it.email?:"",
                            phone= toValidNumberWithCountryCode(it.phone_number.toString()),
                            relation_id=  selectedRelationId,
                            alert_id=  selectedAlertId,
                            type= "contact",
                            contact_type= "device"
                        )
                        addContact(userContactRequest,dialog)
                    }

                }
               /* else {
                    val userContactRequest = UserContactRequest(
                        it.name,
                        "",
                        it.email,
                        it.phone_number,
                        selectedRelationId,
                        selectedAlertId,
                        "map",
                        "map"
                    )
                    addContact(userContactRequest, dialog)
                }*/
            }
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
   /*         if (type.equals("helpingNeighbors", true) || type.equals("addContact", true)) {
                findNavController().navigateUp()
            } else {
                findNavController().navigate(R.id.contactFragment)
            }*/
        }

        getRelation(tvRelation)
        getAllAlerts(tvAlerts)
    }
    fun toValidNumberWithCountryCode(input: String?): String {
        if (input.isNullOrBlank()) return ""

        // Remove all non-digit/non-plus characters
        var cleaned = input.trim().replace(Regex("[^0-9+]"), "")

        // Keep + only at the start
        if (cleaned.indexOf('+') > 0) {
            cleaned = cleaned.replace("+", "")
        }

        return cleaned
    }

    private fun addContact(userContactRequest: UserContactRequest, dialog: Dialog) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.addContact(userContactRequest).collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val addContactResponse =
                                    Gson().fromJson(it, AddContactResponse::class.java)
                                if (addContactResponse.code == 200) {
                                    dialog.dismiss()
                                    if (type.equals("helpingNeighbors", true) || type.equals("addContact", true)) {
                                        findNavController().navigateUp()
                                    } else {
                                        findNavController().navigate(R.id.contactFragment)
                                    }
                                    alertBoxSuccess()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        addContactResponse.message,
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
        } else {
            showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun addContact1(userContactRequest: CreateHelpingNeighbor, dialog: Dialog) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.addEmergencyContact(userContactRequest).collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val addContactResponse =
                                    Gson().fromJson(it, AddContactResponse1::class.java)
                                if (addContactResponse.code==200) {
                                    /*    if (type.equals("helpingNeighbors",true) ||
                                            type.equals("addContact",true)){*/
                                    findNavController().navigateUp()
                                    /*  }else{
                                          findNavController().navigate(R.id.contactFragment)
                                      }*/
                                    dialog.dismiss()
                                    alertBoxSuccess()
                                }else{
                                    Toast.makeText(
                                        requireContext(),
                                        addContactResponse.message,
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

    private fun addHelpingNeighbor(createHelpingNeighbor: CreateHelpingNeighbor,dialogContact:Dialog) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.addNeighbor(createHelpingNeighbor).collect {
                    BaseApplication.dismissDialog()
                    handleApiResponse(it, dialogContact)
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun handleApiResponse(it: NetworkResult<JsonObject>, dialogContact: Dialog) {
        when (it) {
            is NetworkResult.Success -> handleSuccessApiResponse(it.data.toString(), dialogContact)
            is NetworkResult.Error -> showAlert(requireContext(),it.message?:"", false)
            else -> showAlert(requireContext(),it.message?:"", false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessApiResponse(data: String,dialogContact:Dialog) {
        try {
            val apiModel = Gson().fromJson(data, AddNeighborModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.status == true) {
                findNavController().navigateUp()
                //  getHelpingNeighbor(latitude.toDouble(),longitude.toDouble())
                dialogContact.dismiss()
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(requireContext(),e.message?:"", false)
        }
    }
    private fun handleError(code: Int?, message: String?) {
        if (code== MessageClass.deactivatedUser || code== MessageClass.deletedUser){
            showAlert(requireContext(),message?:"", true)
        }else{
            showAlert(requireContext(),message?:"", false)
        }
    }

    private fun getRelation(tv_relation: MaterialAutoCompleteTextView) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getRelation().collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val relationResponse =
                                    Gson().fromJson(it, RelationResponse::class.java)
                                if (relationResponse.code == 200) {
                                    val relationList = relationResponse.data
                                    val relationNames = relationList.map { it.name }
                                    val adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_dropdown_item_1line,
                                        relationNames
                                    )
                                    tv_relation.setAdapter(adapter)
                                    tv_relation.setOnItemClickListener { parent, view, position, id ->
                                        val selectedAlert = relationList[position]
                                        selectedRelationId = selectedAlert.id
                                    }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        relationResponse.message,
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
        } else {
            showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun getAllAlerts(tvAllAlerts: MaterialAutoCompleteTextView) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getAllAlerts().collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val alertsResponse =
                                    Gson().fromJson(it, AlertsResponse::class.java)
                                if (alertsResponse.code == 200) {
                                    val alerts = alertsResponse.data
                                    val alertsTitle = alerts.map { it.title }
                                    val adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_dropdown_item_1line,
                                        alertsTitle
                                    )
                                    tvAllAlerts.setAdapter(adapter)
                                    tvAllAlerts.setOnItemClickListener { parent, view, position, id ->
                                        val selectedAlert = alerts[position]
                                        selectedAlertId = selectedAlert.id
                                    }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        alertsResponse.message,
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
        } else {
            showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun alertBoxSuccess() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_success)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams

        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        val tvOK = dialog.findViewById<TextView>(R.id.tvOK)
        val text = dialog.findViewById<TextView>(R.id.tv_text)

        text.text = "Your Contact has been added\nSuccessfully."

        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvOK.setOnClickListener {
            dialog.dismiss()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun onClickDropDown(pos: String?, type: String?) {
        if (type.equals("time")) {
            for (i in data.indices) {
                val item = data[i].copy()
                item.name = data[i].name ?: ""
                item.status = i == pos?.toInt()
                data[i] = item
            }
        }
        popupWindow.dismiss()
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = true
        }

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        googleMap.setOnInfoWindowClickListener { marker ->
            val user = marker.tag as? UserData
            user?.let {
                addAlert(it)
            }
        }
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

    fun showCountryCodeDialog(
        context: Context,
        phone: String,
        onResult: (String) -> Unit
    ) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_country_code)
        dialog.setCancelable(true)
        val window = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        // Optional: Background transparent करें
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val ccp = dialog.findViewById<CountryCodePicker>(R.id.ccp)
        val etPhone = dialog.findViewById<EditText>(R.id.etPhone)
        val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmit)

        etPhone.setText(phone)

        btnSubmit.setOnClickListener {
            val countryCode = ccp.selectedCountryCodeWithPlus
            val number = etPhone.text.toString().trim()

            if (number.isEmpty()) {
                Toast.makeText(context, "Enter phone number", Toast.LENGTH_SHORT).show()
            } else {
                val finalNumber = "$countryCode$number"
                dialog.dismiss()
                onResult(finalNumber)
            }
        }

        dialog.show()
    }
    fun hasCountryCode(number: String): Boolean {
        return number.trim().startsWith("+")
    }
}