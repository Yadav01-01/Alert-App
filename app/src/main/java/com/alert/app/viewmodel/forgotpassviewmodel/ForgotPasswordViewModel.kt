package com.alert.app.viewmodel.forgotpassviewmodel

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun forgotPasswordRequest(successCallback: (response: NetworkResult<String>) -> Unit, email: String?,phone:String?){
        repository.forgotPasswordRequestApi({ successCallback(it) }, email, phone)
    }

    suspend fun reseatPasswordRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                      email: String?,
                                      phone: String?,
                                      password: String,
                                      password_confirmation: String){
        repository.reseatPasswordRequestApi({ successCallback(it) },email,phone, password,password_confirmation)
    }

}