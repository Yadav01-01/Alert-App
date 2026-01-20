package com.alert.app.model.helpingneighbormodel

data class AddNeighborModel(
    val `data`: AddNeighborModelData?,
    val message: String?,
    val code: Int?,
    val status: Boolean?
)

data class AddNeighborModelData(
    val alert: String?,
    val alert_id: String?,
    val contact_id: Int?,
    val contact_type: String?,
    val email: String?,
    val first_name: String?,
    val last_name: String?,
    val phone: String?,
    val relation: String?,
    val time: String?,
    val type: String?
)