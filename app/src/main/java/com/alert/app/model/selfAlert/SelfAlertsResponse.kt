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
    val duration: String?, // Nullable, since it can be null
    val show_user_id: String?,
    val status: Int?,
    val response_status: Int?,
    val created_at: String?,
    val created_by: String?,
    val updated_at: String?,
    val updated_by: String? // Nullable
)
