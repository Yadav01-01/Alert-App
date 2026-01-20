package com.alert.app.model.emergencycontact

data class GetEmergencyContactModel(
    val code: Int?,
    val `data`: GetEmergencyContactModelData?,
    val message: String?,
    val status: Boolean?
)

data class GetEmergencyContactModelData(
    val contactList: MutableList<EmergencyContact>?
)

data class EmergencyContact(
    val address: String?,
    val alert: String?,
    val alert_description: String?,
    val alert_duration: String?,
    val alert_id: Int?,
    val contact_id: Int?,
    val contact_type: String?,
    val created_at: String?,
    val distance_miles: String?,
    val email: String?,
    val first_name: String?,
    val last_name: String?,
    val latitude: String?,
    val longitude: String?,
    val phone: String?,
    val profile_pic: String?,
    val relation: String?,
    val relation_id: Int?
)