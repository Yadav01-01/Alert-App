package com.alert.app.viewmodel.loginviewmodel.apiresponse

data class DataModel(
    val id: Int?,
    val user_id: Int?,
    val name: String?,
    val email: String?,
    val phone_number: String?,
    val username: String?,
    val profile_pic: String?,
    val jwt_token: String?,
    val otp: String?,
    val profile_status: String?="No"
)


