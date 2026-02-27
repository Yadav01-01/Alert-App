package com.alert.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.base.SessionManagement
import com.alert.app.databinding.ItemnotificationBinding
import com.alert.app.listener.OnNotificationClickListener
import com.alert.app.model.Alert
import com.alert.app.model.notification.AlertModel
import com.bumptech.glide.Glide
import com.google.firebase.perf.session.SessionManager


class NotificationAdapter(
    private var alertList: MutableList<Alert>,
    private val listener: OnNotificationClickListener
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private lateinit var sessionManager: SessionManagement


    inner class NotificationViewHolder(val binding: ItemnotificationBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemnotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        sessionManager = SessionManagement(parent.context)

        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val alert = alertList[position]
        with(holder.binding) {
            val myName =   sessionManager.getUserName()
            title.text = if(alert.sender != null) alert.sender.name else alert.contactDetails?.name
            alertText.text = alert.title
            relation.text = alert.relation
            tvTime.text = alert.createdAt
            tvDescription.text = alert.description

            if(alert.sender != null){
                Glide.with(userImg.context)
                    .load(alert.sender.profileImage)
                    .placeholder(R.drawable.dummy_image)
                    .into(userImg)
            }else{
                Glide.with(userImg.context)
                    .load(alert.contactDetails?.profileImage)
                    .placeholder(R.drawable.dummy_image)
                    .into(userImg)
            }

            root.setOnClickListener {
              //  listener.onClick(alert) // or pass whole model if needed
            }


        }
    }

    fun update(alertList: MutableList<Alert>){
        this.alertList = alertList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = alertList.size
}
