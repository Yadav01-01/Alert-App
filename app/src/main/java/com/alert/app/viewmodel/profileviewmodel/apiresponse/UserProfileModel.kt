package com.alert.app.viewmodel.profileviewmodel.apiresponse

data class UserProfileModel(
    val code: Int,
    val `data`: Data?,
    val message: String,
    val status: Boolean
)