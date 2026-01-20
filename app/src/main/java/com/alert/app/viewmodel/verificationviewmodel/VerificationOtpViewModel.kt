package com.alert.app.viewmodel.verificationviewmodel

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class VerificationOtpViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun forgotPasswordRequest(successCallback: (response: NetworkResult<String>) -> Unit, email: String?,phone : String?){
        repository.forgotPasswordRequestApi({ successCallback(it) }, email,phone)
    }

    suspend fun signupRequest(successCallback: (response: NetworkResult<String>) -> Unit, emailOrPhone: String, name:String, password:String){
        repository.signupEmailRequestApi({ successCallback(it) }, emailOrPhone,name, password)
    }

    suspend fun resendOtpRequest(successCallback: (response: NetworkResult<String>) -> Unit,type : String, email: String?, phone:String?){
        repository.resendOtp({ successCallback(it) }, type,email, phone)
    }

    suspend fun signupOtpVerifyRequest(successCallback: (response: NetworkResult<String>) -> Unit,  otp: String, email: String?, phoneNumber: String?, token: String, deviceType: String,){
        repository.signupOtpVerifyRequestApi({ successCallback(it) }, otp,email,phoneNumber,token,deviceType)
    }

    suspend fun forGotOtpVerifyRequest(successCallback: (response: NetworkResult<String>) -> Unit,  email: String?,
                                       otp: String,
                                       phoneNumber :String?){
        repository.forGotOtpVerifyRequestApi({ successCallback(it) }, email, otp, phoneNumber)
    }

}