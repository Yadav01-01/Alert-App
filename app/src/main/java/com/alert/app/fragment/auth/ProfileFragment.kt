/*
package com.yesitlabs.alertapp.fragment.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.yesitlabs.alertapp.R
import com.yesitlabs.alertapp.base.BaseApplication
import com.yesitlabs.alertapp.databinding.FragmentLanguageBinding
import com.yesitlabs.alertapp.databinding.FragmentProfileBinding
import com.yesitlabs.alertapp.errormessage.MessageClass
import java.io.File
import java.util.regex.Pattern


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private var checkUncheck:Boolean=false

    private val emailpattern = MessageClass.emailRegulerExpression
    private val emapattern = Pattern.compile(emailpattern)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.imgCheckUncheckRemember.setOnClickListener{
            if (checkUncheck){
                checkUncheck=false
                binding.imgCheckUncheckRemember.setImageResource(R.drawable.uncheck_blue_tick_icon)
            }else{
                checkUncheck=true
                binding.imgCheckUncheckRemember.setImageResource(R.drawable.check_blue_tick_icon)
            }
        }



        binding.btnSave.setOnClickListener {
            if (isValidation()){
                val bundle = Bundle()
                bundle.putString("screenType", "Profile")
                findNavController().navigate(R.id.verificationCodeProfileFragment,bundle)
            }
        }

        binding.imgUpload.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }




    }

    private fun isValidation(): Boolean {

        val emailmatcher = emapattern.matcher(binding.edEmail.text.toString().trim())

        if (binding.edName.text.toString().trim().isEmpty()){
            BaseApplication.alertError(context, MessageClass.nameError,false)
            return false
        }else if (binding.edEmail.text.toString().trim().isEmpty()){
            BaseApplication.alertError(context, MessageClass.emailError,false)
            return false
        }else if (!emailmatcher.find()){
            BaseApplication.alertError(context, MessageClass.emailErrorValidation,false)
            return false
        }else if (binding.edPhone.text.toString().trim().isEmpty()){
            BaseApplication.alertError(context, MessageClass.phoneError,false)
            return false
        }else if (!checkUncheck){
            BaseApplication.alertError(context, MessageClass.AgreetoacceptError,false)
            return false
        }

        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImagePicker.REQUEST_CODE) {
            if (data?.data != null) {
                val uri = data.data!!
                Glide.with(requireContext())
                    .load(uri)
                    .placeholder(R.drawable.user_img_icon)
                    .error(R.drawable.user_img_icon)
                    .into(binding.userImg)

            }

        }
    }
}*/
package com.alert.app.fragment.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.alert.app.R
import com.alert.app.adapter.PlacesAutoCompleteAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentProfileBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.OnPlacesDetailsListener
import com.alert.app.model.addressmodel.Place
import com.alert.app.model.addressmodel.PlaceAPI
import com.alert.app.model.addressmodel.PlaceDetails
import com.alert.app.viewmodel.profileviewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.regex.Pattern

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var isTermsAccepted = false
    private var phoneStatus = 0
    private var emailStatus = 0
    private var countryCode :String =""
    private val emailPattern = Pattern.compile(MessageClass.emailRegulerExpression)
    private var file : File? = null
    private var latitude = ""
    private var longitude = ""
    private lateinit var viewModel: UserProfileViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        emailPhoneEvent()
        viewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        countryCode = binding.ccp.selectedCountryCodeWithPlus
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
        val phone = binding.edPhone.text.toString().trim()
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
    private fun handleApiResponse(result: NetworkResult<String>, dataType: String,value:String) {

        when (result) {
            is NetworkResult.Success -> {
                findNavController().navigate(R.id.confirmationFragment)
            }
            is NetworkResult.Error -> {
                showAlert(result.message.toString())
               }
        }
    }


    private fun emailPhoneEvent() {

        binding.edEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                try {
                    if (editable.toString().trim().isEmpty()) {
                        emailStatus=0
                        emailAndPhoneStatus()
                    } else {
                        emailStatus = if (editable.toString().trim().equals(editable.toString().trim(), true)) {
                            0
                        } else {
                            1
                        }
                        emailAndPhoneStatus()
                    }
                } catch (e: Exception) {
                    emailStatus = 0
                    emailAndPhoneStatus()
                }
            }
        })
        binding.edPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                try {
                    if (editable.toString().trim().isEmpty()) {
                        phoneStatus=0
                        emailAndPhoneStatus()
                    } else {
                        phoneStatus = if (editable.toString().trim().replace(Regex("[^0-9]"), "").equals(
                                binding.edPhone.text.toString().trim()
                                    .replace(Regex("[^0-9]"), ""), true
                            )
                        ) {
                            0
                        } else {
                            1
                        }
                        emailAndPhoneStatus()
                    }

                } catch (e: Exception) {
                    phoneStatus = 0
                    emailAndPhoneStatus()
                }
            }
        })

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

    private fun emailAndPhoneStatus() {
        val emailText = binding.edEmail.text.toString().trim()
        val phoneText = binding.edPhone.text.toString().trim()
        val emailMatcher = emailPattern.matcher(emailText)
        val isEmailValid = emailMatcher.find()
        val isPhoneValid = phoneText.replace(Regex("[^0-9]"), "").length == 10

        // Email Status
        if (emailStatus == 1) {
            setEmailStatus(true, MessageClass.verifyStatus, R.drawable.ic_green_tick, "#219653")
        } else {
            if (emailText.isEmpty()) {
                setEmailVisibility(false)
            } else {
                setEmailStatus(true, if (isEmailValid) MessageClass.verifyNowStatus else MessageClass.emailVaildStatus, R.drawable.ic_cancel_red_icon, "#CE2127")
            }
        }

        // Phone Status
        if (phoneStatus == 1) {
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
        binding.rlphoneverified.visibility = if (visible) View.VISIBLE else View.GONE
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

    private fun setupListeners() {

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.signInFragment)
            }
        })


        val placesApi = PlaceAPI.Builder()
            .apiKey(getString(R.string.api_keysearch))
            .build(requireContext())

        binding.edAddress.setAdapter(PlacesAutoCompleteAdapter(requireContext(), placesApi))
        binding.edAddress.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val place = parent.getItemAtPosition(position) as Place
            binding.edAddress.setText(place.description)
            getPlaceDetails(place.id, placesApi)
        }


        binding.imgCheckUncheckRemember.setOnClickListener {
            toggleTermsAcceptance()
        }

        binding.btnSave.setOnClickListener {
            if (isValidInput()) {
               upDateProfile()
            }
        }

        binding.tvemailstatus.setOnClickListener {
            navigateToVerification()
        }
        binding.tvphonestatus.setOnClickListener {
            navigateToVerification()
        }

        binding.imgUpload.setOnClickListener {
            openImagePicker()
        }

        binding.privacyPolicy.setOnClickListener {
          findNavController().navigate(R.id.privacyPolicyFragment2)
        }

        binding.termAndCondition.setOnClickListener {
            findNavController().navigate(R.id.termsAndConditionFragment2)
        }



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

    private fun openImagePicker() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }

    private fun navigateToVerification() {
        val bundle = Bundle().apply {
            putString("screenType", "Profile")
        }
        findNavController().navigate(R.id.verificationCodeProfileFragment, bundle)
    }

    private fun isValidInput(): Boolean {
        val emailMatcher = emailPattern.matcher(binding.edEmail.text.toString().trim())

        when {
            binding.edName.text.toString().trim().isEmpty() -> {
                showAlert(MessageClass.nameError)
            }
            binding.edEmail.text.toString().trim().isEmpty() -> {
                showAlert(MessageClass.emailError)
            }
            !emailMatcher.find() -> {
                showAlert(MessageClass.emailErrorValidation)
            }

            binding.edPhone.text.toString().trim().isEmpty() -> {
                showAlert(MessageClass.phoneError)
            }
            countryCode.isNullOrEmpty() ->{
                showAlert(MessageClass.countryCodeSelectionError)
            }

            !isTermsAccepted -> {
                showAlert(MessageClass.AgreetoacceptError)
            }
            else -> return true
        }
        return false
    }

    private fun showAlert(message: String) {
        BaseApplication.alertError(context, message, false)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
