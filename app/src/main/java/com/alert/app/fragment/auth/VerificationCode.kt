package com.alert.app.fragment.auth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.base.BaseApplication
import com.alert.app.base.SessionManagement
import com.alert.app.databinding.FragmentVerificationCodeBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.MessageClass
import com.alert.app.viewmodel.loginviewmodel.apiresponse.DataModel
import com.alert.app.viewmodel.loginviewmodel.apiresponse.LoginRootModel
import com.alert.app.viewmodel.verificationviewmodel.VerificationOtpViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

@AndroidEntryPoint
class VerificationCode : Fragment() {
    private var binding: FragmentVerificationCodeBinding?=null
    private val startTimeInMillis: Long = 120000
    private var mTimeLeftInMillis = startTimeInMillis
    private var screenType:String?=""
    private var email:String?=""
    private var phone:String?=""
    private var name:String?=""
    private var pass:String?=""
    private var token: String = ""
    private var signUpType: String = ""
    private var countryCode: String = ""
    private lateinit var sessionManagement: SessionManagement
    private lateinit var viewModel: VerificationOtpViewModel
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    private var latitude: String = "0"
    private var longitude: String = "0"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVerificationCodeBinding.inflate(layoutInflater, container, false)
        sessionManagement=SessionManagement(requireContext())

        viewModel = ViewModelProvider(this)[VerificationOtpViewModel::class.java]

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        if (arguments!=null){
            screenType= requireArguments().getString("screenType")
            email= requireArguments().getString("email")
            phone= requireArguments().getString("phone")
            countryCode = requireArguments().getString("countryCode").toString()
            signUpType = requireArguments().getString("signUpType").toString()
            Log.d("TESTING_TYPE","Email is "+email+"\n Phone is"+phone)
        }
        if (screenType.equals("SignUp")){
            name= requireArguments().getString("name")
            pass= requireArguments().getString("pass")
            signUpType = requireArguments().getString("signUpType").toString()
            countryCode = requireArguments().getString("countryCode").toString()
        }

        // In side this only call the event listener
        callEvent()
        // In side this only call the event back button
        callBackEvent()

        return binding!!.root
    }

    private fun callBackEvent() {
        binding!!.imageBackVerification.setOnClickListener {
            findNavController().navigateUp()
        }

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun callEvent() {

        binding!!.tvVerificationButton.setOnClickListener{
            if (BaseApplication.isOnline(requireContext())){
                if (binding!!.otpVerificationBox.otp!!.isEmpty()) {
                    BaseApplication.alertError(context, MessageClass.emptyOtp,false)
                }else if (binding!!.otpVerificationBox.otp!!.length != 4){
                    BaseApplication.alertError(context, MessageClass.correctOtp,false)
                } else{
                    otpVerifyApi()
                }
            }else{
                BaseApplication.alertError(context, MessageClass.networkError,false)
            }
        }

        binding!!.tvResendVerification.setOnClickListener{
            if (BaseApplication.isOnline(requireContext())){
                reSendApi()
            }else{
                BaseApplication.alertError(context, MessageClass.networkError,false)
            }
        }

    }

    private fun reSendApi() {
        if (screenType.equals("SignUp")){
            resendOtpSignUpApi()
        }else{
            resendForgotPasswordApi()
        }

    }

    private fun resendOtpSignUpApi() {
        BaseApplication.openDialog()
        lifecycleScope.launch {
            viewModel.resendOtpRequest({ response ->
                BaseApplication.dismissDialog()
                handleApiResponse(response, "signupResend")
            }, type = "signup",email = email, phone = "+"+countryCode+phone )
        }
    }

    private fun resendForgotPasswordApi() {
        BaseApplication.openDialog()
        lifecycleScope.launch {
            if (signUpType == "EMAIL"){
                viewModel.resendOtpRequest({ response ->
                    BaseApplication.dismissDialog()
                    handleApiResponse(response,"forgotResend")
                },"forgot_password",email,null)
            }else{
                viewModel.resendOtpRequest({ response ->
                    BaseApplication.dismissDialog()
                    handleApiResponse(response,"forgotResend")
                }, "forgot_password" ,null,"+"+countryCode+phone)
            }
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>,type:String) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString(),type)
            is NetworkResult.Error -> showAlert(result.message, false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String,type:String) {
        try {
            Log.d("@@@ Api Response", "message: $data")
            when(type){
                "forgotResend" ,"signupResend"->{
                    val apiModel = Gson().fromJson(data, LoginRootModel::class.java)
                    if (apiModel.code == 200 && apiModel.status) {
                        Toast.makeText(requireContext(),"OTP :- "+apiModel.data?.otp,Toast.LENGTH_LONG).show()
                        binding!!.otpVerificationBox.setOTP("")
                        startTime()
                    } else {
                        showAlert(apiModel.message, false)
                    }
                }
                "forgotPasswordOtpVerify"->{
                    val apiModel = Gson().fromJson(data, LoginRootModel::class.java)
                    if (apiModel.code == 200 && apiModel.status) {
                        val bundle=Bundle()
                        bundle.putString("user_id",apiModel.data?.user_id.toString())
                        bundle.putString("otp",binding!!.otpVerificationBox.otp)
                        bundle.putString("email",email)
                        bundle.putString("phone",phone)
                        bundle.putString("signUpType",signUpType)
                        findNavController().navigate(R.id.resetPassword,bundle)
                    } else {
                        showAlert(apiModel.message, false)
                    }
                }
                "signUpOtpVerify"->{
                    val apiModel = Gson().fromJson(data, LoginRootModel::class.java)
                    if (apiModel.code == 200 && apiModel.status) {
                        apiModel.data?.let { showDataUi(it) }?: run {
                            showAlert(MessageClass.apiError, false)
                        }
                    } else {
                        showAlert(apiModel.message, false)
                    }
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showDataUi(apiModel: DataModel) {
        try {
            apiModel.jwt_token.let { sessionManagement.setUserToken(it.toString()) }
            apiModel.name.let { sessionManagement.setUserName(it.toString()) }
            apiModel.email.let { sessionManagement.setUserEmail(it.toString()) }
            apiModel.profile_pic.let { sessionManagement.setUserProfile(it.toString()) }
            openAlertBoxSuccess()
        }catch (e:Exception){
            showAlert(e.message, false)
        }
    }


    private fun saveUserToFirestore(apiModel: DataModel) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val userData = hashMapOf(
            "uid" to user.uid,
            "name" to apiModel.name.orEmpty(),
            "image" to apiModel.profile_pic.orEmpty(),
            "email" to apiModel.email.orEmpty(),
            "online" to true,
            "lastSeen" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d("FIRESTORE", "User saved successfully")
            }
            .addOnFailureListener {
                Log.e("FIRESTORE", "User save failed", it)
            }
    }


    // This is common function for show the alert box
    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(context, message,status)
    }

    private fun otpVerifyApi() {

        if (screenType.equals("SignUp")){
            signUpOtpVerifyApi()
        }else{
            forgotPasswordOtpVerifyApi()
        }
        /*if (screenType.equals("SignUp") *//*|| screenType.equals("Profile")|| screenType.equals("HomeProfile")*//*){
            *//* if (screenType.equals("HomeProfile",true)){
                 sessionManagement.setUserEditable(true)
                 findNavController().navigateUp()
             }else{*//*
            openAlertBoxSuccess()
//                    }
        }else{
            findNavController().navigate(R.id.resetPassword)
        }*/
    }

    private fun forgotPasswordOtpVerifyApi() {
        BaseApplication.openDialog()
        lifecycleScope.launch {
            if (signUpType == "EMAIL"){
                viewModel.forGotOtpVerifyRequest({ response ->
                    BaseApplication.dismissDialog()

                    when (response) {
                        is NetworkResult.Success -> {

                            val data = response.data

                            val jsonObject = JSONObject(data)

                            val status = jsonObject.getBoolean("status")

                            if (status) {
                                val bundle = Bundle()
                                bundle.putString("email", email)
                                bundle.putString("phone", phone)
                                bundle.putString("signUpType", signUpType)
                                findNavController().navigate(R.id.resetPassword, bundle)
                            }
                        }
                        is NetworkResult.Error -> showAlert(response.message, false)
                    }




                }, email,binding!!.otpVerificationBox.otp.toString(),null)
            }else{
                viewModel.forGotOtpVerifyRequest({ response ->
                    BaseApplication.dismissDialog()

                    when (response) {
                        is NetworkResult.Success -> {
                            val data = response.data

                            val jsonObject = JSONObject(data)

                            val status = jsonObject.getBoolean("status")

                            if ( status) {
                                val bundle = Bundle()
                                bundle.putString("email", email)
                                bundle.putString("phone", "+"+countryCode+phone)
                                bundle.putString("signUpType", signUpType)
                                findNavController().navigate(R.id.resetPassword, bundle)
                            }
                        }
                        is NetworkResult.Error -> showAlert(response.message, false)
                    }




                    //handleApiResponse(response,"forgotPasswordOtpVerify")
                }, null,binding!!.otpVerificationBox.otp.toString(),"+"+countryCode+phone)
            }
        }
    }

    private fun signUpOtpVerifyApi() {
        BaseApplication.openDialog()
        lifecycleScope.launch {
            if (signUpType == "EMAIL"){
                viewModel.signupOtpVerifyRequest({ response ->
                    BaseApplication.dismissDialog()
                    handleApiResponse(response,"signUpOtpVerify")
                },binding!!.otpVerificationBox.otp.toString(),email!!,null,token,"Android")
            }else{
                viewModel.signupOtpVerifyRequest({ response ->
                    BaseApplication.dismissDialog()
                    handleApiResponse(response,"signUpOtpVerify")
                },binding!!.otpVerificationBox.otp.toString(),null,countryCode+phone!!,token,"Android")
            }

        }
    }


    @SuppressLint("SetTextI18n")
    private fun openAlertBoxSuccess() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_signup_success)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams
        val tvPwdChangeOK = dialog.findViewById<TextView>(R.id.tvPwdChangeOK)
        val tvText = dialog.findViewById<TextView>(R.id.tv_text)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        if (screenType.equals("SignUp")){
            tvText.text = "Your account has been created \n successfully."
            imgClose.visibility=View.GONE
        }else{
            tvText.text = "Your Email is changed Successfully."
            imgClose.visibility=View.VISIBLE
        }


        tvPwdChangeOK.setOnClickListener {
            if (screenType.equals("SignUp")){
                dialog.dismiss()
                openAllowLocation()
            }else{
                dialog.dismiss()
                findNavController().navigate(R.id.confirmationFragment)
            }

        }

        imgClose.setOnClickListener {
            if (screenType.equals("SignUp")){
                dialog.dismiss()
                openAllowLocation()
            }else{
                dialog.dismiss()
                findNavController().navigate(R.id.confirmationFragment)
            }
        }

    }

    private val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    private val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
    private val LOCATION_REQUEST_CODE = 1001
    private val NOTIFICATION_REQUEST_CODE = 101

    private var openedLocationSettings = false

    override fun onResume() {
        super.onResume()

        if (openedLocationSettings) {
            openedLocationSettings = false

            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (isLocationEnabled) {
                if (ContextCompat.checkSelfPermission(requireContext(), LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                    openAllowNotificationsIfRequired()
                } else {
                    requestPermissions(arrayOf(LOCATION_PERMISSION), LOCATION_REQUEST_CODE)
                }
            }
        }
    }


    private fun openAllowLocation() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_allow_location)
        dialog.setCancelable(false)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        val tvAllowButton = dialog.findViewById<TextView>(R.id.tvAllowButton)
        val tvSkipForNow = dialog.findViewById<TextView>(R.id.tvSkipForNow)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)

        tvAllowButton.setOnClickListener {
            dialog.dismiss()
            checkAndRequestLocationPermission()
        }

        tvSkipForNow.setOnClickListener {
            dialog.dismiss()
            openAllowNotificationsIfRequired()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
            openAllowNotificationsIfRequired()
        }

        dialog.show()
    }

    private fun checkAndRequestLocationPermission() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isLocationEnabled) {
            Toast.makeText(requireContext(), "Please enable device location", Toast.LENGTH_LONG).show()
            openedLocationSettings = true
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                openAllowNotificationsIfRequired()
            } else {
                requestPermissions(arrayOf(LOCATION_PERMISSION), LOCATION_REQUEST_CODE)
            }
        }
    }


    private fun openAllowNotificationsIfRequired() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openAllowNotifications()
        } else {
            navigateToHome()
        }
    }


    private fun openAllowNotifications() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_allow_notifications)
        dialog.setCancelable(false)
        dialog.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        val tvAllowButton = dialog.findViewById<TextView>(R.id.tvAllowButton)
        val tvSkipForNow = dialog.findViewById<TextView>(R.id.tvSkipForNow)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)

        tvAllowButton.setOnClickListener {
            dialog.dismiss()
            if (ContextCompat.checkSelfPermission(requireContext(), NOTIFICATION_PERMISSION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(NOTIFICATION_PERMISSION), NOTIFICATION_REQUEST_CODE)
            } else {
                navigateToHome()
            }
        }

        tvSkipForNow.setOnClickListener {
            dialog.dismiss()
            navigateToHome()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
            navigateToHome()
        }

        dialog.show()
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                // Whether granted or not, move to notification step
                openAllowNotificationsIfRequired()
            }

            NOTIFICATION_REQUEST_CODE -> {
                // Continue regardless of grant or deny
                navigateToHome()
            }
        }
    }

    private fun navigateToHome() {
        sessionManagement.setProfileScreen("signup")
        val bundle = bundleOf("source" to "auth")
        findNavController().navigate(R.id.homeProfileFragment2,bundle)
    }


    private fun startTime() {
        object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis = millisUntilFinished
                binding!!.tvResendVerification.setTextColor(Color.parseColor("#828282"))
                binding!!.tvResendVerification.isEnabled = false
                binding!!.llResendTimer.visibility = View.VISIBLE
                updateCountDownText()
            }

            override fun onFinish() {
                mTimeLeftInMillis = 120000
                binding!!.tvResendVerification.setTextColor(Color.parseColor("#1E60AC"))
                binding!!.llResendTimer.visibility = View.GONE
                binding!!.tvResendVerification.isEnabled = true
            }
        }.start()
    }

    @SuppressLint("SetTextI18n")
    private fun updateCountDownText() {
        val minutes = mTimeLeftInMillis.toInt() / 1000 / 60
        val seconds = mTimeLeftInMillis.toInt() / 1000 % 60
        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        binding!!.tvTimer.text = "$timeLeftFormatted sec"
    }

    // This function call for get fcm token from the FirebaseMessaging
    private fun getDeviceToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String> ->
            if (task.isSuccessful) {
                Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                token = task.result
                // Log and toast
                Log.d(ContentValues.TAG, "Fcm token$token")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        getDeviceToken()
    }

}