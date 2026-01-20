package com.alert.app.model.contact

data class ContactListResponse(
    val status: Boolean,
    val code: Int,
    val message: String,
    val data: List<Contact>
)

data class Contact(
    val contact_id: Int,
    val first_name: String?,   // nullable as it can be null
    val last_name: String,
    val phone: String,
    val email: String,
    val relation: String,
    val alert: String,
    val time: String,
    val alert_id: Int,
    val type: String,
    val profile_pic: String
)
