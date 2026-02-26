package com.alert.app.model.watchoverme

data class JourneyStarted(
    val status: Boolean,
    val message: String,
    val code: Int,
    val data: JourneyData
)
data class JourneyData(
    val journey_id: Int,
    val user_id: Int,
    val current_latitude: String,
    val current_longitude: String,
    val destination_latitude: String,
    val destination_longitude: String,
    val started_at: String,
    val journey_status: String,
)
