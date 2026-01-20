package com.alert.app.viewmodel.loginviewmodel.apiresponse

data class LoginRootModel(
    val code: Int,
    val message: String,
    val status: Boolean,
    val data: DataModel?,
    val status_code : Int?=0
)


