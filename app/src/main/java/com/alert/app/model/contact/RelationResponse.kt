package com.alert.app.model.contact

data class RelationResponse( val status: Boolean,
                             val message: String,
                             val code: Int,
                             val data: List<Relation>)

data class Relation(
    val id: Int,
    val name: String
)
