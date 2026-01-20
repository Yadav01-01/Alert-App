package com.alert.app.model.selfAlert

data class CreateSelfAlertRequest( val title: String,
                                   val start_date: String,
                                   val end_date: String,
                                   val start_time: String,
                                   val end_time: String,
                                   val description: String)
