package com.alert.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Alert(
    val id: Int,
    val type: String,

    val title: String,
    val description: String,

    @SerialName("alert_id")
    val alertId: Int,

    val relation: String,

    @SerialName("is_read")
    val isRead: Boolean,

    @SerialName("created_at")
    val createdAt: String,

    // sender can be null OR duplicated
    val sender: UserDetail? = null,

    @SerialName("contact_details")
    val contactDetails: UserDetail? = null,

    @SerialName("alert_type")
    val alertType: String
)

data class UserDetail(
    val id: Int,
    val name: String,

    @SerialName("profile_image")
    val profileImage: String? = null
)
