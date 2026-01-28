package com.alert.app.model

import com.google.gson.annotations.SerializedName

data class EmergencyContactResponse(
    @SerializedName("status")
    val status: Boolean? = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("code")
    val code: Int? = null,

    @SerializedName("data")
    val data: List<EmergencyContact>? = emptyList()
)
data class EmergencyContact(
    @SerializedName("contact_id")
    val contactId: Int? = null,

    @SerializedName("first_name")
    val firstName: String? = null,

    @SerializedName("last_name")
    val lastName: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("relation_id")
    val relationId: Int? = null,

    @SerializedName("relation")
    val relation: String? = null,

    @SerializedName("alert_id")
    val alertId: Int? = null,

    @SerializedName("alert")
    val alert: String? = null,

    @SerializedName("alert_duration")
    val alertDuration: String? = null,

    @SerializedName("alert_description")
    val alertDescription: String? = null,

    @SerializedName("contact_type")
    val contactType: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("latitude")
    val latitude: String? = null,

    @SerializedName("longitude")
    val longitude: String? = null,

    @SerializedName("profile_pic")
    val profilePic: String? = null,

    @SerializedName("distance_miles")
    val distanceMiles: Double? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
)

