package com.alert.app.model.contactdetailsactivity

data class ContactDetailsScreenModel(
    val code: Int?,
    val `data`: ContactDetailsScreenModelData?,
    val message: String?,
    val status: Boolean?
)

data class ContactDetailsScreenModelData(
    val address: String,
    val alert: String?,
    val alert_description: String?,
    val alert_duration: String?,
    val alert_id: Int?,
    val contact_id: Int?,
    val contact_type: String?,
    val created_at: Any,
    val email: String?,
    val first_name: Any,
    val last_name: Any,
    val latitude: String?,
    val longitude: String?,
    val phone: String?,
    val profile_pic: Any,
    val relation: String?,
    val relation_id: Int?
)