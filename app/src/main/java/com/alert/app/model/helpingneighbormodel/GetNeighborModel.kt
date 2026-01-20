package com.alert.app.model.helpingneighbormodel

data class GetNeighborModel(
    val code: Int?,
    val `data`: GetNeighborModelData?,
    val message: String?,
    val status: Boolean?
)

data class GetNeighborModelData(
    val userAddress: UserAddress,
    val alertList: MutableList<Alert>?,
    val contactList: MutableList<Contact>?,
    val relationList: MutableList<Relation>?
)

data class UserAddress(
    val address: String?,
    val latitude: String?,
    val longitude: String?,
    val type: String?
)

data class Alert(
    val id: Int?,
    val name: String?
)

data class Contact(
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

data class Relation(
    val id: Int?,
    val name: String?
)