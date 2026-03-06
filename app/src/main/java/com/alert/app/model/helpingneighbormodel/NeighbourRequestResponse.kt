package com.alert.app.model.helpingneighbormodel

data class NeighbourRequestResponse(
    val status: Boolean,
    val message: String,
    val code: Int,
    val data: List<Any>
)