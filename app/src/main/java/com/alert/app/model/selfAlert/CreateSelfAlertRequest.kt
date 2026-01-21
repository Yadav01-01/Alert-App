package com.alert.app.model.selfAlert

data class CreateSelfAlertRequest( val title: String,
                                   val startDate: String,
                                   val endDate: String,
                                   val startTime: String,
                                   val endTime: String,
                                   val notes: String)
