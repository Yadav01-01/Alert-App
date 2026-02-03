package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.base.BaseApplication
import com.alert.app.base.SessionManagement
import com.alert.app.databinding.FragmentVerifyProfileBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.MessageClass
import com.alert.app.viewmodel.profileviewmodel.UserProfileViewModel
import com.alert.app.viewmodel.profileviewmodel.apiresponse.UserProfileModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.activity.OnBackPressedCallback

@AndroidEntryPoint
class VerifyProfileFragment : Fragment() {

    private lateinit var binding:FragmentVerifyProfileBinding

    private val startTimeInMillis: Long = 120000
    private var mTimeLeftInMillis = startTimeInMillis
    private var emailOrPhone: String? = ""
    private lateinit var sessionManagement: SessionManagement
    private lateinit var viewModel: UserProfileViewModel
    private var screenType: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {

        }




    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentVerifyProfileBinding.inflate(LayoutInflater.from(requireContext()))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())

        if (arguments != null) {
            emailOrPhone = requireArguments().getString("emailOrPhone")
            screenType =
                if (requireArguments().containsKey("screenType")) requireArguments().getString("screenType") else ""
        }

        if (!sessionManagement.getProfileScreen().toString().equals("signup", true)) {
            binding.shadow.root.visibility = View.VISIBLE
            binding.imgBack.visibility = View.GONE
        } else {
            binding.shadow.root.visibility = View.GONE
            binding.imgBack.visibility = View.VISIBLE
        }


        /*if (screenType.equals("HomeProfile")){
           binding.shadow.root.visibility=View.VISIBLE
           binding.imgBack.visibility=View.GONE
        }else{
            binding.shadow.root.visibility=View.GONE
            binding.imgBack.visibility=View.VISIBLE
        }*/

        binding.tvVerificationButton.setOnClickListener {
            if (BaseApplication.isOnline(requireContext())) {
                if (binding.otpVerificationBox.otp!!.isEmpty()) {
                    BaseApplication.alertError(requireContext(), MessageClass.emptyOtp, false)
                } else if (binding.otpVerificationBox.otp!!.length != 4) {
                    BaseApplication.alertError(context, MessageClass.correctOtp, false)
                } else {
                    verificationApi()
                }
            } else {
                BaseApplication.alertError(requireContext(), MessageClass.networkError, false)
            }
        }




        binding.tvResendVerification.setOnClickListener {
            reSendOtp()
        }


        binding.imgBack.setOnClickListener {
            findNavController().navigate(R.id.homeProfileFragment2)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.homeProfileFragment2)
                }
            }
        )

    }

    private fun verificationApi() {
        val input = emailOrPhone.toString().trim()

        val isEmail = android.util.Patterns.EMAIL_ADDRESS
            .matcher(input)
            .matches()

        val email: String? = if (isEmail) input else null
        val phone: String? = if (!isEmail) input else null

        BaseApplication.openDialog()
        Log.d("TESTING_VERIFICATION","I am here outside Verification" + email +" "+phone)

        if (screenType.equals("HomeProfile", true)) {
            lifecycleScope.launch {
                BaseApplication.openDialog()
                viewModel.verifyProfileUpdateOtp(binding.otpVerificationBox.otp.toString(),
                    email, phone).collect { result ->

                    when (result) {
                        is NetworkResult.Success -> {
                            Log.d("TESTING_VERIFICATION","I am here inside Verification" + email +" "+phone)
                            BaseApplication.dismissDialog()
                            findNavController().popBackStack(R.id.homeProfileFragment, false)

                        }
                        is NetworkResult.Error -> {
                            BaseApplication.dismissDialog()
                            BaseApplication.alertError(requireContext(),result.message.toString(),false)
                            // show error
                        }
                        else ->{

                        }
                    }
                }

            }

        } else {
            lifecycleScope.launch {
                viewModel.forGotOtpVerifyRequest(
                    { response ->
                        BaseApplication.dismissDialog()
                        handleApiResponseVerifyOtp(response)
                    },
                    email,
                    binding.otpVerificationBox.otp.toString(),
                    phone
                )
            }
        }

    }


    private fun reSendOtp() {

        val input = emailOrPhone.toString().trim()

        val isEmail = android.util.Patterns.EMAIL_ADDRESS
            .matcher(input)
            .matches()

        val email: String? = if (isEmail) input else null

        val phone: String? = if (!isEmail) input else null

        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()

            Log.d("TESTING_VERIFICATION","I am here outside" + emailOrPhone)
            if(screenType.equals("HomeProfile",true)){
                Log.d("TESTING_VERIFICATION","I am here Indise" + emailOrPhone)

                lifecycleScope.launch {
                    viewModel.sendProfileUpdateVerifyOtp(emailOrPhone?:"").collect { result->
                        when(result){
                            is NetworkResult.Success ->{
                                BaseApplication.dismissDialog()
                            }
                            is NetworkResult.Error ->{
                                BaseApplication.dismissDialog()
                                BaseApplication.alertError(requireContext(),result.message.toString()?:"",false)
                            }
                            else ->{
                                BaseApplication.dismissDialog()
                            }
                        }
                    }

                }
            }
            else {

            }

            lifecycleScope.launch {
                viewModel.resendOtp({ response ->
                    BaseApplication.dismissDialog()
                    handleApiResponse(response, "reSend")
                }, email = email, phone = phone, type = "profile_verify")
            }
        } else {
            BaseApplication.alertError(context, MessageClass.networkError, false)
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>, dataType: String) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString(), dataType)
            is NetworkResult.Error -> showAlert(result.message.toString(), false)
        }
    }

    private fun handleApiResponseVerifyOtp(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponseVerifyOtp(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message.toString(), false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String, dataType: String) {
        try {
            Log.d("@@@ Api Response", "message: $data")
            when (dataType) {
                "reSend" -> {
                    val apiModel = Gson().fromJson(data, UserProfileModel::class.java)
                    if (apiModel.code == 200 && apiModel.status) {
                        startTime()
                    } else {
                        handleError(apiModel.code, apiModel.message)
                    }
                }

                "verify" -> {
                    val apiModel = Gson().fromJson(data, UserProfileModel::class.java)
                    if (apiModel.code == 200 && apiModel.status) {
                        if (!sessionManagement.getProfileScreen().toString()
                                .equals("signup", true)
                        ) {
                            findNavController().navigate(R.id.homeProfileFragment2)
                        } else {
                            openAlertBoxSuccess()
                        }
                    } else {
                        handleError(apiModel.code, apiModel.message)
                    }
                }
            }
        } catch (e: Exception) {
            showAlert(e.message.toString(), false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponseVerifyOtp(data: String) {
        try {
            val apiModel = Gson().fromJson(data, UserProfileModel::class.java)
            if (apiModel.code == 200 && apiModel.status) {
                findNavController().popBackStack(R.id.homeProfileFragment, false)

            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message.toString(), false)
        }
    }

    private fun handleError(code: Int, msg: String) {
        if (code == MessageClass.deactivatedUser || code == MessageClass.deletedUser) {
            showAlert(msg, true)
        } else {
            showAlert(msg, false)
        }
    }


    private fun showAlert(message: String, status: Boolean) {
        BaseApplication.alertError(context, message, status)
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

        if (emailOrPhone!!.contains("@")) {
            tvText.text = "Your Email is changed Successfully."
        } else {
            tvText.text = "Your Phone is changed Successfully."
        }

        imgClose.visibility = View.VISIBLE

        tvPwdChangeOK.setOnClickListener {
            dialog.dismiss()
            findNavController().navigateUp()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
            findNavController().navigateUp()
        }


    }

    private fun startTime() {
        object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis = millisUntilFinished
                binding.tvResendVerification.setTextColor(Color.parseColor("#828282"))
                binding.tvResendVerification.isEnabled = false
                binding.llResendTimer.visibility = View.VISIBLE
                updateCountDownText()
            }

            override fun onFinish() {
                mTimeLeftInMillis = 120000
                binding.tvResendVerification.setTextColor(Color.parseColor("#1E60AC"))
                binding.llResendTimer.visibility = View.GONE
                binding.tvResendVerification.isEnabled = true
            }
        }.start()
    }

    @SuppressLint("SetTextI18n")
    private fun updateCountDownText() {
        val minutes = mTimeLeftInMillis.toInt() / 1000 / 60
        val seconds = mTimeLeftInMillis.toInt() / 1000 % 60
        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        binding.tvTimer.text = "$timeLeftFormatted sec"
    }

}