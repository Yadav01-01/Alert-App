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
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.alert.app.R
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentProfileBinding
import com.alert.app.errormessage.MessageClass
import java.util.regex.Pattern

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var isTermsAccepted = false
    private var phoneStatus = 0
    private var emailStatus = 0

    private val emailPattern = Pattern.compile(MessageClass.emailRegulerExpression)

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

        binding.imgCheckUncheckRemember.setOnClickListener {
            toggleTermsAcceptance()
        }

        binding.btnSave.setOnClickListener {
            if (isValidInput()) {
                findNavController().navigate(R.id.confirmationFragment)
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
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
