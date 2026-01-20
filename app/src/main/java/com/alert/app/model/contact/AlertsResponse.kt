package com.alert.app.model.contact

data class AlertsResponse(val status: Boolean,
                          val message: String,
                          val code: Int,
                          val data: List<Alert>)

data class Alert(
    val id: Int,
    val title: String,
    val duration: String,
    val description: String,
    val status: Int
)
