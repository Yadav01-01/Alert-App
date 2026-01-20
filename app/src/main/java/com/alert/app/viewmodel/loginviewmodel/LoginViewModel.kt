package com.alert.app.viewmodel.loginviewmodel

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun loginRequest(successCallback: (response: NetworkResult<String>) -> Unit, email: String, password:String, token : String,deviceType:String){
        repository.loginRequestApi({ successCallback(it) }, email,password,token,deviceType)
    }

    suspend fun loginPhoneRequest(successCallback: (response: NetworkResult<String>) -> Unit, phone: String,countryCode : String , password:String, token : String,deviceType:String){
        repository.loginPhoneRequestApi({ successCallback(it)}, phone,countryCode,password,token,deviceType)
    }

    suspend fun socialLoginRequest(successCallback: (response: NetworkResult<String>) -> Unit, emailOrPhone: String, deviceType:String, token:String){
        repository.socialLoginRequestApi({ successCallback(it) }, emailOrPhone,deviceType, token)
    }


}