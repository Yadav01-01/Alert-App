package com.alert.app.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.alert.app.R
import com.alert.app.model.TimeModel


class TimeArrayAdapter(context: Context, var items: MutableList<TimeModel>) : ArrayAdapter<TimeModel>(context, 0, items) {  // Note '0' as layout resource since we are inflating manually.

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.drop_down_item_time, parent, false)
        val textView = view.findViewById<TextView>(R.id.text)

        try {
            textView.text = getItem(position)!!.name

            if (getItem(position)!!.status!!) {
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_icon, 0, 0, 0)
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.uncheck_icon, 0, 0, 0)
            }

        } catch (e: java.lang.Exception) {
            Log.d("******", "Error :- " + e.message.toString())
        }

        return view
    }

}

