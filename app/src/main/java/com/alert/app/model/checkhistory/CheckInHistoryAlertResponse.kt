package com.alert.app.model.checkhistory

data class CheckInHistoryAlertResponse(
    val code: Int,
    val `data`: MutableList<CheckInHistoryAlertResponseData>,
    val message: String,
    val status: Boolean
)

data class CheckInHistoryAlertResponseData(
    val alert_type: String?,
    val created_at: String?,
    val created_by: String?,
    val description: String?,
    val duration: Any?,
    val end_date: String?,
    val end_time: String?,
    val id: Int?,
    val relation: String?,
    val response_status: Int?,
    val show_user_id: String?,
    val start_date: String?,
    val start_time: String?,
    val status: Int?,
    val title: String?,
    val updated_at: String?,
    val updated_by: Any?,
    val user_name: String?,
    val address: String?
)