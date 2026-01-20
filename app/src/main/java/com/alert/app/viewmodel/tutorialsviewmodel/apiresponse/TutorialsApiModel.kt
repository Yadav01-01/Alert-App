package com.alert.app.viewmodel.tutorialsviewmodel.apiresponse

data class TutorialsApiModel(
    val code: Int,
    val `data`: Data?,
    val message: String,
    val status: Boolean
)