package com.alert.app.fragment.auth

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.base.BaseApplication
import com.alert.app.base.SessionManagement
import com.alert.app.databinding.FragmentSignUpBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.MessageClass
import com.alert.app.viewmodel.loginviewmodel.apiresponse.DataModel
import com.alert.app.viewmodel.loginviewmodel.apiresponse.LoginRootModel
import com.alert.app.viewmodel.signupviewmodel.apiresponse.SignupViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import androidx.core.view.isVisible


@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var binding: FragmentSignUpBinding?=null
    private lateinit var sessionManagement: SessionManagement
    private lateinit var googleSignInClient: GoogleSignInClient
    private var personName: String? = ""
    private var personGivenName: String? = ""
    private var personFamilyName: String? = ""
    private var personEmail: String? = ""
    private var personId: String = ""
    private lateinit var viewModel: SignupViewModel
    private var token: String = ""
    private enum class SignupType {
        EMAIL, PHONE
    }
    private var countryCode:String = ""
    private var signupType = SignupType.EMAIL   // default



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)
        sessionManagement= SessionManagement(requireContext())
        viewModel = ViewModelProvider(this)[SignupViewModel::class.java]
        countryCode = binding!!.ccp.selectedCountryCodeWithPlus
        logOutGmail()
        // In side this only call the event listener
        callEvent()
        // In side this only call the event back button
        callBackEvent()

        return binding!!.root
    }

    private fun callEvent() {

        selectEmail()

        binding?.tvEmail?.setOnClickListener {
            selectEmail()
        }

        binding?.tvPhone?.setOnClickListener {
            selectPhone()
        }
        /*binding!!.ccp.setOnCountryChangeListener {
            countryCode = binding!!.ccp.selectedCountryCodeWithPlus
//            val iso = binding!!.ccp.selectedCountryNameCode
        }*/

        binding!!.tvSignUpButton.setOnClickListener {

            if (!BaseApplication.isOnline(requireContext())) {
                BaseApplication.alertError(context, MessageClass.networkError, false)
                return@setOnClickListener
            }

            if (!isValidate()) return@setOnClickListener

        //    binding!!.tvSignUpButton.isEnabled = false

            when (signupType) {
                SignupType.EMAIL -> {
                    signupEmailApi()
                }

                SignupType.PHONE -> {
                    signupPhoneApi(countryCode)
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

        binding!!.tvSign.setOnClickListener {
            findNavController().navigate(R.id.signInFragment)
        }

        binding!!.eyehideIcon.setOnClickListener {
            binding!!.eyeIcon.visibility= View.VISIBLE
            binding!!.eyehideIcon.visibility= View.GONE
            binding!!.etSignUpPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding!!.etSignUpPassword.setSelection(binding!!.etSignUpPassword.text.length)
        }

        binding!!.eyeIcon.setOnClickListener {
            binding!!.etSignUpPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding!!.eyeIcon.visibility= View.GONE
            binding!!.eyehideIcon.visibility= View.VISIBLE
            binding!!.etSignUpPassword.setSelection(binding!!.etSignUpPassword.text.length)
        }
    }
    private fun selectEmail() {
        binding?.let {
            signupType = SignupType.EMAIL
            // Tab UI
            it.tvEmail.setBackgroundResource(R.drawable.bg_segment_selected)
            it.tvEmail.setTextColor(Color.WHITE)

            it.tvPhone.background = null
            it.tvPhone.setTextColor(Color.BLACK)

            // Input switch
            it.layoutEmail.visibility = View.VISIBLE
            it.layoutPhone.visibility = View.GONE

            // Hint + icon
            it.etSignUpEmail.hint = "Email"
            it.imageEmail.setImageResource(R.drawable.group_5)

            // Focus
            it.etSignUpEmail.requestFocus()
        }
    }
    private fun selectPhone() {
        signupType = SignupType.PHONE
        binding?.let {
            it.tvPhone.setBackgroundResource(R.drawable.bg_segment_selected)
            it.tvPhone.setTextColor(Color.WHITE)

            it.tvEmail.background = null
            it.tvEmail.setTextColor(Color.BLACK)

            // Input switch
            it.layoutEmail.visibility = View.GONE
            it.layoutPhone.visibility = View.VISIBLE

            // Hint
            it.etSignUpPhone.hint = "Phone Number"

            // Focus
            it.etSignUpPhone.requestFocus()
        }
    }


    private fun signupEmailApi() {
        BaseApplication.openDialog()
        lifecycleScope.launch {
            viewModel.signupRequest({ response ->
                BaseApplication.dismissDialog()
                handleApiResponse(response, "signup")
            },binding!!.etSignUpFullName.text.toString(),binding!!.etSignUpEmail.text.toString(),binding!!.etSignUpPassword.text.toString())
        }
    }

    private fun signupPhoneApi(countryCode: String) {
        BaseApplication.openDialog()
        lifecycleScope.launch {
            viewModel.signupPhoneRequest({ response ->
                BaseApplication.dismissDialog()
                handleApiResponse(response, "signup")
            },binding!!.etSignUpFullName.text.toString(),countryCode,binding!!.etSignUpPhone.text.toString(),binding!!.etSignUpPassword.text.toString())
        }
    }

    private fun callBackEvent() {
        binding!!.backBtn.setOnClickListener {
            navigateToWelcomeScreen()
        }

        // This line is used for the system back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateToWelcomeScreen()
                }
            }
        )


    }

    private fun isValidate(): Boolean {

        val fullName = binding!!.etSignUpFullName.text.toString().trim()
        val password = binding!!.etSignUpPassword.text.toString().trim()

        val email = binding!!.etSignUpEmail.text.toString().trim()
        val phone = binding!!.etSignUpPhone.text.toString().trim()

        val emailPattern = Pattern.compile(MessageClass.emailRegulerExpression)
        val passwordPattern = Pattern.compile(MessageClass.passwordRegulerExpression)

        // Full name
        if (fullName.isEmpty()) {
            BaseApplication.alertError(context, MessageClass.nameError, false)
            return false
        }

        // ================= EMAIL SIGN UP =================
        if (binding!!.layoutEmail.isVisible) {

            if (email.isEmpty()) {
                BaseApplication.alertError(context, MessageClass.emailError, false)
                return false
            }

            if (!emailPattern.matcher(email).matches()) {
                BaseApplication.alertError(context, MessageClass.emailValidationError, false)
                return false
            }
        }

        // ================= PHONE SIGN UP =================
        else if (binding!!.layoutPhone.isVisible) {

            if (phone.isEmpty()) {
                BaseApplication.alertError(context, MessageClass.phoneError, false)
                return false
            }

            if (!validNumber()) {
                BaseApplication.alertError(context, MessageClass.phoneValidationError, false)
                return false
            }
        }

        // Password empty
        if (password.isEmpty()) {
            BaseApplication.alertError(context, MessageClass.passwordError, false)
            return false
        }

        // Password format
        if (!passwordPattern.matcher(password).matches()) {
            BaseApplication.alertError(context, MessageClass.passwordValidationError, false)
            return false
        }

        return true
    }


    private fun validNumber(): Boolean {
        val phone = binding!!.etSignUpPhone.text.toString().trim()

        // Length check
        if (phone.length != 10) {
            return false
        }

        // Digits only
        return phone.all { it.isDigit() }
    }

    // Helper method to navigate to WelcomeScreen
    private fun navigateToWelcomeScreen() {
        findNavController().navigate(R.id.signInFragment)
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
            val apiModel = Gson().fromJson(data, LoginRootModel::class.java)
            when (dataType) {
                "socialLogin" -> {
                    if (apiModel.code == 200 && apiModel.status) {
                        apiModel.data?.let { showDataUi(it) }?: run {
                            showAlert(MessageClass.apiError, false)
                        }
                    } else {
                        handleError(apiModel.code,apiModel.message)
                    }
                }
                "signup" -> {
                    if (apiModel.code == 200 && apiModel.status) {
                        Toast.makeText(requireContext(),"OTP :- "+apiModel.data?.otp,Toast.LENGTH_LONG).show()
                        val bundle = Bundle()
                        bundle.putString("screenType", "SignUp")
                        bundle.putString("email", binding!!.etSignUpEmail.text.toString()?:"")
                        bundle.putString("countryCode", countryCode)
                        bundle.putString("phone", binding!!.etSignUpPhone.text.toString()?:"")
                        bundle.putString("name", binding!!.etSignUpFullName.text.toString())
                        bundle.putString("pass", binding!!.etSignUpPassword.text.toString())
                        bundle.putString("signUpType", signupType.name)
                        findNavController().navigate(R.id.verificationCode,bundle)
                    } else {
                        handleError(apiModel.code,apiModel.message)
                    }
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showDataUi(apiModel: DataModel) {
        try {
            apiModel.let {
                sessionManagement.setUserToken(apiModel.jwt_token.toString())
                if (apiModel.profile_status.equals("No",true)){
//                    findNavController().navigate(R.id.profileFragment)
                    sessionManagement.setProfileScreen("signup")
                    val bundle = bundleOf("source" to "auth")
                    findNavController().navigate(R.id.homeProfileFragment2,bundle)
                }else{
                    sessionManagement.setLoginSession(true)
                    sessionManagement.setProfileScreen("login")
                    sessionManagement.setUserName(apiModel.name.toString())
                    sessionManagement.setUserEmail(apiModel.email.toString())
                    sessionManagement.setUserProfile(apiModel.profile_pic.toString())
                    val intent = Intent(context, MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        }catch (e:Exception){
            showAlert(e.message, false)
        }
    }

/*
    private fun showDataUi(apiModel: DataModel) {
        try {
            // Save API token
            sessionManagement.setUserToken(apiModel.jwt_token.orEmpty())

            if (apiModel.profile_status.equals("No", true)) {

                sessionManagement.setProfileScreen("signup")
                val bundle = bundleOf("source" to "auth")
                findNavController().navigate(R.id.homeProfileFragment2, bundle)

            } else {

                sessionManagement.setLoginSession(true)
                sessionManagement.setProfileScreen("login")
                sessionManagement.setUserName(apiModel.name.orEmpty())
                sessionManagement.setUserEmail(apiModel.email.orEmpty())
                sessionManagement.setUserProfile(apiModel.profile_pic.orEmpty())

                //  STEP 1: Firebase anonymous login
                FirebaseAuth.getInstance()
                    .signInAnonymously()
                    .addOnSuccessListener {

                        //  STEP 2: Save user in Firestore
                        saveUserToFirestore(apiModel)

                        //  STEP 3: Open MainActivity
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    .addOnFailureListener {
                        showAlert("Firebase login failed", false)
                    }
            }

        } catch (e: Exception) {
            showAlert(e.message ?: "Something went wrong", false)
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
    }
*/


    private fun handleError(code:Int,msg:String){
        if (code==MessageClass.deactivatedUser || code==MessageClass.deletedUser){
            showAlert(msg, true)
        }else{
            showAlert(msg, false)
        }
    }

    // This is common function for show the alert box
    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(context, message,status)
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