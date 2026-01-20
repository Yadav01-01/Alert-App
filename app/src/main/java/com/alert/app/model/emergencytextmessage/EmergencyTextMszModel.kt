package com.alert.app.model.emergencytextmessage

data class EmergencyTextMszModel(
    val code: Int?,
    val `data`: EmergencyTextMszModelData?,
    val message: String?,
    val status: Boolean?
)

data class EmergencyTextMszModelData(
    val created_at: String?,
    val id: Int?,
    val message: String?,
    val updated_at: String?,
    val user_id: Int?
)