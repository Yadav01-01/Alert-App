package com.alert.app.viewmodel.profileviewmodel

import androidx.lifecycle.ViewModel
import com.alert.app.di.NetworkResult
import com.alert.app.repository.MainRepository
import com.alert.app.viewmodel.profileviewmodel.apiresponse.UserProfileModel
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject


@HiltViewModel
class UserProfileViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun getProfileRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getProfileRequestApi { successCallback(it) }
    }

    suspend fun sendOtpEmailPhoneRequest(successCallback: (response: NetworkResult<String>) -> Unit,emailOrPhone: String){
        repository.sendOtpEmailPhoneRequestApi({ successCallback(it) }, emailOrPhone)
    }

    suspend fun resendOtp(successCallback: (response: NetworkResult<String>) -> Unit,email: String?,phone : String?,type: String){
        repository.resendOtp({ successCallback(it) }, type,email,phone)
    }



    suspend fun profileUpdateRequest(
        successCallback: (response: NetworkResult<String>) -> Unit,
        cusName: RequestBody,
        cusEmail: RequestBody,
        cusPhone: RequestBody,
        cusAddress: RequestBody,
        cusLatitude: RequestBody,
        cusLongitude: RequestBody,
        requestImage: MultipartBody.Part?
    ){
        repository.profileUpdateRequestApi({ successCallback(it) }, cusName,cusEmail,cusPhone,cusAddress,cusLatitude,cusLongitude,requestImage)
    }

    suspend fun forGotOtpVerifyRequest(successCallback: (response: NetworkResult<String>) -> Unit, email: String?, otp:String,phone : String?){
        repository.forGotOtpVerifyRequestApi({ successCallback(it) }, email,otp,phone)
    }

}