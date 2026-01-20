package com.alert.app.listener

import com.alert.app.model.contact.Contact

interface ContactClick {

    fun onClick(data:String, contact: Contact,pos:Int)

}