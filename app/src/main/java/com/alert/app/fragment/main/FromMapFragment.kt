package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter

import android.widget.ImageView
import android.widget.PopupWindow
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
      /*  val dataAdapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, data)
        tvRelation.setAdapter<ArrayAdapter<String>>(dataAdapter)*/

        /*tvRelation.setOnClickListener {
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


        }*/



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
}