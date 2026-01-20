package com.alert.app.model

sealed class ListItem {
    data class Contact(val name: String?, val number: String?,val email: String?) : ListItem()
    data class Header(val letter: String) : ListItem()
}