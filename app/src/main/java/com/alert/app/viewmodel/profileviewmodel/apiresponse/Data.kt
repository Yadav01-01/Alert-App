package com.alert.app.viewmodel.profileviewmodel.apiresponse

data class Data(
    val address: String?,
    val country: String?,
    val created_at: String?,
    val email: String?,
    val email_verified_status: Boolean?,
    val is_verified: Int?,
    val lat: String?,
    val longi: String?,
    val name: String?,
    val phone_number: String?,
    val phone_verified_status: Boolean?,
    val profile_pic: String?,
    val profile_status: String?,
    val token: String?,
    val updated_at: String?,
    val user_id: Int,
    val username: String?
)