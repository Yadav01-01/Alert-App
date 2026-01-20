package com.alert.app.model.mapView

data class NearbyUsersResponse( val status: Boolean,
                                val message: String,
                                val code: Int,
                                val data: List<UserData>)
data class UserData(
    val id: Int,
    val name: String,
    val email: String,
    val username: String?,
    val email_verified_at: String?,
    val jwt_token: String?,
    val device_type: String,
    val google: Int,
    val fcm_token: String?,
    val phone_number: String,
    val phone_verify: String,
    val country: String?,
    val profile_pic: String?,
    val address: String?,
    val latitude: String?,
    val longitude: String?,
    val status: Int,
    val otp: String?,
    val is_verified: Int,
    val token: String?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val profile_status: String,
    val distance: Int
)
