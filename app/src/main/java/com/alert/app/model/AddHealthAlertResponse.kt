package com.alert.app.model

data class AddHealthAlertResponse(
    val status: Boolean = false,
    val message: String = "",
    val code: Int = 0,
    val data: HealthAlertData = HealthAlertData()
)
data class HealthAlertData(
    val alert_ids: List<Int> = emptyList(),
    val alertFor: String = ""
)

