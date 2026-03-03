package com.alert.app.adapter

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.alert.app.R
import com.alert.app.model.RespondModel

class ResponseDetailDialog(
    context: Context,
    private val data: RespondModel
) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_response_detail)
        setCancelable(true)

        val tvResponseText = findViewById<TextView>(R.id.tvResponseText)
        val btnClose = findViewById<Button>(R.id.btnClose)

        tvResponseText.text = data.response_description ?: context.getString(R.string.no_response_text)

        btnClose.setOnClickListener { dismiss() }
    }
}
