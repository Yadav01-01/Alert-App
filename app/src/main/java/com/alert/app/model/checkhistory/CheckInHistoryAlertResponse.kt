package com.alert.app.model.checkhistory

data class CheckInHistoryAlertResponse(
    val code: Int,
    val `data`: MutableList<CheckInHistoryAlertResponseData>,
    val message: String,
    val status: Boolean
)

data class CheckInHistoryAlertResponseData(
    val alert_id: Int?,
    val alert_type: String?,
    val title: String?,
    val description: String?,
    val start_date: String?,
    val end_date: String?,
    val start_time: String?,
    val end_time: String?,
    val duration: String?,
    val status: Int?,
    val response_status: Int?,
    val created_at: String?,
    val sender_name: String?,
    val sender_address: String?,
    val relation: String?,

    // Optional: Keep these if they appear in other API calls,
    val created_by: String? = null,
    val show_user_id: String? = null,
    val updated_at: String? = null,
    val updated_by: String? = null
)