package com.alert.app.model.neighborprofile

data class NeighborBlockProfile(
    val code: Int,
    val message: String,
    val status: Boolean
)


data class NeighborDeleteProfile(
    val code: Int,
    val message: String,
    val status: Boolean
)