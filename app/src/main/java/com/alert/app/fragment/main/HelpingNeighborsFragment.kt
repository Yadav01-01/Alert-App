package com.alert.app.fragment.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.BuildConfig
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.AddressAdapter
import com.alert.app.adapter.HelpingNeighborsAdapter
import com.alert.app.adapter.PlacesAutoCompleteAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentHelpingNeighborsBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.OnClickContact
import com.alert.app.listener.OnPlacesDetailsListener
import com.alert.app.model.TimeModel
import com.alert.app.model.addressmodel.Place
import com.alert.app.model.addressmodel.PlaceAPI
import com.alert.app.model.addressmodel.PlaceDetails
import com.alert.app.model.contact.Alert
import com.alert.app.model.contact.AlertsResponse
import com.alert.app.model.contact.Relation
import com.alert.app.model.contact.RelationResponse
import com.alert.app.model.helpingneighbormodel.AddNeighborModel
import com.alert.app.model.helpingneighbormodel.Contact
import com.alert.app.model.helpingneighbormodel.CreateHelpingNeighbor
import com.alert.app.model.helpingneighbormodel.GetNeighborModel
import com.alert.app.model.helpingneighbormodel.GetNeighborModelData
import com.alert.app.viewmodel.helpingneighborviewmodel.HelpingNeighborViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class HelpingNeighborsFragment : Fragment() , OnClickContact, OnMapReadyCallback {

    private var _binding: FragmentHelpingNeighborsBinding?=null
    private val binding get() = _binding!!
    private lateinit var viewModel: HelpingNeighborViewModel
    private lateinit var openBottomSheetDialog: BottomSheetDialog
    private lateinit var adapter: HelpingNeighborsAdapter
    private var selectedAlertId = -1
    private var selectedRelationId = -1
    private var relation: List<Relation> = mutableListOf()
    private var alerts: List<Alert> = mutableListOf()
    private lateinit var tvRelation :MaterialAutoCompleteTextView
    private lateinit var tvAlerts :MaterialAutoCompleteTextView
    private lateinit var tvAddress :TextView
    private val getContactList: MutableList<Contact> = mutableListOf()
    private var latitude = ""
    private var longitude = ""
    private var currentLocation: Location? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    val data: MutableList<TimeModel> = mutableListOf()
    private val REQUEST_CODE = 101
    private val LOCATION_CODE = 100
    lateinit var popupWindow:PopupWindow
    private val TAG = "LocationOnOff"
    private var mMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHelpingNeighborsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as? MainActivity)?.setImageShowTv()?.visibility = View.GONE
        (requireActivity() as? MainActivity)?.setImgChatBoot()?.visibility = View.GONE

        viewModel = ViewModelProvider(this)[HelpingNeighborViewModel::class.java]

    /*    MainScope().launch {
            delay(5000) // Delay for 5 seconds
            binding.rcyData.visibility=View.VISIBLE
            binding.layTitle.visibility=View.VISIBLE
            binding.tvTitle.visibility = View.VISIBLE
            binding.layCurrentLocation.visibility=View.VISIBLE
            binding.layno.visibility=View.GONE
        }*/

        initialize()

    }

    private fun initialize() {

        adapter=  HelpingNeighborsAdapter(requireContext(),getContactList,this)
        binding.rcyData.adapter= adapter

/*        binding.layno.visibility=View.VISIBLE
        binding.rcyData.visibility=View.GONE
        binding.layTitle.visibility=View.GONE
        binding.tvTitle.visibility = View.GONE
        binding.btnAddNow.visibility = View.GONE*/

        binding.btnAdd.setOnClickListener {
            alertBottom()
        }

        binding.btnAddNow.setOnClickListener {
            alertBottom()
        }

        binding.layCurrentLocation.setOnClickListener {
            openAlertBoxLocation()
        }

        getHelpingNeighbor()
    }

    private fun getHelpingNeighbor() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getNeighbor().collect {
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
            is NetworkResult.Error -> showAlert(it.message, false)
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
            val apiModel = Gson().fromJson(data, GetNeighborModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.status == true) {
                if (apiModel.data != null) {
                    showDataInUI(apiModel.data)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showDataInUI(data: GetNeighborModelData) {
        try {
            getContactList.clear()

            data.contactList?.let {
                getContactList.addAll(it)
            }

            data.userAddress.let { address ->
                address.type?.let {
                    binding.tvAddressType.text = it
                }

                address.address?.let {
                    binding.tvUserAddress.text = it
                }
            }

            if (getContactList.size > 0) {
                binding.rcyData.visibility = View.VISIBLE
                binding.tvTitle.visibility = View.VISIBLE
                binding.layno.visibility = View.GONE
                binding.layCurrentLocation.visibility = View.VISIBLE
                binding.btnAddNow.visibility = View.VISIBLE
                adapter.update(getContactList)
            } else {
                binding.layno.visibility = View.VISIBLE
                binding.tvTitle.visibility = View.GONE
                binding.rcyData.visibility = View.GONE
                binding.layCurrentLocation.visibility = View.GONE
                binding.btnAddNow.visibility = View.GONE
            }

        }catch (e:Exception){
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int?, message: String?) {
        if (code== MessageClass.deactivatedUser || code== MessageClass.deletedUser){
            showAlert(message, true)
        }else{
            showAlert(message, false)
        }
    }

    private fun alertBottom() {
        openBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        openBottomSheetDialog.setContentView(R.layout.bottom_sheet_scanner)
        val cancel: ImageView? = openBottomSheetDialog.findViewById(R.id.img_cross)
        val tvManually: TextView? = openBottomSheetDialog.findViewById(R.id.tv_manually)
        val tvContacts: TextView? = openBottomSheetDialog.findViewById(R.id.tv_contacts)
        val tvMap: TextView? = openBottomSheetDialog.findViewById(R.id.tv_map)

        cancel?.setOnClickListener {
            openBottomSheetDialog.dismiss()
        }
        openBottomSheetDialog.show()

        tvManually?.setOnClickListener {
            openBottomSheetDialog.dismiss()
            openAlertBox("add")
        }

        tvContacts?.setOnClickListener {
            openBottomSheetDialog.dismiss()
            val bundle=Bundle()
            bundle.putString("type","helpingNeighbors")
            findNavController().navigate(R.id.mobileContactListFragment,bundle)
        }

        tvMap?.setOnClickListener {
            openBottomSheetDialog.dismiss()
            val bundle=Bundle()
            bundle.putString("type","helpingNeighbors")
            findNavController().navigate(R.id.fromMapFragment,bundle)
        }
    }


    private fun openAlertBoxLocation(){
        openBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        openBottomSheetDialog.setContentView(R.layout.bottom_location_open)
        val cancel: ImageView? = openBottomSheetDialog.findViewById(R.id.img_cross)
        val tvCurrent: TextView? = openBottomSheetDialog.findViewById(R.id.tv_current)
        val addAddress: TextView? = openBottomSheetDialog.findViewById(R.id.add_address)
        val rcyData: RecyclerView? = openBottomSheetDialog.findViewById(R.id.rcy_data)
        val edtAddress: AutoCompleteTextView? = openBottomSheetDialog.findViewById(R.id.edtAddress)

        val placesApi = PlaceAPI.Builder()
            .apiKey(getString(R.string.api_keysearch))
            .build(requireContext())

        edtAddress?.setAdapter(PlacesAutoCompleteAdapter(requireContext(), placesApi))
        edtAddress?.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val place = parent.getItemAtPosition(position) as Place
            edtAddress?.setText(place.description)
            getPlaceDetails(place.id, placesApi)
        }

        cancel?.setOnClickListener {
            openBottomSheetDialog.dismiss()
        }

        tvCurrent?.setOnClickListener {
            addCurrentAddressMap()
//            openBottomSheetDialog.dismiss()
        }

        addAddress?.setOnClickListener {
            addAlertBoxAddress()
        }

        rcyData?.adapter= AddressAdapter(requireContext())

        openBottomSheetDialog.show()

    }

    @SuppressLint("SuspiciousIndentation")
    private fun addCurrentAddressMap() {
        val dialogCurrentAddress = Dialog(requireContext())
        dialogCurrentAddress.setContentView(R.layout.dialog_current_address_item)
        dialogCurrentAddress.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        dialogCurrentAddress.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        layoutParams.copyFrom(dialogCurrentAddress.window!!.attributes)
        dialogCurrentAddress.window!!.attributes = layoutParams

        tvAddress=dialogCurrentAddress.findViewById(R.id.tvAddress)

        dialogCurrentAddress.show()

        val apiKey = getString(R.string.api_key)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
          fetchLocation()
            // check condition
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // When permission is granted
                // Call method
                getCurrentLocation()
            } else {
                // When permission is not granted
                // Call method
                requestPermissions(
                    arrayOf<String>(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), LOCATION_CODE
                )
                displayLocationSettingsRequest(requireContext())
            }
    }

    private fun displayLocationSettingsRequest(requireContext: Context) {
        val googleApiClient =
            GoogleApiClient.Builder(requireContext).addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 1000
        locationRequest.numUpdates = 1
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.i(TAG,
                        "All location settings are satisfied.")
                    getCurrentLocation()
                }

                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(TAG,
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(
                            requireActivity(),
                            LOCATION_CODE
                        )
                    } catch (e: SendIntentException) {
                        Log.i(TAG,
                            "PendingIntent unable to execute request."
                        )
                    }
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(TAG,
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )
            }
        }
    }


    private fun getCurrentLocation() {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
            // When location service is enabled
            // Get last location
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
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
            fusedLocationProviderClient!!.lastLocation.addOnCompleteListener(OnCompleteListener<Location?> { task ->
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    latitude = location!!.latitude.toString()
                    longitude = location.longitude.toString()
                    tvAddress.text = getAddress(location.latitude, location.longitude)
                } else {
                    val locationRequest =
                        LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000).setFastestInterval(1000)
                    val locationCallback: LocationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            // Initialize
                            // location
                            val location1 = locationResult.lastLocation
                            latitude = location1!!.latitude.toString()
                            longitude = location1.longitude.toString()
                            tvAddress.text = getAddress(location1.latitude, location1.longitude)

                        }
                    }

                    // Request location updates
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        return@OnCompleteListener
                    }
                    fusedLocationProviderClient!!.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.myLooper()
                    )
                }
            })
        } else {
            // When location service is not enabled
            // open location setting
            requestPermissions(
                arrayOf<String>(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
              LOCATION_CODE
            )
            displayLocationSettingsRequest(requireContext())
        }
    }


    private fun getAddress(lat: Double, longi: Double): String? {
        var address = ""
        try {
            val addresses: List<Address>?
            val geocoder: Geocoder = Geocoder(requireActivity(), Locale.getDefault())
            addresses = geocoder.getFromLocation(
                lat,
                longi,
                1
            ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses!![0].getAddressLine(0)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return address
    }


    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            return
        }
        val task = fusedLocationProviderClient!!.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                val mapFragment = childFragmentManager.findFragmentById(R.id.mapid) as? SupportMapFragment
                mapFragment?.getMapAsync(this)
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        val lat = latitude.toDoubleOrNull() ?: 0.0  // Convert String to Double, default to 0.0 if null
        val lng = longitude.toDoubleOrNull() ?: 0.0
        this.mMap = googleMap

        val latLng = LatLng(lat, lng) // Example: Sydney

     /*   // 1. Create LatLng from currentLocation
        val latLng = LatLng(
            currentLocation!!.latitude, currentLocation!!.longitude
        )*/

        // 2. Create MarkerOptions with the obtained LatLng
        val markerOptions = MarkerOptions().position(latLng).title("I am here!")
        // 3. Animate camera to the LatLng (first call)
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        // 4. Animate camera to the LatLng with a zoom level of 15f (second call, overrides first)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        // 5. Add the marker to the map
        googleMap.addMarker(markerOptions)
    }

    /*override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap

            val latLng = LatLng(
                currentLocation!!.latitude, currentLocation!!.longitude
            )
            val markerOptions = MarkerOptions().position(latLng).title("I am here!")
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            googleMap.addMarker(markerOptions)
//            googleMap.setOnMapClickListener(this)
    }*/

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation()
            }
            LOCATION_CODE -> if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // When permission are granted
                // Call  method
//            getCurrentLocation();
            } else {
                // When permission are denied
                // Display toast
                Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            getCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getPlaceDetails(placeId: String, placesApi: PlaceAPI) {
        placesApi.fetchPlaceDetails(placeId, object :
            OnPlacesDetailsListener {
            override fun onError(errorMessage: String) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceDetailsFetched(placeDetails: PlaceDetails) {
                try {
                    latitude = placeDetails.lat.toString()
                    longitude = placeDetails.lng.toString()
                } catch (e: java.lang.Exception) {
                    BaseApplication.alertError(requireContext(), e.message, false)
                }
            }
        })
    }

    private fun addAlertBoxAddress(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_address)

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()

        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

        val placesApi = PlaceAPI.Builder()
            .apiKey(getString(R.string.api_keysearch))
            .build(requireContext())

        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams

        val edText = dialog.findViewById<AutoCompleteTextView>(R.id.ed_text)
        val btnSave = dialog.findViewById<TextView>(R.id.btnSave)
        val imgCross = dialog.findViewById<ImageView>(R.id.img_cross)
        val home = dialog.findViewById<RelativeLayout>(R.id.rl_home)
        val office = dialog.findViewById<RelativeLayout>(R.id.rl_office)
        val hotel = dialog.findViewById<RelativeLayout>(R.id.rl_hotel)
        val other = dialog.findViewById<RelativeLayout>(R.id.rl_other)

        edText?.setAdapter(PlacesAutoCompleteAdapter(requireContext(), placesApi))
        edText?.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val place = parent.getItemAtPosition(position) as Place
            edText?.setText(place.description)
            getPlaceDetails(place.id, placesApi)
        }

        var worktype: String = ""


        if (worktype.equals("home",true)) {
            home.setBackgroundResource(R.drawable.bg_selected_address_btn)
            office.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            hotel.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            other.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
        }

        if (worktype.equals("work",true)) {
            home.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            office.setBackgroundResource(R.drawable.bg_selected_address_btn)
            hotel.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            other.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
        }

        if (worktype.equals("hotel",true)) {
            home.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            office.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            hotel.setBackgroundResource(R.drawable.bg_selected_address_btn)
            other.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
        }

        if (worktype.equals("other",true)) {
            home.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            office.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            hotel.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            other.setBackgroundResource(R.drawable.bg_selected_address_btn)
        }

        home.setOnClickListener {
            worktype = "home"
            home.setBackgroundResource(R.drawable.bg_selected_address_btn)
            office.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            hotel.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            other.setBackgroundResource(R.drawable.bg_four_side_corner_grey)

        }
        office.setOnClickListener {
            worktype = "work"
            home.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            office.setBackgroundResource(R.drawable.bg_selected_address_btn)
            hotel.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            other.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
        }
        hotel.setOnClickListener {
            worktype = "hotel"
            home.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            office.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            hotel.setBackgroundResource(R.drawable.bg_selected_address_btn)
            other.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
        }
        other.setOnClickListener {
            worktype = "other"
            home.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            office.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            hotel.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            other.setBackgroundResource(R.drawable.bg_selected_address_btn)
        }

        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        imgCross.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val userAddress:String =edText.text.toString().trim()
            if (worktype.equals("",true)) {
                BaseApplication.alertError(context, MessageClass.typeError,false)
            }else{
                if (userAddress.isEmpty()) {
                    BaseApplication.alertError(context, MessageClass.addressError,false)
                } else {
                    addUserAddress(userAddress,worktype,dialog)
                }
            }
        }
    }

    private fun addUserAddress(userAddress:String,workType:String,dialog: Dialog) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.addUserAddress(workType,userAddress,latitude,longitude).collect {
                    BaseApplication.dismissDialog()
                    handleAddUserApiResponse(it,dialog)
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun handleAddUserApiResponse(it: NetworkResult<JsonObject>, dialogAddress: Dialog) {
        when (it) {
            is NetworkResult.Success -> handleSuccessAddressApiResponse(it.data.toString(), dialogAddress)
            is NetworkResult.Error -> showAlert(it.message, false)
            else -> showAlert(it.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessAddressApiResponse(data: String,dialogAddress:Dialog) {
        try {
            val apiModel = Gson().fromJson(data, AddNeighborModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.status == true) {
                getHelpingNeighbor()
                openBottomSheetDialog.dismiss()
                dialogAddress.dismiss()
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    override fun onClick(data: String, id: String) {

        if (data.equals("openProfile",true)){
            val position= id.toIntOrNull()
            showNotificationDialog(position)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showNotificationDialog(position: Int?) {
        val dialog = createDialog(R.layout.dialog_notification)

        val userImg= dialog.findViewById<ImageView>(R.id.user_img)
        val layProgress = dialog.findViewById<View>(R.id.layProgess)
        val textUserName = dialog.findViewById<TextView>(R.id.textUserName)
        val tvTime = dialog.findViewById<TextView>(R.id.tv_time)
        val tvAlertName = dialog.findViewById<TextView>(R.id.tvAlertName)
        val tvRelationType = dialog.findViewById<TextView>(R.id.tvRelationType)
        val tvDescription = dialog.findViewById<TextView>(R.id.tvDescription)

        val item = getContactList[position!!]
        val profileImage = item.profile_pic?.takeIf { it.isNotBlank() } ?: ""
        val userName = item.first_name?.takeIf { it.isNotBlank() } ?: ""
        val lastName = item.last_name?.takeIf { it.isNotBlank() } ?: ""
        val duration = item.alert_duration?.takeIf { it.isNotBlank() } ?: ""
        val alertName = item.alert?.takeIf { it.isNotBlank() } ?: ""
        val relationType = item.relation?.takeIf { it.isNotBlank() } ?: ""
        val description = item.alert_description?.takeIf { it.isNotBlank() } ?: ""

        textUserName.text= "$userName $lastName"
        tvTime.text=BaseApplication.getTimeAgoText(duration)
        tvAlertName.text=alertName
        tvRelationType.text=relationType
        tvDescription.text=description

        Glide.with(requireActivity())
            .load(BuildConfig.BASE_URL+profileImage)
            .error(R.drawable.no_image)
            .placeholder(R.drawable.no_image)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    layProgress.visibility= View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    layProgress.visibility= View.GONE
                    return false
                }
            })
            .into(userImg)

        dialog.findViewById<TextView>(R.id.tvView).setOnClickListener {
            dialog.dismiss()
            val bundle=Bundle()
            bundle.putString("contactId",item.contact_id.toString())
            findNavController().navigate(R.id.neighborProfileFragment,bundle)
        }
        dialog.findViewById<TextView>(R.id.tvCancel).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<ImageView>(R.id.img_close).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun createDialog(layoutRes: Int): Dialog {
        return Dialog(requireContext()).apply {
            setContentView(layoutRes)
            setCancelable(false)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                attributes = attributes?.apply {
                    width = WindowManager.LayoutParams.MATCH_PARENT
                    height = WindowManager.LayoutParams.WRAP_CONTENT
                }
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun openAlertBox(data:String){
        val dialogContact = Dialog(requireContext())
        dialogContact.setContentView(R.layout.dialog_contact)
        dialogContact.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        dialogContact.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        layoutParams.copyFrom(dialogContact.window!!.attributes)
        dialogContact.window!!.attributes = layoutParams

        val btnAdd = dialogContact.findViewById<TextView>(R.id.btnAdd)
        val tvHeading = dialogContact.findViewById<TextView>(R.id.tv_heading)
        val tvText = dialogContact.findViewById<TextView>(R.id.tv_text)
        val edFullName = dialogContact.findViewById<EditText>(R.id.ed_full_name)
        val edLastName = dialogContact.findViewById<EditText>(R.id.ed_last_name)
        val edEmail = dialogContact.findViewById<EditText>(R.id.ed_email)
        val edPhone = dialogContact.findViewById<EditText>(R.id.ed_phone)
        val layRelation = dialogContact.findViewById<TextInputLayout>(R.id.layRelation)
        tvRelation = dialogContact.findViewById(R.id.tvRelation)
        tvAlerts = dialogContact.findViewById(R.id.tvAlerts)

        tvRelation.setOnClickListener {
            if (relation.isNotEmpty()){

                // 1. Extract names only for showing
                val relationNames = relation.map { it.name }
                // 2. Set adapter to MaterialAutoCompleteTextView
                val adapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, relationNames)
                tvRelation.setAdapter(adapter)
                tvRelation.showDropDown()

                // 3. Handle item selection
                tvRelation.setOnItemClickListener { _, _, position, _ ->
                    val selectedRelation = relation[position] // Get full model by position
                     selectedRelationId = selectedRelation.id

                    // Store ID or use it as needed
                    Log.d("SelectedRelation", "ID: $selectedRelationId ")
                }
            }else{
                getRelation()
            }
        }

        tvAlerts.setOnClickListener {
            if (alerts.isNotEmpty()){
                // 1. Extract names only for showing
                val relationNames = alerts.map { it.title }
                // 2. Set adapter to MaterialAutoCompleteTextView
                val adapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, relationNames)
                tvAlerts.setAdapter(adapter)
                tvAlerts.showDropDown()

                // 3. Handle item selection
                tvAlerts.setOnItemClickListener { _, _, position, _ ->
                    val selectedAlerts = alerts[position] // Get full model by position
                    selectedAlertId= selectedAlerts.id
                    val selectedAlertsName = selectedAlerts.title

                    // Store ID or use it as needed
                    Log.d("SelectedRelation", "ID: $selectedRelationId, Name: $selectedAlertId")
                }
            }else{
                getAllAlerts()
            }
        }
        dialogContact.show()
        dialogContact.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        btnAdd.setOnClickListener {
            if (BaseApplication.cantactValidationError(requireContext(),edFullName,edLastName,edEmail,edPhone,selectedAlertId,selectedRelationId)){
                val createHelpingNeighbor = CreateHelpingNeighbor(
                    edFullName.text.toString().trim(),edLastName.text.toString().trim(),edEmail.text.toString().trim(),
                    edPhone.text.toString().trim(),   selectedRelationId.toString(),
                    selectedAlertId.toString(),"manual"
                )
                addHelpingNeighbor(createHelpingNeighbor,dialogContact)
//                dialog.dismiss()
            }
        }
    }

    private fun getRelation() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getRelation().collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {  apiData->
                                val relationResponse = Gson().fromJson(apiData, RelationResponse::class.java)
                                if (relationResponse.code==200) {
                                    relation = relationResponse.data

                                    // 1. Extract names only for showing
                                    val relationNames = relation.map { it.name }
                                    // 2. Set adapter to MaterialAutoCompleteTextView
                                    val adapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, relationNames)
                                    tvRelation.setAdapter(adapter)
                                    tvRelation.showDropDown()

                                    // 3. Handle item selection
                                    tvRelation.setOnItemClickListener { _, _, position, _ ->
                                        val selectedRelation = relation[position] // Get full model by position
                                        selectedRelationId= selectedRelation.id

                                        // Store ID or use it as needed
                                        Log.d("SelectedRelation", "ID: $selectedRelationId")
                                    }
                                }else{
                                    Toast.makeText(requireContext(), relationResponse.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }else{
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun getAllAlerts() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getAllAlerts().collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val alertsResponse = Gson().fromJson(it, AlertsResponse::class.java)
                                if (alertsResponse.code==200) {
                                    alerts = alertsResponse.data
                                    // 1. Extract names only for showing
                                    val relationNames = alerts.map { it.title }
                                    // 2. Set adapter to MaterialAutoCompleteTextView
                                    val adapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, relationNames)
                                    tvAlerts.setAdapter(adapter)
                                    tvAlerts.showDropDown()

                                    // 3. Handle item selection
                                    tvAlerts.setOnItemClickListener { _, _, position, _ ->
                                        val selectedAlerts = alerts[position] // Get full model by position
                                        selectedAlertId= selectedAlerts.id
                                        val selectedAlertsName = selectedAlerts.title

                                        // Store ID or use it as needed
                                        Log.d("SelectedRelation", "ID: $selectedRelationId, Name: $selectedAlertId")
                                    }
                                }else{
                                    Toast.makeText(requireContext(), alertsResponse.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }else{
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
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
            is NetworkResult.Error -> showAlert(it.message, false)
            else -> showAlert(it.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessApiResponse(data: String,dialogContact:Dialog) {
        try {
            val apiModel = Gson().fromJson(data, AddNeighborModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.status == true) {
                getHelpingNeighbor()
                dialogContact.dismiss()
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }
}