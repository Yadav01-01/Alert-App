package com.alert.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.alert.app.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? = null

    @SuppressLint("MissingInflatedId")
    override fun getInfoContents(marker: Marker): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_marker_info, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val tvDistance = view.findViewById<TextView>(R.id.tvDistance)

        // Marker tag carries the info: "PICKUP|5 min ago|1.2 mi" or "DEST|8 min|2.3 mi"
        val tag = marker.tag as? String ?: return null
        val parts = tag.split("|")

        when {
            tag.startsWith("PICKUP") -> {
                tvTitle.text = "🏪 Pickup Point"
                tvTime.text = if (parts.size > 1) "Departed: ${parts[1]}" else "Pickup"
                tvDistance.text = if (parts.size > 2) "Traveled: ${parts[2]}" else ""
            }
            tag.startsWith("DEST") -> {
                tvTitle.text = "🏠 Destination"
                tvTime.text = if (parts.size > 1) "ETA: ${parts[1]}" else "Destination"
                tvDistance.text = if (parts.size > 2) "Remaining: ${parts[2]}" else ""
            }
            tag.startsWith("RIDER") -> {
                tvTitle.text = "🚴 Delivery Boy"
                tvTime.text = if (parts.size > 1) parts[1] else ""
                tvDistance.text = if (parts.size > 2) parts[2] else ""
            }
            else -> return null
        }

        return view
    }
}