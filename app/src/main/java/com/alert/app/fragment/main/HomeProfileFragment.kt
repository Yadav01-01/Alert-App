package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.BuildConfig
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.PlacesAutoCompleteAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.base.SessionManagement
import com.alert.app.databinding.FragmentHomeProfileBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.OnPlacesDetailsListener
import com.alert.app.model.addressmodel.Place
import com.alert.app.model.addressmodel.PlaceAPI
import com.alert.app.model.addressmodel.PlaceDetails
import com.alert.app.viewmodel.profileviewmodel.UserProfileViewModel
import com.alert.app.viewmodel.profileviewmodel.apiresponse.Data
import com.alert.app.viewmodel.profileviewmodel.apiresponse.UserProfileModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.regex.Pattern



@AndroidEntryPoint
class HomeProfileFragment : Fragment() {

    private var _binding: FragmentHomeProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManagement: SessionManagement
    private val emailPattern = Pattern.compile(MessageClass.emailRegulerExpression)
    private var isEmailVerifiedFromApi = false
    private var isPhoneVerifiedFromApi = false

    private var latitude = ""
    private var longitude = ""
    private var isTermsAccepted = false
    private lateinit var viewModel: UserProfileViewModel
    private var file: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View { _binding = FragmentHomeProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val source = arguments?.getString("source")
        if (source != null) {
            Log.d("checkSource",source)
        }
        if (source == "auth") {

            binding.layPrivacy.visibility = View.VISIBLE
        } else {
            binding.layPrivacy.visibility = View.GONE
        }
        sessionManagement = SessionManagement(requireContext())
        viewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]

        if (!sessionManagement.getProfileScreen().toString().equals("signup",true)){
            val mainActivity = requireActivity() as MainActivity
            setupUI(mainActivity)
            checkEditable(sessionManagement.getUserEditable() ?: false)
            binding.backBtn.visibility=View.GONE
            binding.layPrivacy.visibility=View.GONE
            isTermsAccepted=true
        }else{
            binding.backBtn.visibility=View.VISIBLE
            binding.layPrivacy.visibility=View.VISIBLE
            isTermsAccepted=false
            checkEditable(true)
        }


        loadApi()

        setupBackButtonHandler()

        setupClickListeners()

        emailPhoneEvent()

    }


    private fun loadApi() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getProfileRequest { response ->
                    BaseApplication.dismissDialog()
                    handleApiResponse(response, "userProfile", "")
                }
            }
        }else{
            showAlert(MessageClass.networkError,false)
        }
    }


    private fun handleApiResponse(result: NetworkResult<String>, dataType: String,value:String) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString(),dataType,value)
            is NetworkResult.Error -> showAlert(result.message.toString(), false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String, dataType: String,value:String) {
        try {
            Log.d("@@@ Api Response", "message: $data")
            when (dataType) {
                "userProfile" -> {
                    val apiModel = Gson().fromJson(data, UserProfileModel::class.java)
                    if (apiModel.code == 200 && apiModel.status) {
                        apiModel.data?.let { showDataUi(it) }?: run {
                            showAlert(MessageClass.apiError, false)
                        }
                    } else {
                        handleError(apiModel.code,apiModel.message)
                    }
                }
                "verify" -> {
                    val apiModel = Gson().fromJson(data, UserProfileModel::class.java)
                    if (apiModel.code == 200 && apiModel.status) {
                        navigateToVerificationCodeProfile(value)
                    } else {
                        handleError(apiModel.code,apiModel.message)
                    }
                }
                "profileUpdate" -> {
                    val apiModel = Gson().fromJson(data, UserProfileModel::class.java)
                    if (apiModel.code == 200 && apiModel.status) {
                        if (sessionManagement.getProfileScreen().toString().equals("signup",true)){
                            apiModel.data?.name.let { sessionManagement.setUserName(it.toString()) }
                            apiModel.data?.email.let { sessionManagement.setUserEmail(it.toString()) }
                            apiModel.data?.profile_pic.let { sessionManagement.setUserProfile(it.toString()) }
                            navigateToConfirmationFragment(value)
                           /* sessionManagement.setLoginSession(true)
                            sessionManagement.setProfileScreen("login")
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()*/
                        }else{
                            loadApi()
                        }
                    } else {
                        handleError(apiModel.code,apiModel.message)
                    }
                }
            }
        } catch (e: Exception) {
            showAlert(e.message.toString(), false)
        }
    }

    private fun showDataUi(data: Data) {
        try {

            /* -------- BASIC DATA -------- */
            data.name?.let {
                binding.edName.setText(it)
                sessionManagement.setUserName(it)
            }

            data.email?.let {
                binding.edEmail.setText(it)
                binding.edPhone.isEnabled = false
                sessionManagement.setUserEmail(it)
            }

            data.phone_number?.let {
                binding.edPhone.setText(it)
                binding.edPhone.isEnabled = false
                sessionManagement.setUserPhoneNumber(it)
            }

            /* -------- API VERIFICATION FLAGS (SOURCE OF TRUTH) -------- */
            isEmailVerifiedFromApi = data.email_verified_status == true
            isPhoneVerifiedFromApi = data.phone_verified_status == true


            /* --- UPDATE UI BASED ON API --- */
            updateVerificationUI()

            /* -------- ADDRESS -------- */
            data.address?.let {
                binding.edAddress.setText(it)
            }

            latitude = data.lat?.toString().orEmpty()
            longitude = data.longi?.toString().orEmpty()

            /* -------- PROFILE IMAGE -------- */
            data.profile_pic?.let {
                sessionManagement.setUserProfile(it)
                Glide.with(requireContext())
                    .load(BuildConfig.BASE_URL + it)
                    .placeholder(R.drawable.user_img_icon)
                    .error(R.drawable.user_img_icon)
                    .into(binding.userImg)
            }

        } catch (e: Exception) {
            showAlert(e.message.toString(), false)
        }
    }

    private fun updateVerificationUI() {

        val emailText = binding.edEmail.text.toString().trim()
        val phoneText = binding.edPhone.text.toString().trim()

        val isEmailValid = emailPattern.matcher(emailText).find()
        val isPhoneValid = phoneText.replace(Regex("[^0-9]"), "").length == 10

        /* ---------------- EMAIL ---------------- */
        when {
            emailText.isEmpty() -> setEmailVisibility(false)

            isEmailVerifiedFromApi -> {
                setEmailStatus(true, MessageClass.verifyStatus,
                    R.drawable.ic_green_tick, "#219653")
            }

            isEmailValid -> {
                setEmailStatus(true, MessageClass.verifyNowStatus,
                    R.drawable.ic_cancel_red_icon, "#CE2127")
            }

            else -> {
                setEmailStatus(true, MessageClass.emailVaildStatus,
                    R.drawable.ic_cancel_red_icon, "#CE2127")
            }
        }

        /* ------ PHONE ------ */

        when {
            phoneText.isEmpty() -> setPhoneVisibility(false)

            isPhoneVerifiedFromApi -> {

                setPhoneStatus(true, MessageClass.verifyStatus,
                    R.drawable.ic_green_tick, "#219653")

            }

            isPhoneValid -> {
                setPhoneStatus(true, MessageClass.verifyNowStatus,
                    R.drawable.ic_cancel_red_icon, "#CE2127")
            }

            else -> {

                setPhoneStatus(true, MessageClass.phoneVaildStatus,
                    R.drawable.ic_cancel_red_icon, "#CE2127")

            }
        }
    }


    private fun handleError(code:Int,msg:String){
        if (code==MessageClass.deactivatedUser || code==MessageClass.deletedUser){
            showAlert(msg, true)
        }else{
            showAlert(msg, false)
        }
    }

    private fun emailPhoneEvent() {

        binding.edEmail.doAfterTextChanged {
            updateVerificationUI()
        }



        binding.edPhone.doAfterTextChanged {
             setPhoneVisibility(true)
        //   updateVerificationUI()
        }
    }


    private fun emailAndPhoneStatus() {

        val emailText = binding.edEmail.text.toString().trim()
        val phoneText = binding.edPhone.text.toString().trim()
        val emailMatcher = emailPattern.matcher(emailText)
        val isEmailValid = emailMatcher.find()
        val isPhoneValid = phoneText.replace(Regex("[^0-9]"), "").length == 10

        if (isEmailValid) {
            setEmailStatus(true, MessageClass.verifyStatus, R.drawable.ic_green_tick, "#219653")
        } else {
            if (emailText.isEmpty()) {
                setEmailVisibility(false)
            } else {
                setEmailStatus(true, if (isEmailValid) MessageClass.verifyNowStatus else MessageClass.emailVaildStatus, R.drawable.ic_cancel_red_icon, "#CE2127")
            }
        }


        if (isPhoneValid ) {
            setPhoneStatus(true, MessageClass.verifyStatus, R.drawable.ic_green_tick, "#219653")
        } else {
            if (phoneText.isEmpty()) {
                setPhoneVisibility(false)
            } else {
                setPhoneStatus(true, if (isPhoneValid) MessageClass.verifyNowStatus else MessageClass.phoneVaildStatus, R.drawable.ic_cancel_red_icon, "#CE2127")
            }
        }
    }

    private fun setEmailStatus(visible: Boolean, statusText: String, iconRes: Int, color: String) {

        binding.rlemailverified.visibility = if (visible) View.VISIBLE else View.GONE

        binding.emailStatus.visibility = if (visible) View.VISIBLE else View.GONE

        if (visible) {
            binding.tvemailstatus.text = Html.fromHtml(statusText, Html.FROM_HTML_MODE_LEGACY)
            binding.emailStatus.setImageResource(iconRes)
            binding.tvemailstatus.setTextColor(Color.parseColor(color))
        }

    }

    private fun setPhoneStatus(visible: Boolean, statusText: String, iconRes: Int, color: String) {
        binding.rlemailverified.visibility = if (visible) View.VISIBLE else View.GONE

        binding.phoneStatus.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            binding.tvphonestatus.text = Html.fromHtml(statusText, Html.FROM_HTML_MODE_LEGACY)
            binding.phoneStatus.setImageResource(iconRes)
            binding.tvphonestatus.setTextColor(Color.parseColor(color))
        }
    }

    private fun setEmailVisibility(visible: Boolean) {
        binding.rlemailverified.visibility = if (visible) View.VISIBLE else View.GONE
        binding.emailStatus.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun setPhoneVisibility(visible: Boolean) {
        binding.rlphoneverified.visibility = if (visible) View.VISIBLE else View.GONE
        binding.phoneStatus.visibility = if (visible) View.VISIBLE else View.GONE
    }



    private fun setupUI(mainActivity: MainActivity) {
        mainActivity.apply {
            setFooter("userProfile")
            setImageShowTv()?.visibility = View.GONE
            setImgChatBoot().visibility = View.GONE
        }
    }

    private fun setupBackButtonHandler() {

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backLogic()
                }
            }
        )
    }


    private fun backLogic(){
        if (!sessionManagement.getProfileScreen().toString().equals("signup",true)){
            requireActivity().finish()
        }else{
            findNavController().navigate(R.id.signInFragment)
        }
    }

    private fun setupClickListeners() {

        binding.imgUpload.setOnClickListener { openImagePicker() }

        binding.btnSave.setOnClickListener {
            if (BaseApplication.isOnline(requireContext())){
                handleSaveButtonClick()
            }else{
                BaseApplication.alertError(context, MessageClass.networkError,false)
            }
        }

        binding.backBtn.setOnClickListener { backLogic() }

        binding.tvemailstatus.setOnClickListener {
            if (binding.tvemailstatus.text.toString().trim().equals(Html.fromHtml(MessageClass.verifyNowStatus).toString().trim(), true)) {
                if (isValidateEmail()) {
                    verifySendOtp(binding.edEmail.text.toString())
                }
            }
        }

        binding.tvphonestatus.setOnClickListener {
            if (binding.tvphonestatus.text.toString().trim().equals(Html.fromHtml(MessageClass.verifyNowStatus).toString().trim(), true)) {
                if (isValidatePhone()) {
                    Log.d("TESTING_CURRENT",binding.ccp.defaultCountryCodeWithPlus)
                    verifySendOtp(binding.ccp.defaultCountryCodeWithPlus+binding.edPhone.text.toString())
                }
            }
        }


        binding.layPrivacy.setOnClickListener {
            toggleTermsAcceptance()
        }

        binding.privacyPolicy.setOnClickListener {
            findNavController().navigate(R.id.privacyPolicyFragment2)
        }

        binding.termAndCondition.setOnClickListener {
            findNavController().navigate(R.id.termsAndConditionFragment2)
        }

        val placesApi = PlaceAPI.Builder()
            .apiKey(getString(R.string.api_keysearch))
            .build(requireContext())

        binding.edAddress.setAdapter(PlacesAutoCompleteAdapter(requireContext(), placesApi))
        binding.edAddress.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val place = parent.getItemAtPosition(position) as Place
            binding.edAddress.setText(place.description)
            getPlaceDetails(place.id, placesApi)
        }

    }

    private fun verifySendOtp(value:String){
        if (BaseApplication.isOnline(requireContext())){
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.sendOtpEmailPhoneRequest({ response ->
                    BaseApplication.dismissDialog()
                    handleApiResponse(response, "verify",value)
                }, value,)
            }
        }else{
            BaseApplication.alertError(context, MessageClass.networkError,false)
        }
    }


    private fun isValidateEmail(): Boolean {
        val emailMatcher = emailPattern.matcher(binding.edEmail.text.toString().trim())
        if (binding.edEmail.text.toString().trim().isEmpty()) {
            showAlert(MessageClass.emailError,false)
            return false
        }else if (!emailMatcher.find() ) {
            showAlert(MessageClass.emailErrorValidation,false)
            return false
        }
        return true
    }


    private fun isValidatePhone(): Boolean {
        if (binding.edPhone.text.toString().trim().isEmpty()) {
            showAlert(MessageClass.phoneError,false)
            return false
        }
        return true
    }


    private fun toggleTermsAcceptance() {
        isTermsAccepted = !isTermsAccepted
        val iconRes = if (isTermsAccepted) {
            R.drawable.check_blue_tick_icon
        } else {
            R.drawable.uncheck_blue_tick_icon
        }
        binding.imgCheckUncheckRemember.setImageResource(iconRes)
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

    private fun openImagePicker() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }

    private fun handleSaveButtonClick() {
        if (binding.btnSave.text.equals("Edit")) {
            checkEditable(true)
        } else if (isValidation()) {
            upDateProfile()
//            sessionManagement.setUserEditable(false)
//            checkEditable(false)
        }
    }

    private fun upDateProfile() {
        BaseApplication.openDialog()
        var requestImage: MultipartBody.Part? = null

        file?.let {
            val requestBody = it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            requestImage = MultipartBody.Part.createFormData("user_pic", it.name, requestBody)
        }
        val name = binding.edName.text.toString().trim()
        val email = binding.edEmail.text.toString().trim()
        val phone = "+"+binding.ccp.selectedCountryCodeWithPlus+binding.edPhone.text.toString().trim()
        val address = binding.edAddress.text.toString()
        // Convert to RequestBody
        fun createPart(value: String) = value.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val cusName = createPart(name)
        val cusEmail = createPart(email)
        val cusPhone = createPart(phone)
        val cusAddress = createPart(address)
        val cusLatitude = createPart(latitude)
        val cusLongitude = createPart(longitude)
        lifecycleScope.launch {
            viewModel.profileUpdateRequest({ response ->
                BaseApplication.dismissDialog()
                handleApiResponse(response, "profileUpdate","")
            }, cusName,cusEmail,cusPhone,cusAddress,cusLatitude,cusLongitude,requestImage)
        }
    }

    private fun navigateToVerificationCodeProfile(value:String) {
        val bundle=Bundle()
        bundle.putString("screenType", "HomeProfile")
        bundle.putString("emailOrPhone", value)
        findNavController().navigate(R.id.verificationCodeProfileFragment,bundle)
    }

    private fun navigateToConfirmationFragment(value:String) {
        val bundle=Bundle()
        //findNavController().navigate(R.id.confirmationFragment,bundle)
        findNavController().navigate(R.id.knowMoreFragment2,bundle)
    }

    @SuppressLint("SetTextI18n")
    private fun checkEditable(isEditable: Boolean) {
        binding.apply {
            edName.isEnabled = isEditable
            edEmail.isEnabled = isEditable
            edPhone.isEnabled = isEditable
            edAddress.isEnabled = isEditable
            if (!sessionManagement.getProfileScreen().toString().equals("signup",true)){
                btnSave.text = if (isEditable) "Save" else "Edit"
            }else{
                btnSave.text = "Save"
            }
            imgUpload.visibility = if (isEditable) View.VISIBLE else View.GONE
        }
    }

    private fun isValidation(): Boolean {

        val name = binding.edName.text.toString().trim()
        val email = binding.edEmail.text.toString().trim()
        val phone = binding.edPhone.text.toString().trim()
        val address = binding.edAddress.text.toString().trim()

        val isEmailValid = emailPattern.matcher(email).matches()

        if(binding.ccp.selectedCountryCode.isNullOrEmpty()){
            showAlert("Please Select country code",false)
            return false
        }


        return when {
            name.isEmpty() -> {
                showAlert(MessageClass.nameError, false)
                false
            }

            email.isEmpty() -> {
                showAlert(MessageClass.emailError, false)
                false
            }

            !isEmailValid -> {
                showAlert(MessageClass.emailErrorValidation, false)
                false
            }

            !isEmailVerifiedFromApi -> {
                showAlert(MessageClass.emailVerifyStatus, false)
                false
            }

            phone.isEmpty() -> {
                showAlert(MessageClass.phoneError, false)
                false
            }

            !isPhoneVerifiedFromApi -> {
                showAlert(MessageClass.phoneVerifyStatus, false)
                false
            }

            address.isEmpty() -> {
                showAlert(MessageClass.addressError, false)
                false
            }



            file == null -> {
                showAlert(MessageClass.profilePic, false)
                false
            }

            !isTermsAccepted -> {
                showAlert(MessageClass.AgreetoacceptError, false)
                false
            }

            else -> true
        }
    }


    private fun showAlert(message: String, status: Boolean) {
        BaseApplication.alertError(context, message, status)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImagePicker.REQUEST_CODE && data?.data != null) {
            val uri = data.data!!
            Glide.with(requireContext())
                .load(uri)
                .placeholder(R.drawable.user_img_icon)
                .error(R.drawable.user_img_icon)
                .into(binding.userImg)
            val path = BaseApplication.getPath(requireContext(), uri)
            checkNotNull(path) { MessageClass.PATH_ERROR}
            file = File(path)
        }
    }
}
