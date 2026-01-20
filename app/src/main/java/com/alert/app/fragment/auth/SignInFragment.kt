package com.alert.app.fragment.auth

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.RememberPasswordListAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.base.SessionManagement
import com.alert.app.databinding.FragmentSignInBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.MessageClass
import com.alert.app.viewmodel.loginviewmodel.LoginViewModel
import com.alert.app.viewmodel.loginviewmodel.apiresponse.DataModel
import com.alert.app.viewmodel.loginviewmodel.apiresponse.LoginRootModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.mykameal.planner.fragment.authfragment.login.model.RememberMe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private var binding: FragmentSignInBinding?=null
    private var checkUncheck:Boolean=false
    private lateinit var sessionManagement: SessionManagement
    private lateinit var googleSignInClient: GoogleSignInClient
    private var personName: String? = ""
    private var personGivenName: String? = ""
    private var personFamilyName: String? = ""
    private var personEmail: String? = ""
    private var personId: String = ""
    lateinit var adapter :RememberPasswordListAdapter
    private lateinit var viewModel: LoginViewModel
    private var token: String = ""
    private var isFirstTimeTouched = true
    private enum class SignInType {
        EMAIL, PHONE
    }
    private var countryCode:String = ""

    private var signInType = SignInType.EMAIL

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignInBinding.inflate(layoutInflater, container, false)
        sessionManagement= SessionManagement(requireContext())
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        countryCode = binding!!.ccp.selectedCountryCodeWithPlus

        logOutGmail()
        // openRemember()

        // In side this only call the event listener
        callEvent()
        // In side this only call the event back button
        callBackEvent()

        return binding!!.root
    }

    private fun callBackEvent() {
        // This line use for system back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateToWelcomeScreen()
                }
            })
    }

    private fun callEvent() {

        selectEmail()

        binding?.tvEmail?.setOnClickListener {
            selectEmail()
        }

        binding?.tvPhone?.setOnClickListener {
            selectPhone()
        }

        binding!!.tvSignUpText.setOnClickListener{
            findNavController().navigate(R.id.signUpFragment)
        }

        binding!!.tvForgotPassword.setOnClickListener {
            val bundle = Bundle().apply {
                putString("signInType", signInType.name)
            }

            findNavController().navigate(R.id.forgotPassword, bundle)
        }


        binding!!.backBtn.setOnClickListener {
            navigateToWelcomeScreen()
        }

//        binding!!.ccp.setOnCountryChangeListener {
//            countryCode = binding!!.ccp.selectedCountryCode
////            val iso = binding!!.ccp.selectedCountryNameCode
//        }

        binding!!.imgCheckUncheckRemember.setOnClickListener{
            if (checkUncheck){
                checkUncheck=false
                binding!!.imgCheckUncheckRemember.setImageResource(R.drawable.uncheck_blue_tick_icon)
            }else{
                checkUncheck=true
                binding!!.imgCheckUncheckRemember.setImageResource(R.drawable.check_blue_tick_icon)
            }
        }

        binding!!.tvSignInButton.setOnClickListener{
            if (!BaseApplication.isOnline(requireContext())) {
                BaseApplication.alertError(context, MessageClass.networkError, false)
                return@setOnClickListener
            }

            if (!isValidate()) return@setOnClickListener

            binding!!.tvSignInButton.isEnabled = false

            when (signInType) {
                SignInType.EMAIL -> {
                    loginApi()
                }

                SignInType.PHONE -> {
                    loginPhoneApi()
                }
            }
        }

        binding!!.imgGoogle.setOnClickListener {
            if (BaseApplication.isOnline(requireContext())){
                signIn()
            }else{
                BaseApplication.alertError(context, MessageClass.networkError,false)
            }
        }

        binding!!.etSignEmail.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus && isFirstTimeTouched) { // Only trigger on first touch
                val data: String = sessionManagement.getRememberMe().toString()
                if (data.isNotEmpty()) {
                    showRememberDialog()
                }
                isFirstTimeTouched = false // Set flag to false so it doesn't trigger again
            }
        }


        binding!!.eyehideIcon.setOnClickListener {
            binding!!.eyeIcon.visibility= View.VISIBLE
            binding!!.eyehideIcon.visibility= View.GONE
            binding!!.etSignPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding!!.etSignPassword.setSelection(binding!!.etSignPassword.text.length)
        }

        binding!!.eyeIcon.setOnClickListener {
            binding!!.etSignPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding!!.eyeIcon.visibility= View.GONE
            binding!!.eyehideIcon.visibility= View.VISIBLE
            binding!!.etSignPassword.setSelection(binding!!.etSignPassword.text.length)
        }
    }
    private fun selectEmail() {
        signInType = SignInType.EMAIL
        binding?.let {
            // Tab UI
            it.tvEmail.setBackgroundResource(R.drawable.bg_segment_selected)
            it.tvEmail.setTextColor(Color.WHITE)

            it.tvPhone.background = null
            it.tvPhone.setTextColor(Color.BLACK)

            // Input switch
            it.layoutEmail.visibility = View.VISIBLE
            it.layoutPhone.visibility = View.GONE

            // Hint + icon
            it.etSignEmail.hint = "Email"
            it.imageFullName.setImageResource(R.drawable.group_5)

            // Focus
            it.etSignEmail.requestFocus()
        }
    }
    private fun selectPhone() {
        signInType = SignInType.PHONE
        binding?.let {
            it.tvPhone.setBackgroundResource(R.drawable.bg_segment_selected)
            it.tvPhone.setTextColor(Color.WHITE)

            it.tvEmail.background = null
            it.tvEmail.setTextColor(Color.BLACK)

            // Input switch
            it.layoutEmail.visibility = View.GONE
            it.layoutPhone.visibility = View.VISIBLE

            // Hint
            it.etSignInPhone.hint = "Phone Number"

            // Focus
            it.tvSignInUsing.requestFocus()
        }
    }

    private fun loginApi() {
        BaseApplication.openDialog()
        lifecycleScope.launch {
            viewModel.loginRequest({ response ->
                BaseApplication.dismissDialog()
                handleApiResponse(response, "login")
            },binding!!.etSignEmail.text.toString().trim(),
                binding!!.etSignPassword.text.toString().trim(),token,"Android")
        }
    }
    private fun loginPhoneApi() {

        val phone = binding!!.etSignInPhone.text.toString().trim()
        val finalCountryCode =
            countryCode.ifEmpty { binding!!.ccp.selectedCountryCodeWithPlus }

        BaseApplication.openDialog()
        lifecycleScope.launch {
            viewModel.loginPhoneRequest(
                { response ->
                    BaseApplication.dismissDialog()
                    handleApiResponse(response, "login")
                },
                phone,
                finalCountryCode,
                binding!!.etSignPassword.text.toString().trim(),
                token,
                "Android"
            )
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>, dataType: String) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString(),dataType)
            is NetworkResult.Error -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String, dataType: String) {
        try {
            Log.d("@@@ Api Response", "message: $data")
            when (dataType) {
                "login","socialLogin" -> {
                    val apiModel = Gson().fromJson(data, LoginRootModel::class.java)
                    if (apiModel.code == 200 && apiModel.status) {
                        apiModel.data?.let { showDataUi(it,dataType) }?: run {
                            showAlert(MessageClass.apiError, false)
                        }
                    } else {
                        handleError(apiModel.code,apiModel.message)
                    }
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


    private fun handleError(code:Int,msg:String){
        if (code==MessageClass.deactivatedUser || code==MessageClass.deletedUser){
            showAlert(msg, true)
        }else{
            showAlert(msg, false)
        }
    }


    private fun showDataUi(apiModel: DataModel, dataType: String) {
        try {
            apiModel.let {
                if (!dataType.equals("socialLogin",true)){
                    if (checkUncheck){
                        rememberIdPassword()
                    }
                }
                apiModel.jwt_token.let { sessionManagement.setUserToken(it.toString()) }
//                if (apiModel.profile_status.equals("No",true)){
////                    findNavController().navigate(R.id.profileFragment)
//                    sessionManagement.setProfileScreen("signup")
//                    findNavController().navigate(R.id.homeProfileFragment2)
//                }else{
                    sessionManagement.setLoginSession(true)
                    sessionManagement.setProfileScreen("login")
                    apiModel.name.let { sessionManagement.setUserName(it.toString()) }
                    apiModel.email.let { sessionManagement.setUserEmail(it.toString()) }
                    apiModel.profile_pic.let { sessionManagement.setUserProfile(it.toString()) }
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
//                }
            }
        }catch (e:Exception){
            showAlert(e.message, false)
        }
    }

/*    private fun showDataUi(apiModel: DataModel, dataType: String) {
        try {
            if (!dataType.equals("socialLogin", true)) {
                if (checkUncheck) {
                    rememberIdPassword()
                }
            }

            sessionManagement.setUserToken(apiModel.jwt_token.orEmpty())
            sessionManagement.setLoginSession(true)
            sessionManagement.setProfileScreen("login")
            sessionManagement.setUserName(apiModel.name.orEmpty())
            sessionManagement.setUserEmail(apiModel.email.orEmpty())
            sessionManagement.setUserProfile(apiModel.profile_pic.orEmpty())

            //  FIREBASE LOGIN FIRST
            FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnSuccessListener {
                    //  Now Firebase user exists
                    saveUserToFirestore(apiModel)

                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .addOnFailureListener {
                    showAlert("Firebase login failed", false)
                }

        } catch (e: Exception) {
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
    }*/



    // This is common function for show the alert box
    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(context, message,status)
    }

    private fun openRemember() {
        val data: String = sessionManagement.getRememberMe().toString()
        if (data.isNotEmpty()){
            showRememberDialog()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showRememberDialog() {
        val filterList: MutableList<RememberMe> = mutableListOf()
        var email =""
        var pass =""
        val dialogRemember: BottomSheetDialog = context?.let { BottomSheetDialog(it,R.style.BottomSheetDialog) }!!
        dialogRemember.setContentView(R.layout.dialog_remember)
        dialogRemember.window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val windowAttributes = dialogRemember.window!!.attributes
        windowAttributes.gravity = Gravity.BOTTOM
        dialogRemember.window!!.attributes = windowAttributes
        val rvRememberData = dialogRemember.findViewById<RecyclerView>(R.id.rvRememberData)
        val tvSignInButton = dialogRemember.findViewById<TextView>(R.id.tvSignInButton)
        dialogRemember.show()
        dialogRemember.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val data: String = sessionManagement.getRememberMe().toString()

        if (data.isNotEmpty()) {
            val objectList = Gson().fromJson(data, Array<RememberMe>::class.java).asList()

            // Populate the filterList with the entire objectList (since type is not used anymore)
            filterList.addAll(objectList)

            adapter = RememberPasswordListAdapter(requireContext(), filterList) { pos ->
                email = filterList[pos].email
                pass = filterList[pos].pass
                // Set all statuses to false
                filterList.forEach { it.status = false }
                // Set the selected position to true, if it's within bounds
                if (pos in filterList.indices) {
                    filterList[pos].status = true
                }

                adapter.updateList(filterList)
            }
            rvRememberData?.adapter = adapter
        }

        tvSignInButton?.setOnClickListener {
            if (email.equals("",true) || pass.equals("",true)){
                Toast.makeText(requireContext(),"Please Select at least one.",Toast.LENGTH_LONG).show()
            }else{
                dialogRemember.dismiss()
                binding!!.etSignEmail.setText(email)
                binding!!.etSignPassword.setText(pass)
            }



        }
    }

    private fun rememberIdPassword() {
        val data: String? = sessionManagement.getRememberMe()
        var mutableList: MutableList<RememberMe> = ArrayList()
        if (!data.isNullOrEmpty()) {
            val objectList: List<RememberMe> = Gson().fromJson(data, Array<RememberMe>::class.java).asList()
            mutableList = objectList.toMutableList()
        }
        val email = binding!!.etSignEmail.text.toString()
        val password = binding!!.etSignPassword.text.toString()
        var found = false
        for (item in mutableList) {
            if (item.email.equals(email,true)) {
                item.pass = password  // Update password if email exists
                found = true
                break
            }
        }
        if (!found) {
            mutableList.add(RememberMe(email, password, false)) // Add new entry if not found
        }
        sessionManagement.setRememberMe(mutableList)
    }

    // Configure Google Sign-In
    private fun logOutGmail() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        googleSignInClient.signOut()
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    handleSignInResult(account)
                }
            } catch (e: ApiException) {
                BaseApplication.alertError(context, "Sign-in failed: ${e.message}",false)
            }
        }
    }

    // Function to start Google Sign-In
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    // This function handle the social login request
    private fun handleSignInResult(account: GoogleSignInAccount?) {
        try {
            if (account != null) {
                personName = account.displayName
                personGivenName = account.givenName
                personFamilyName = account.familyName
                personEmail = account.email
                personId = account.id.toString()
                Log.d("GamailUserid", "result$personId")
                Log.d("personName", "data$personName")
                Log.d("personId", "data....$personId")
                Log.d("personEmail", "data....$personEmail")
                logOutGmail()
                socialLogin()
            }
        } catch (e: ApiException) {
            logOutGmail()
            showAlert(e.message, false)
            Log.d("massage", e.toString())
        }
    }

    // This function call for social login request
    private fun socialLogin() {
        BaseApplication.openDialog()
        lifecycleScope.launch {
            viewModel.socialLoginRequest({ response ->
                BaseApplication.dismissDialog()
                handleApiResponse(response, "socialLogin")
            },personEmail.toString(),"Android",token)
        }
    }

    // This function call for validate all field when user enter the details
    private fun isValidate(): Boolean {

        val password = binding!!.etSignPassword.text.toString().trim()

        if (signInType == SignInType.EMAIL) {

            val email = binding!!.etSignEmail.text.toString().trim()
            val emailPattern = Pattern.compile(MessageClass.emailRegulerExpression)

            if (email.isEmpty()) {
                BaseApplication.alertError(
                    context,
                    MessageClass.emailError,
                    false
                )
                return false
            }

            if (!emailPattern.matcher(email).matches()) {
                BaseApplication.alertError(
                    context,
                    "Enter valid email address",
                    false
                )
                return false
            }

        } else { // PHONE

            val phone = binding!!.etSignInPhone.text.toString().trim()
            val phonePattern = Pattern.compile("^[0-9]{10}$")

            if (phone.isEmpty()) {
                BaseApplication.alertError(
                    context,
                    "Phone number required",
                    false
                )
                return false
            }

            if (!phonePattern.matcher(phone).matches()) {
                BaseApplication.alertError(
                    context,
                    "Enter valid 10 digit phone number",
                    false
                )
                return false
            }
        }

        if (password.isEmpty()) {
            BaseApplication.alertError(
                context,
                MessageClass.passwordError,
                false
            )
            return false
        }

        return true
    }


    // This function call for validate user number
    private fun validNumber(): Boolean {
        val email:String = binding!!.etSignEmail.text.toString().trim()
        if(email.length!=10){
            return false
        }
        var onlyDigits = true
        for (element in email) {
            if (!Character.isDigit(element)) {
                onlyDigits = false
                break
            }
        }
        return onlyDigits
    }

    // This function call for move to previous screen
    private fun navigateToWelcomeScreen() {
        requireActivity().finish()
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

    fun payment(){
       /* val payUPaymentParams = PayUPaymentParams.Builder()
            .setAmount("10.00") // amount in INR
            .setIsProduction(false) // true = live, false = test
            .setKey("your_merchant_key")
            .setProductInfo("Test Product")
            .setPhone("9876543210")
            .setTransactionId("TXN_${System.currentTimeMillis()}")
            .setFirstName("John")
            .setEmail("john@example.com")
            .setSurl("https://yourdomain.com/success") // success callback URL
            .setFurl("https://yourdomain.com/failure") // failure callback URL
            .build()*/

    }

}