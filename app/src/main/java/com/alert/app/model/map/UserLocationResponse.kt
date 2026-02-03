package com.alert.app.model.map

import com.google.gson.annotations.SerializedName

//UserLocationResponse

data class UserLocationResponse(val status: Boolean,
                          val message: String,
                          val code: Int,
                          val data: List<UserLocation>)

// Individual user location data
data class UserLocation(
    @SerializedName("user_id") val userId: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("latitude") val latitude: String? = null,
    @SerializedName("longitude") val longitude: String? = null,
    @SerializedName("user_type") val userType: String? = null,
    @SerializedName("relation") val relation: String? = null,
    @SerializedName("alert_id") val alertId: Long? = null,
    @SerializedName("profile_pic") val profilePic: String? = null
)
