package com.alert.app.model

import com.google.gson.annotations.SerializedName

data class AddressModel(

    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("type")
    val type: String = "",

    @SerializedName("latitude")
    val latitude: String? = null,

    @SerializedName("longitude")
    val longitude: String? = null,

    @SerializedName("address")
    val address: String = "",

    @SerializedName("default")
    val isDefault: Int = 0,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("default_user_id")
    val defaultUserId: Int? = null
)
