package com.alert.app.model.emergencycontact

data class AddEmergencyContactModel(
    val code: Int?,
    val `data`: AddEmergencyContactModelData?,
    val message: String?,
    val status: Boolean?
)

data class AddEmergencyContactModelData(
    val alert: String?,
    val alert_id: String?,
    val contact_id: Int?,
    val contact_type: String?,
    val email: String?,
    val first_name: String?,
    val last_name: String?,
    val phone: String?,
    val relation: String?,
    val type: String?
)