package com.alert.app.model.watchoverme



import com.google.gson.annotations.SerializedName

data class LiveLocationResponse(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("code")
    val code: Int,

    @SerializedName("data")
    val data: LiveLocationData
)



data class LiveLocationData(

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("journey_id")
    val journeyId: Int,

    @SerializedName("current_latitude")
    val currentLatitude: String,

    @SerializedName("current_longitude")
    val currentLongitude: String,

    @SerializedName("destination_latitude")
    val destinationLatitude: String,

    @SerializedName("destination_longitude")
    val destinationLongitude: String,

    @SerializedName("journey_status")
    val journeyStatus: String,

    @SerializedName("route_status")
    val routeStatus: String,  // 👈 New field added

    @SerializedName("last_updated_at")
    val lastUpdatedAt: String
)
