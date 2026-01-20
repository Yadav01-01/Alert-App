package com.alert.app.model.setting

data class FaqResponse( val status: Boolean,
                        val message: String,
                        val code: Int,
                        val data: List<Faq>)

data class Faq(
    val id: Int,
    val question: String,
    val answer: String
)
