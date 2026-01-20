package com.alert.app.model.neighborprofile

data class NeighborProfileModel(
    val code: Int?,
    val `data`: NeighborProfileModelData?,
    val message: String?,
    val status: Boolean?
)


data class NeighborProfileModelData(
    val address: String?,
    val alert: String?,
    val alert_description: String?,
    val alert_duration: String?,
    val alert_id: Int?,
    val contact_id: Int?,
    val contact_type: String?,
    val created_at: String?,
    val email: String?,
    val first_name: String?,
    val last_name: String?,
    val phone: String?,
    val profile_pic: String?,
    val relation: String?,
    val relation_id: Int?
)