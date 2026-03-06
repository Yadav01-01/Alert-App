package com.alert.app.model.helpingneighbormodel

data class PendingInvitationsResponse(
    val status: Boolean,
    val message: String,
    val code: Int,
    val data: List<InvitationData>
)

data class InvitationData(
    val relationship_id: Int,
    val sender_id: Int,
    val name: String,
    val profile_image: String
)