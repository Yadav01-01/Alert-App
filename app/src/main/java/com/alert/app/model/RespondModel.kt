package com.alert.app.model

import com.alert.app.model.checkhistory.CheckInHistoryAlertResponseData

data class ResponseModelParent(
    val code: Int,
    val data: MutableList<RespondModel>,
    val message: String,
    val status: Boolean
)


data class RespondModel(

    val alert_id: Int = 0,
    val alert_type: String = "",
    val title: String = "",
    val description: String = "",
    val start_date: String = "",
    val end_date: String = "",
    val start_time: String = "",
    val end_time: String? = null,
    val alert_duration: Int = 0,
    val created_at: String = "",
    val is_responded: Boolean = false,
    val response_description: String? = null,
    val relation: String = "",
    val responder_name: String = "",
    val responder_email: String = "",
    val responder_address: String = "",
    val alert_type_display: String = ""
)
