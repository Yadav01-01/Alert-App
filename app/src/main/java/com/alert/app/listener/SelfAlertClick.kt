package com.alert.app.listener
import com.alert.app.model.selfAlert.SelfAlert

interface SelfAlertClick {
    fun onClick(type:String, selfAlert: SelfAlert, pos:Int)
}