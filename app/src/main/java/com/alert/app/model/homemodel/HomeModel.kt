package com.alert.app.model.homemodel

data class HomeModel(
    val code: Int?,
    val `data`: HomeModelData?,
    val message: String?,
    val status: Boolean?
)

data class HomeModelData(
    val emergency_message: String?,
    val notified_users: MutableList<NotifiedUser>?
)

data class NotifiedUser(
    val id: Int,
    val name: String
)