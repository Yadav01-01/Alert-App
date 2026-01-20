package com.alert.app.viewmodel.signupviewmodel.apiresponse

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SignupViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun socialLoginRequest(successCallback: (response: NetworkResult<String>) -> Unit, emailOrPhone: String, deviceType:String, token:String){
        repository.socialLoginRequestApi({ successCallback(it) }, emailOrPhone,deviceType, token)
    }

    suspend fun signupRequest(successCallback: (response: NetworkResult<String>) -> Unit,  name:String,email: String, password:String){
        repository.signupEmailRequestApi({ successCallback(it) },name,email, password)
    }

    suspend fun signupPhoneRequest(successCallback: (response: NetworkResult<String>) -> Unit, name:String, countryCode:String, phoneNumber:String, password: String,){
        repository.signupPhoneRequestApi({ successCallback(it) },name, countryCode, phoneNumber,password)
    }

}