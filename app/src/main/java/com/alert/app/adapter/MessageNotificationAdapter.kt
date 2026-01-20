package com.alert.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.databinding.ItemmessagelistBinding
import com.alert.app.model.notification.AlertModel
import com.bumptech.glide.Glide

class MessageNotificationAdapter(
    private val messageList: List<AlertModel> // Or another model if different
) : RecyclerView.Adapter<MessageNotificationAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(val binding: ItemmessagelistBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemmessagelistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        with(holder.binding) {
            tvName.text = message.name
            tvTime.text = message.time
            chatDesc.text = message.description // assuming "description" = chat content

            Glide.with(userImg.context)
                .load(message.image.trim())
                .placeholder(com.alert.app.R.drawable.dummy_image)
                .into(userImg)
        }
    }

    override fun getItemCount(): Int = messageList.size
}
