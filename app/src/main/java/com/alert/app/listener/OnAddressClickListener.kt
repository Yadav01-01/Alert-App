package com.alert.app.listener

import com.alert.app.model.AddressModel

interface OnAddressClickListener {
    fun onAddressSelected(address: AddressModel,type:String ="",message :String ="")
}