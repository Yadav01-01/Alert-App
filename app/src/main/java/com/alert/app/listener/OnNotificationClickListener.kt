package com.alert.app.listener

import com.alert.app.model.notification.AlertModel

interface OnNotificationClickListener {
    fun onClick(alert: AlertModel)
}
