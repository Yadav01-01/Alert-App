package com.alert.app.model.contact

data class UserContactRequest(val first_name: String,
                              val last_name: String,
                              val email: String,
                              val phone: String,
                              val relation_id: Int,
                              val alert_id: Int,
                              val type: String?)
