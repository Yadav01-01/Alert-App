package com.alert.app.model.selfAlert

data class SelfAlertsResponse(  val status: Boolean,
                                val message: String,
                                val code: Int,
                                val data: MutableList<SelfAlert>?)
data class SelfAlert(
    val id: Int?,
    val alert_type: String?,
    val title: String?,
    val description: String?,
    val start_date: String?,
    val end_date: String?,
    val start_time: String?,
    val end_time: String?,
    val duration: Int?,
    val alert_duration_minutes: Int?,
    val show_user_id: Int?,
    val relation_id: Int?,
    val status: Int?,
    val response_status: Int?,
    val response_description: String?,
    val created_at: String?,
    val created_by: String?,
    val updated_at: String?,
    val updated_by: String?,
    val deleted_at: String?,
    val alert_user_name: String?,    // NEW
    val relation_name: String?,      // NEW
    val alert_time: String?
)