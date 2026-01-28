package com.alert.app.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.BuildConfig
import com.alert.app.R
import com.alert.app.adapter.ChatAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.base.SessionManagement
import com.alert.app.databinding.ActivityMainBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.chatbot.ChatMessage
import com.alert.app.model.emergencytextmessage.EmergencyTextMszModel
import com.alert.app.model.homemodel.HomeModel
import com.alert.app.viewmodel.locationmain.LocationViewModel
import com.alert.app.viewmodel.mainactivitymodel.MainActivityViewModel
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManagement: SessionManagement
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var locationViewModel: LocationViewModel
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    //tts
    private lateinit var tts: TextToSpeech
    private var isTtsReady = false


    private val handler = Handler(Looper.getMainLooper())
    private var userReminderRunnable: Runnable? = null

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]

        sessionManagement = SessionManagement(this)
        sessionManagement.setProfileScreen("login")
        BaseApplication.alertBox(this)
        setFooter("home")

//        val filter = IntentFilter("FCM_NOTIFICATION_RECEIVED")
//        registerReceiver(fcmReceiver, filter)
        val filter = IntentFilter("FCM_NOTIFICATION_RECEIVED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(fcmReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(fcmReceiver, filter)
        }


        // Initialize TextToSpeech
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                isTtsReady = true

                speakTotalNotifications()
            }
        }

        resetCheckInDailyIfNeeded()

        // Observe check-in response
        viewModel.checkInResponse.observe(this) { response ->
            when (response) {
                MainActivityViewModel.CheckInResponse.YES -> {
                    saveCheckInTime()
                    cancelScheduledUserAlerts()
                    Log.d("MainActivity", "User clicked YES")
                }

                MainActivityViewModel.CheckInResponse.NO -> {
                    saveCheckInTime()
                    sendEmergencyAlertImmediately()
                    cancelScheduledUserAlerts()
                    Log.d("MainActivity", "User clicked NO")
                }

                null -> {}
            }
        }

        // Location check
        if (checkLocationPermission()) {
            if (isLocationEnabled()) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Please enable location services", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            requestLocationPermission()
        }

        setUI()
        sideBar()
        clickListener()
    }

    override fun onResume() {
        super.onResume()
        checkIf15MinutesPassed()
    }

    //
    private fun speakTotalNotifications() {
        val count = getNotificationCount()
        val msg = if (count > 0)
            "You have received $count notifications"
        else
            "You have no notifications"

        speak(msg)
    }

    private fun speak(msg: String) {
        if (isTtsReady) {
            tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun getNotificationCount(): Int {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return prefs.getInt("notif_count", 0)
    }

    private val fcmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            speak("You received a new notification")
        }
    }



    //tts end

    fun saveCheckInTime() {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        getSharedPreferences("CheckInPrefs", MODE_PRIVATE)
            .edit {
                putLong("last_checkin_time", System.currentTimeMillis())
                putBoolean("has_checked_in", true)
                putString("checkin_date", today)
            }
    }


    private fun resetCheckInDailyIfNeeded() {
        val prefs = getSharedPreferences("CheckInPrefs", MODE_PRIVATE)
        val lastDate = prefs.getString("last_date", "")
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        if (today != lastDate) {
            prefs.edit {
                putBoolean("has_checked_in", false)
                    .putInt("skipped_alerts", 0)
                    .putString("last_date", today)
            }
        }
    }

    private fun checkIf15MinutesPassed() {
        val prefs = getSharedPreferences("CheckInPrefs", MODE_PRIVATE)
        val lastCheckIn = prefs.getLong("last_checkin_time", 0L)
        val hasCheckedIn = prefs.getBoolean("has_checked_in", false)
        val lastDate = prefs.getString("checkin_date", "")
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val now = System.currentTimeMillis()

        val timePassed = now - lastCheckIn

        // If user has not checked in today
        if (!hasCheckedIn || lastDate != today) {
            if (timePassed > 15 * 60 * 1000) {
                Log.d("MainActivity", "15 minutes passed. No check-in today.")
                scheduleUserReminder()
            } else {
                val delay = 15 * 60 * 1000 - timePassed
                Log.d("MainActivity", "Scheduling reminder in: ${delay / 1000} sec")
                scheduleUserReminder(delay)
            }
        }
    }


    private fun scheduleUserReminder(delayMillis: Long = 15 * 60 * 1000) {
        cancelScheduledUserAlerts()

        userReminderRunnable = Runnable {
            sendUserNotification("Please check in to the Home Screen")
            trackSkippedAlert() // will re-call schedule again if skip < 3
        }

        handler.postDelayed(userReminderRunnable!!, delayMillis)
    }


    private fun cancelScheduledUserAlerts() {
        getSharedPreferences("CheckInPrefs", MODE_PRIVATE)
            .edit {
                putBoolean("has_checked_in", true)
            }

        userReminderRunnable?.let {
            handler.removeCallbacks(it)
        }
        userReminderRunnable = null
    }

    // Replace this with actual notification logic
    private fun sendUserNotification(message: String) {
        Log.d("MainActivity", "USER NOTIFICATION: $message")
        // send notification using NotificationManager
        //firebase notification
    }

    private fun trackSkippedAlert() {
        val prefs = getSharedPreferences("CheckInPrefs", MODE_PRIVATE)
        val skips = prefs.getInt("skipped_alerts", 0)

        if (skips < 2) {
            prefs.edit { putInt("skipped_alerts", skips + 1) }
            Log.d("MainActivity", "Skipped alert count: ${skips + 1}")
            scheduleUserReminder()
        } else {
            Log.d("MainActivity", "Third skip! Sending emergency alert.")
            sendEmergencyAlertAfter3Skips()
        }
    }

    private fun sendEmergencyAlertAfter3Skips() {
        val username = sessionManagement.getUserName() ?: "This user"
        val message = "$username has skipped his daily check-ins for 3 times. Please check by yourself."
        sendEmergencyNotificationToContacts(message)
    }

    private fun sendEmergencyNotificationToContacts(message: String) {
        // Call your backend API or use SMS/Email to notify emergency contacts
        Log.d("MainActivity", "EMERGENCY ALERT: $message")
        if (BaseApplication.isOnline(this)) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.sendEmergencyMessageRequest().collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> handleSuccessApiResponse(it.data.toString())
                        is NetworkResult.Error -> showAlert(it.message, false)
                    }
                }
            }
        } else {
            AlertUtils.showAlert(this, MessageClass.networkError, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessApiResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, HomeModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.status == true) {
                Toast.makeText(this, "Emergency message sent to contacts ", Toast.LENGTH_LONG).show()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun sendEmergencyAlertImmediately() {
        val username = sessionManagement.getUserName() ?: "This user"
        val message = "$username is not feeling well. Please check immediately."
        sendEmergencyNotificationToContacts(message)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(this, "Please enable location services", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            } else {
                // User denied permission — don't allow access
                Toast.makeText(this, "Location permission is required to use this app", Toast.LENGTH_LONG).show()
                showPermissionBlockedDialog()
            }
        }
    }

    private fun showPermissionBlockedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs location permission to work. Please allow it in settings.")
            .setCancelable(false)
            .setPositiveButton("Grant") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .show()
    }



    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude
                locationViewModel.setLocation(lat, lng)
//                Log.d("FUSED", "Lat: $lat, Lng: $lng")
//                Toast.makeText(this, "Lat: $lat, Lng: $lng", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to get location: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clickListener(){
        binding.layprofile.setOnClickListener {
            val bundle = bundleOf("source" to "main")
            findNavController(R.id.frameLayoutMain).navigate(R.id.homeProfileFragment)
        }

        binding.laycheckhistory.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.checkHistoryFragment)
        }

        binding.layhome.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.homeFragment)
        }

        binding.laymap.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.mapFullScreenFragment)
        }

        binding.laysetting.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.settingFragment)
        }

        binding.imgChatBoot.setOnClickListener {
            alertBoxChatBox()
        }


        // topShow Icon Event

        binding.imgprofile.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.homeProfileFragment)
        }

        binding.imgcheckhistory.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.checkHistoryFragment)
        }

        binding.imghome.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.homeFragment)
        }

        binding.imgmap.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.mapFullScreenFragment)
        }

        binding.imgsetting.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.settingFragment)
        }

        binding.imgChatBoot.setOnClickListener {
            alertBoxChatBox()
        }
    }

    private fun setUI() {
        binding.layprofile.visibility = View.VISIBLE
        binding.laycheckhistory.visibility = View.VISIBLE
        binding.layhome.visibility = View.INVISIBLE
        binding.laymap.visibility = View.VISIBLE
        binding.laysetting.visibility = View.VISIBLE
        binding.imgprofile.visibility = View.GONE
        binding.imgcheckhistory.visibility = View.GONE
        binding.imghome.visibility = View.VISIBLE
        binding.imgmap.visibility = View.GONE
        binding.imgsetting.visibility = View.GONE
    }


    /*private fun alertBoxChatBox() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_chat_boot)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        val imgSend = dialog.findViewById<ImageView>(R.id.img_send)
        val edText = dialog.findViewById<EditText>(R.id.ed_text)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.rv_messages)

        val chatMessages = mutableListOf<ChatMessage>()
        val adapter = ChatAdapter(sessionManagement.getUserId().toString())

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        dialog.show()

        imgClose.setOnClickListener {
            dialog.dismiss()
        }

        // Observe chatbot response only once
        lifecycleScope.launch {
            viewModel.chatBotResponse.collect { result ->

                when (result) {
                    is NetworkResult.Success -> {
                        val reply =
                            result.data?.get("response")?.asString ?: "How can I help you?"
                        chatMessages.add(ChatMessage(reply, isUser = false))
                        adapter.notifyItemInserted(chatMessages.size - 1)
                        recyclerView.scrollToPosition(chatMessages.size - 1)
                    }

                    is NetworkResult.Error -> {
                        Toast.makeText(
                            this@MainActivity,
                            result.message ?: "Unknown error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                //  only once
                BaseApplication.dismissDialog()
            }
        }


        imgSend.setOnClickListener {
            val userMsg = edText.text.toString().trim()
            if (userMsg.isNotEmpty()) {
                // Show user message
                chatMessages.add(ChatMessage(userMsg, isUser = true))
                adapter.notifyItemInserted(chatMessages.size - 1)
                recyclerView.scrollToPosition(chatMessages.size - 1)
                edText.text.clear()

                // Call API via ViewModel
                viewModel.fetchChatBotResponse(userMsg)
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    private fun alertBoxChatBox() {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_chat_boot)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        val imgSend = dialog.findViewById<ImageView>(R.id.img_send)
        val edText = dialog.findViewById<EditText>(R.id.ed_text)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.rv_messages)

        val currentUserId = sessionManagement.getUserId().toString()

        val chatMessages = mutableListOf<ChatMessage>()
        val adapter = ChatAdapter(currentUserId)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        dialog.show()

        imgClose.setOnClickListener { dialog.dismiss() }

        //  SAFE FLOW COLLECTOR
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatBotResponse.collect { result ->

                    when (result) {
                        is NetworkResult.Success -> {
                            val reply = result.data
                                ?.getAsJsonObject("data")
                                ?.get("reply")
                                ?.asString
                                ?: "How can I help you?"

                            chatMessages.add(
                                ChatMessage(
                                    message = reply,
                                    senderId = "BOT"
                                )
                            )

                            adapter.submitList(chatMessages)
                            recyclerView.scrollToPosition(chatMessages.size - 1)
                        }

                        is NetworkResult.Error -> {
                            Toast.makeText(
                                this@MainActivity,
                                result.message ?: "Unknown error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    BaseApplication.dismissDialog() // ✅ only once
                }
            }
        }

        imgSend.setOnClickListener {
            val userMsg = edText.text.toString().trim()

            if (userMsg.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //  Add user message
            chatMessages.add(
                ChatMessage(
                    message = userMsg,
                    senderId = currentUserId
                )
            )

            adapter.submitList(chatMessages)
            recyclerView.scrollToPosition(chatMessages.size - 1)
            edText.text.clear()

            if (BaseApplication.isOnline(this)) {
                BaseApplication.openDialog()
                viewModel.fetchChatBotResponse(userMsg)
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("WrongViewCast")
    fun setImageShowTv(): ImageView? {
        return findViewById(R.id.img_home_show)
    }

    @SuppressLint("WrongViewCast")
    fun setBottomLayout(): RelativeLayout? {
        return findViewById(R.id.lay_bottom_main)
    }

    private fun sideBar() {
        val navigationView: NavigationView = binding.root.findViewById(R.id.navigation_side_nav_bar)
        val tvMyProfile = navigationView.findViewById<TextView>(R.id.tv_my_profile)
        val tvCheckInHistory = navigationView.findViewById<TextView>(R.id.tv_check_in_history)
        val tvAlert = navigationView.findViewById<TextView>(R.id.tv_alert)
        val tvLocationSharing = navigationView.findViewById<TextView>(R.id.tv_Location_Sharing)
        val tvEmergencyContacts = navigationView.findViewById<TextView>(R.id.tv_emergency_contacts)
        val tvLogout = navigationView.findViewById<TextView>(R.id.tv_logout)
        val tvEmergencyText = navigationView.findViewById<TextView>(R.id.tv_emergency_text)
        val tvWatchOverMe = navigationView.findViewById<TextView>(R.id.tv_watch_over_me)
        val tvPrivacyPolicy = navigationView.findViewById<TextView>(R.id.tv_privacy_policy)
        val tvHelp = navigationView.findViewById<TextView>(R.id.tv_Help)
        val tvTermsAmpConditions =
            navigationView.findViewById<TextView>(R.id.tv_terms_amp_conditions)
        val tvHelpingNeighbors = navigationView.findViewById<TextView>(R.id.tv_helping_neighbors)
        val tvAddedContacts = navigationView.findViewById<TextView>(R.id.tv_added_contacts)
        val layKnowMore = navigationView.findViewById<LinearLayout>(R.id.lay_knowmore)
        val layTutorials = navigationView.findViewById<LinearLayout>(R.id.lay_tutorials)
        val layDrop = navigationView.findViewById<LinearLayout>(R.id.lay_drop)
        val tvHealthAlert = navigationView.findViewById<TextView>(R.id.tv_health_alert)

        val tvText = navigationView.findViewById<TextView>(R.id.tv_text)
        val ivProfileSideNav =
            navigationView.findViewById<CircleImageView>(R.id.iv_profile_side_nav)

        sessionManagement.getUserName().let {
            tvText.text = it
        }

        sessionManagement.getUserProfile().let {
            Glide.with(this)
                .load(BuildConfig.BASE_URL + it)
                .placeholder(R.drawable.user_img_icon)
                .error(R.drawable.user_img_icon)
                .into(ivProfileSideNav)
        }

        var status = false

        layDrop.setOnClickListener {
            if (status) {
                status = false
                layKnowMore.visibility = View.GONE
                layTutorials.visibility = View.GONE
                tvHelp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.help_icon_down, 0);
            } else {
                layKnowMore.visibility = View.VISIBLE
                layTutorials.visibility = View.VISIBLE
                tvHelp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.help_icon_end, 0);

                status = true
            }
        }

        tvMyProfile.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.homeProfileFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        tvCheckInHistory.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.checkHistoryFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        tvAlert.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.selfAlertFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        tvLocationSharing.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.locationShareFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        tvEmergencyContacts.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.emergencyContactsFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        tvEmergencyText.setOnClickListener {
//            getEmergencyMessage()
            findNavController(R.id.frameLayoutMain).navigate(R.id.emergencyTextFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        tvWatchOverMe.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.watchOverMeFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }


        tvTermsAmpConditions.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.termsAndConditionFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)

        }

        tvPrivacyPolicy.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.privacyPolicyFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        tvHelpingNeighbors.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.helpingNeighborsFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }


        tvAddedContacts.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.addedContactsFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        layKnowMore.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.knowMoreFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        layTutorials.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.tutorialsFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        tvHealthAlert.setOnClickListener {
            findNavController(R.id.frameLayoutMain).navigate(R.id.healthFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        tvLogout.setOnClickListener {
            alertBoxLogOut()
        }
    }

    private fun getEmergencyMessage() {
        if (BaseApplication.isOnline(this)) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getEmergencyMessage().collect {
                    BaseApplication.dismissDialog()
                    handleGetApiResponse(it)
                }
            }
        } else {
            AlertUtils.showAlert(this, MessageClass.networkError, false)
        }
    }

    private fun handleGetApiResponse(it: NetworkResult<JsonObject>) {
        when (it) {
            is NetworkResult.Success -> handleSuccessGetApiResponse(it.data.toString())
            is NetworkResult.Error -> showAlert(it.message, false)
            else -> showAlert(it.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(this, message, status)
    }

    private fun handleError(code: Int?, message: String?) {
        if (code == MessageClass.deactivatedUser || code == MessageClass.deletedUser) {
            showAlert(message, true)
        } else {
            findNavController(R.id.frameLayoutMain).navigate(R.id.emergencyTextFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            showAlert(message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessGetApiResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, EmergencyTextMszModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.status == true) {
                findNavController(R.id.frameLayoutMain).navigate(R.id.emergencyTextSubmitFragment)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun alertBoxLogOut() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams
        val tvOK = dialog.findViewById<TextView>(R.id.tvOK)
        val tvNo = dialog.findViewById<TextView>(R.id.tvNo)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)


        tvOK.setOnClickListener {
            dialog.dismiss()
            sessionManagement.logOut()
            val intent = Intent(this@MainActivity, AuthActivity::class.java)
            intent.putExtra("openScreen", "Login")
            startActivity(intent)
            finish()
        }

        tvNo.setOnClickListener {
            dialog.dismiss()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }

    }

    fun getDrawerLayout(): DrawerLayout {
        return findViewById(R.id.drawer_layout)
    }

    @SuppressLint("WrongViewCast")
    fun setImgseetimer(): ImageView {
        return findViewById(R.id.img_seetimer)
    }

    @SuppressLint("WrongViewCast")
    fun setImgLocation(): ImageView {
        return findViewById(R.id.imglocation)
    }

    @SuppressLint("WrongViewCast")
    fun setHeading(): TextView {
        return findViewById(R.id.tv_heading_main)
    }

    @SuppressLint("WrongViewCast")
    fun setTitle(): TextView {
        return findViewById(R.id.tv_title_main)
    }

    @SuppressLint("WrongViewCast")
    fun setCircle(): LinearLayout {
        return findViewById(R.id.showcircle)
    }

    fun setFooter(): ConstraintLayout {
        return findViewById(R.id.footer)
    }

    @SuppressLint("WrongViewCast")
    fun setSubTitle(): TextView {
        return findViewById(R.id.tv_sub_title_main)
    }

    @SuppressLint("WrongViewCast")
    fun setImgHomeShow(): ImageView {
        return findViewById(R.id.img_home_show)
    }

    @SuppressLint("WrongViewCast")
    fun setImgChatBoot(): ImageView {
        return findViewById(R.id.img_chat_boot)
    }

    fun setFooter(type: String) {
        if (type.equals("userProfile", true)) {
            binding.layprofile.visibility = View.INVISIBLE
            binding.laycheckhistory.visibility = View.VISIBLE
            binding.layhome.visibility = View.VISIBLE
            binding.laymap.visibility = View.VISIBLE
            binding.laysetting.visibility = View.VISIBLE
            binding.imgprofile.visibility = View.VISIBLE
            binding.imgcheckhistory.visibility = View.GONE
            binding.imghome.visibility = View.GONE
            binding.imgmap.visibility = View.GONE
            binding.imgsetting.visibility = View.GONE
        }

        if (type.equals("checkHistory", true)) {
            binding.layprofile.visibility = View.VISIBLE
            binding.laycheckhistory.visibility = View.INVISIBLE
            binding.layhome.visibility = View.VISIBLE
            binding.laymap.visibility = View.VISIBLE
            binding.laysetting.visibility = View.VISIBLE
            binding.imgprofile.visibility = View.GONE
            binding.imgcheckhistory.visibility = View.VISIBLE
            binding.imghome.visibility = View.GONE
            binding.imgmap.visibility = View.GONE
            binding.imgsetting.visibility = View.GONE
        }

        if (type.equals("home", true)) {
            binding.layprofile.visibility = View.VISIBLE
            binding.laycheckhistory.visibility = View.VISIBLE
            binding.layhome.visibility = View.INVISIBLE
            binding.laymap.visibility = View.VISIBLE
            binding.laysetting.visibility = View.VISIBLE
            binding.imgprofile.visibility = View.GONE
            binding.imgcheckhistory.visibility = View.GONE
            binding.imghome.visibility = View.VISIBLE
            binding.imgmap.visibility = View.GONE
            binding.imgsetting.visibility = View.GONE
        }

        if (type.equals("map", true)) {
            binding.layprofile.visibility = View.VISIBLE
            binding.laycheckhistory.visibility = View.VISIBLE
            binding.layhome.visibility = View.VISIBLE
            binding.laymap.visibility = View.INVISIBLE
            binding.laysetting.visibility = View.VISIBLE
            binding.imgprofile.visibility = View.GONE
            binding.imgcheckhistory.visibility = View.GONE
            binding.imghome.visibility = View.GONE
            binding.imgmap.visibility = View.VISIBLE
            binding.imgsetting.visibility = View.GONE
        }

        if (type.equals("setting", true)) {
            binding.layprofile.visibility = View.VISIBLE
            binding.laycheckhistory.visibility = View.VISIBLE
            binding.layhome.visibility = View.VISIBLE
            binding.laymap.visibility = View.VISIBLE
            binding.laysetting.visibility = View.INVISIBLE
            binding.imgprofile.visibility = View.GONE
            binding.imgcheckhistory.visibility = View.GONE
            binding.imghome.visibility = View.GONE
            binding.imgmap.visibility = View.GONE
            binding.imgsetting.visibility = View.VISIBLE
        }
    }

}