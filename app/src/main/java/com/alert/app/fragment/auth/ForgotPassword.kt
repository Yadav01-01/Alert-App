package com.alert.app.fragment.auth
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentForgotPasswordBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.MessageClass
import com.alert.app.viewmodel.forgotpassviewmodel.ForgotPasswordViewModel
import com.alert.app.viewmodel.loginviewmodel.apiresponse.LoginRootModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern


@AndroidEntryPoint
class ForgotPassword : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ForgotPasswordViewModel
    private var selectedType = "EMAIL"
    private var countryCode:String = ""



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]
        val signInType = arguments?.getString("signInType")
        countryCode = binding.ccp.selectedCountryCodeWithPlus

        Log.d("TAG", "onCreateView: $signInType")

        setupListeners(signInType)

        return binding.root
    }

    private fun setupListeners(selectedType: String?) {

        // default selection
        selectEmail()

        binding.tvEmail.setOnClickListener {
            selectEmail()
        }

        binding.tvPhone.setOnClickListener {
            selectPhone()
        }

        binding.imageBackForgot.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvForgotButton.setOnClickListener {
            if (BaseApplication.isOnline(requireContext())) {
                if (isInputValid()) {
                    forgotPasswordApi()
                }
            } else {
                BaseApplication.alertError(context, MessageClass.networkError, false)
            }
        }
    }

    private fun selectEmail() {
        selectedType = "EMAIL"

        binding.layoutEmail.visibility = View.VISIBLE
        binding.layoutPhone.visibility = View.GONE

        binding.tvEmail.setBackgroundResource(R.drawable.bg_segment_selected)
        binding.tvEmail.setTextColor(resources.getColor(android.R.color.white))

        binding.tvPhone.setBackgroundColor(resources.getColor(android.R.color.transparent))
        binding.tvPhone.setTextColor(resources.getColor(android.R.color.black))
    }

    private fun selectPhone() {
        selectedType = "PHONE"

        binding.layoutEmail.visibility = View.GONE
        binding.layoutPhone.visibility = View.VISIBLE

        binding.tvPhone.setBackgroundResource(R.drawable.bg_segment_selected)
        binding.tvPhone.setTextColor(resources.getColor(android.R.color.white))

        binding.tvEmail.setBackgroundColor(resources.getColor(android.R.color.transparent))
        binding.tvEmail.setTextColor(resources.getColor(android.R.color.black))
    }


    private fun forgotPasswordApi() {
        BaseApplication.openDialog()
        lifecycleScope.launch {
            if (selectedType == "EMAIL") {
                viewModel.forgotPasswordRequest(
                    { response ->
                        BaseApplication.dismissDialog()
                        handleApiResponse(response)
                    },
                    binding.etForgotEmail.text.toString().trim(),
                    null
                )
            } else {

                val finalPhone =
                    "${binding.ccp.selectedCountryCode}${binding.etPhone.text.toString().trim()}"

                viewModel.forgotPasswordRequest(
                    { response ->
                        BaseApplication.dismissDialog()
                        handleApiResponse(response)
                    },
                    null,
                    finalPhone
                )
            }
        }
    }



    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String) {
        try {
            Log.d("@@@ Api Response", "message: $data")
            val apiModel = Gson().fromJson(data, LoginRootModel::class.java)
            if (apiModel.code == 200 && apiModel.status) {
                Toast.makeText(requireContext(),"OTP :- "+apiModel.data?.otp,Toast.LENGTH_LONG).show()
                val bundle = Bundle()
                bundle.putString(
                    "emailOrPhone",
                    if (selectedType == "EMAIL")
                        binding.etForgotEmail.text.toString().trim()
                    else
                        binding.etPhone.text.toString().trim()
                )

                bundle.putString("screenType", "Forgot")
                findNavController().navigate(R.id.verificationCode, bundle)
            } else {
                showAlert(apiModel.message, false)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    // This is common function for show the alert box
    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(context, message,status)
    }

    private fun isInputValid(): Boolean {
        return if (selectedType == "EMAIL") {
            val email = binding.etForgotEmail.text.toString().trim()

            when {
                email.isEmpty() -> {
                    BaseApplication.alertError(context, "Please enter email", false)
                    false
                }
                !Pattern.compile(MessageClass.emailRegulerExpression)
                    .matcher(email).matches() -> {
                    BaseApplication.alertError(context, "Please enter valid email", false)
                    false
                }
                else -> true
            }
        } else {
            val phone = binding.etPhone.text.toString().trim()

            when {
                phone.isEmpty() -> {
                    BaseApplication.alertError(context, "Please enter phone number", false)
                    false
                }
                phone.length < 10 -> {
                    BaseApplication.alertError(context, "Please enter valid phone number", false)
                    false
                }
                else -> true
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
