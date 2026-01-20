package com.alert.app.fragment.auth

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentResetpasswordBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.MessageClass
import com.alert.app.viewmodel.forgotpassviewmodel.ForgotPasswordViewModel
import com.alert.app.viewmodel.loginviewmodel.apiresponse.LoginRootModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern


@AndroidEntryPoint
class ResetPassword : Fragment() {

    private var binding: FragmentResetpasswordBinding?=null
    private var userId:String?=""
    private var otp: String? = ""
    private var email: String? = ""
    private var phone: String? = ""
    private var signUpType: String? = ""
    private lateinit var viewModel: ForgotPasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            otp = it.getString("otp")?: ""
            email = it.getString("email")?: ""
            phone = it.getString("phone")?: " "
            signUpType = it.getString("signUpType")?: ""
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentResetpasswordBinding.inflate(layoutInflater, container, false)
        userId= requireArguments().getString("user_id")
        viewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]


        binding!!.tvResetButton.setOnClickListener{
            if (BaseApplication.isOnline(requireContext())){
                if (isValidate()){
                    resetPassword()
                }
            }else{
                BaseApplication.alertError(context, MessageClass.networkError,false)
            }
        }

        binding!!.imageBackReset.setOnClickListener {
            findNavController().navigateUp()
        }

        binding!!.eyehideIcon.setOnClickListener {
            binding!!.eyeIcon.visibility= View.VISIBLE
            binding!!.eyehideIcon.visibility= View.GONE
            binding!!.etCreateNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding!!.etCreateNewPassword.setSelection(binding!!.etCreateNewPassword.text.length)
        }

        // This event is use for show the edit text value
        binding!!.eyeIcon.setOnClickListener {
            binding!!.etCreateNewPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding!!.eyeIcon.visibility= View.GONE
            binding!!.eyehideIcon.visibility= View.VISIBLE
            binding!!.etCreateNewPassword.setSelection(binding!!.etCreateNewPassword.text.length)
        }

        binding!!.eyecnfhideIcon.setOnClickListener {
            binding!!.eyecnfIcon.visibility= View.VISIBLE
            binding!!.eyecnfhideIcon.visibility= View.GONE
            binding!!.etConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding!!.etConfirmPassword.setSelection(binding!!.etConfirmPassword.text.length)
        }

        // This event is use for show the edit text value
        binding!!.eyecnfIcon.setOnClickListener {
            binding!!.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding!!.eyecnfIcon.visibility= View.GONE
            binding!!.eyecnfhideIcon.visibility= View.VISIBLE
            binding!!.etConfirmPassword.setSelection(binding!!.etConfirmPassword.text.length)
        }



        return binding!!.root
    }

    private fun resetPassword() {
        Log.d("checkData",otp+" " +email)
        BaseApplication.openDialog()
        lifecycleScope.launch {
            if (signUpType == "EMAIL"){
                viewModel.reseatPasswordRequest({ response ->
                    BaseApplication.dismissDialog()
                    handleApiResponse(response)
                },email,null, binding!!.etCreateNewPassword.text.toString(),binding!!.etConfirmPassword.text.toString())
            }else{
                viewModel.reseatPasswordRequest({ response ->
                    BaseApplication.dismissDialog()
                    handleApiResponse(response)
                },null,phone, binding!!.etCreateNewPassword.text.toString(),binding!!.etConfirmPassword.text.toString())
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
            Log.d("checkCode",apiModel.code.toString())
            if (apiModel.status_code == 200 && apiModel.status) {
                openAlertBoxPassword()
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

    private fun openAlertBoxPassword() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_password_change)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

       // Get the current window attributes
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)

       // Set the desired width and height
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT // For full screen width, you can also use a specific size like 600
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT // You can set this to a fixed size, for example, 500

       // Apply the updated layout parameters
        dialog.window!!.attributes = layoutParams

       // Make sure to show the dialog and set the soft input mode
        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        val tvPwdChangeOK = dialog.findViewById<TextView>(R.id.tvPwdChangeOK)
        tvPwdChangeOK.setOnClickListener {
            findNavController().navigate(R.id.signInFragment)
            dialog.dismiss()

        }

    }

    private fun isValidate():Boolean{
        val pattern = Pattern.compile(MessageClass.passwordRegulerExpression)
        val passMatcher = pattern.matcher(binding!!.etConfirmPassword.text.toString().trim())
        if (binding!!.etCreateNewPassword.text.toString().isEmpty()) {
            BaseApplication.alertError(context, MessageClass.passwordError,false)
            return false
        } else if (!passMatcher.find()) {
            BaseApplication.alertError(context, MessageClass.passwordValidationError,false)
            return false
        }else if (binding!!.etConfirmPassword.text.toString().isEmpty()) {
            BaseApplication.alertError(context, MessageClass.cnfPasswordError,false)
            return false
        } else if(binding!!.etCreateNewPassword.text.toString().trim() != binding!!.etConfirmPassword.text.toString().trim()){
            BaseApplication.alertError(context, MessageClass.passwordSameError,false)
            return false
        }
        return true
    }


}
