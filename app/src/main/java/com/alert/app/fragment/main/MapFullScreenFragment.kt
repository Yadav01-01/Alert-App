package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.alert.app.base.AppConstant
import com.alert.app.base.BaseApplication
import com.alert.app.base.BaseApplication.alertError
import com.alert.app.databinding.FragmentMapFullScreenBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.OnClickEventDropDownType
import com.alert.app.model.SpinnerModel
import com.alert.app.model.contact.UserContactRequest
import com.alert.app.model.helpingneighbormodel.CreateHelpingNeighbor
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
import android.widget.PopupWindow
import com.alert.app.model.contact.AddContactResponse
import com.alert.app.model.contact.AlertsResponse
import com.alert.app.model.contact.RelationResponse
import com.hbb20.CountryCodePicker

@AndroidEntryPoint
class MapFullScreenFragment : Fragment(), OnMapReadyCallback,OnClickEventDropDownType {

    private lateinit var binding: FragmentMapFullScreenBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null
    private val viewModel: MapViewModel by viewModels()
    val data: MutableList<SpinnerModel> = mutableListOf()
    private var selectedAlertId = -1
    private var selectedRelationId = -1
    lateinit var popupWindow: PopupWindow

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
                                    Log.d("TESTING_RECYCLER_SIZE","Size is "+users.size)
                                    users.forEach { user ->
                                        user.latitude?.let { lat->
                                            user.longitude?.let { lon->
                                                Log.d("TESTING_RECYCLER_SIZE","Latitude "+user.latitude +" Longitude"+ user.longitude)

                                                createMarkerFromView(requireContext(), AppConstant.IMAGE_BASE_URL+user.profile_image)
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
        googleMap.setOnInfoWindowClickListener { marker ->
            val user = marker.tag as? UserData
            user?.let {
                addAlert(it)
            }
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
        Log.d("TESTING_SIZE","Image Url is "+imageUrl)
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .placeholder(R.drawable.marker_demmy_pic)
            .error(R.drawable.marker_demmy_pic) // Agar URL crash kare toh ye chale
            .into(object : CustomTarget<Bitmap>() {

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Case 1: Sahi image load ho gayi
                    processMarkerView(resource)
                }

                override fun onLoadStarted(placeholder: Drawable?) {
                    super.onLoadStarted(placeholder)
                    // Case 2: Loading start hote hi dummy image dikhao (agar callback turant chahiye)
                    handlePlaceholder(placeholder)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {

                    handlePlaceholder(errorDrawable)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}

                // Helper function to handle Drawable to Bitmap conversion
                private fun handlePlaceholder(drawable: Drawable?) {
                    drawable?.let {
                        val bitmap = if (it is BitmapDrawable) {
                            it.bitmap
                        } else {
                            // Drawable ko bitmap mein convert karna padega
                            val b = Bitmap.createBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(b)
                            it.setBounds(0, 0, canvas.width, canvas.height)
                            it.draw(canvas)
                            b
                        }
                        processMarkerView(bitmap)
                    }
                }

                // Common logic to convert your custom view to Google Maps Marker
                private fun processMarkerView(imageBitmap: Bitmap) {
                    profileImage.setImageBitmap(imageBitmap)

                    // View update logic
                    val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    markerView.measure(measureSpec, measureSpec)
                    markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

                    val finalBitmap = Bitmap.createBitmap(
                        markerView.measuredWidth, markerView.measuredHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(finalBitmap)
                    markerView.draw(canvas)

                    // Final callback for Google Maps
                    callback(BitmapDescriptorFactory.fromBitmap(finalBitmap))
                }
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


        val relationList = data.map { it.name }
        val relationAdapter = ArrayAdapter(requireContext(),
            R.layout.drop_down_item,
            relationList)
        tvRelation.setAdapter(relationAdapter)

        // Use setOnItemClickListener instead of setOnClickListener
        tvRelation.setOnItemClickListener { _, _, position, _ ->
            selectedRelationId = data[position].id
            tvRelation.setText(relationList[position], false)
        }
        /*   tvRelation.setOnItemClickListener { _, _, position, _ ->
               selectedRelationId = data[position].id
               tvRelation.setText(stringList[position], false)
           }*/

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
                    return@setOnClickListener
                }
                else{
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

                                        findNavController().navigate(R.id.contactFragment)

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
}
