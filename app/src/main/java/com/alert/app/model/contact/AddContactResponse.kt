package com.alert.app.model.contact

data class AddContactResponse( val status: Boolean,
                               val message: String,
                               val code: Int,
                               val data: Contact)

data class AddContactResponse1( val status: Boolean,
                               val message: String,
                               val code: Int,
                               val data: List<Contact>)