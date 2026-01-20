package com.alert.app.model.notification

data class AlertModel(
    val id: Int,
    val name: String,
    val image: String,
    val alert: String,
    val relation: String,
    val description: String,
    val time: String
)
