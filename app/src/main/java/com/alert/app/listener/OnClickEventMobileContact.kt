package com.alert.app.listener

import com.alert.app.model.ListItem

interface OnClickEventMobileContact {

    fun onClick(data: ListItem.Contact?, pos:Int?)

}