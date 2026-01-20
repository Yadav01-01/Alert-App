package com.alert.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.databinding.ItemnotificationBinding
import com.alert.app.listener.OnNotificationClickListener
import com.alert.app.model.notification.AlertModel
import com.bumptech.glide.Glide

class NotificationAdapter(
    private val alertList: List<AlertModel>,
    private val listener: OnNotificationClickListener
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(val binding: ItemnotificationBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemnotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val alert = alertList[position]
        with(holder.binding) {
            title.text = alert.name
            alertText.text = alert.alert  // `alertText` is the ID renamed below for clarity
            relation.text = alert.relation
            tvTime.text = alert.time
            tvDescription.text = alert.description

            // Load image with Glide (optional fallback)
            Glide.with(userImg.context)
                .load(alert.image.trim())
                .placeholder(R.drawable.dummy_image)
                .into(userImg)

            root.setOnClickListener {
                listener.onClick(alert) // or pass whole model if needed
            }
        }
    }

    override fun getItemCount(): Int = alertList.size
}
