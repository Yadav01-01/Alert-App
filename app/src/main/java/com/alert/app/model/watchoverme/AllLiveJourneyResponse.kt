package com.alert.app.model.watchoverme

import com.google.gson.annotations.SerializedName

data class AllLiveJourneyResponse(

    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("code")
    val code: Int,

    @SerializedName("data")
    val data: AllLiveJourneyDataWrapper  // 👈 Wrapper class for inner data
)

data class AllLiveJourneyDataWrapper(

    @SerializedName("data")  // 👈 This is the array of journeys
    val journeys: List<LiveJourneyDetail>
)


data class LiveJourneyDetail(

    @SerializedName("journey_id")
    val journeyId: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("user_pickup_latitude")
    val currentPickupLatitude: String,

    @SerializedName("user_pickup_longitude")
    val currentPickupLongitude: String,

    @SerializedName("user_current_latitude")
    val userCurrentLatitude: String,

    @SerializedName("user_current_longitude")
    val userCurrentLongitude: String,

    @SerializedName("user_destination_latitude")
    val userDestinationLatitude: String,

    @SerializedName("user_destination_longitude")
    val userDestinationLongitude: String,

    @SerializedName("journey_status")
    val journeyStatus: String,

    @SerializedName("started_at")
    val startedAt: String,

    @SerializedName("last_updated_at")
    val lastUpdatedAt: String
)