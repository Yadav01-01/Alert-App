package com.alert.app.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.activity.ChatActivity
import com.alert.app.model.ChatUserModel
import com.alert.app.model.message.ChatListItem
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper

class SwipeAdapter(private val context: Context) :
    ListAdapter<ChatListItem, SwipeAdapter.SwipeViewHolder>(DiffCallback()) {

    private val viewBinderHelper = ViewBinderHelper().apply { setOpenOnlyOne(true) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_swipe_row, parent, false)
        return SwipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: SwipeViewHolder, position: Int) {
        val item = getItem(position)

        // Swipe handling
        viewBinderHelper.bind(holder.swipeLayout, item.chatId)

        holder.tvName.text = item.fullName
        holder.tvLastMessage.text = item.lastMessage ?: "Say hi 👋"

        // Unread count
        if (item.unreadCount > 0) {
            holder.tvCount.visibility = View.VISIBLE
            holder.tvCount.text = item.unreadCount.toString()
        } else {
            holder.tvCount.visibility = View.GONE
        }

        // Click to open chat
        holder.imgAppbar.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("receiverId", item.userId.toString())
                putExtra("chatId", item.chatId)
                putExtra("receiverName", item.fullName)
                putExtra("receiverProfile", item.profile)
            }
            context.startActivity(intent)
        }
    }

    inner class SwipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        val tvCount: TextView = itemView.findViewById(R.id.tv_count)
        val swipeLayout: SwipeRevealLayout = itemView.findViewById(R.id.swipe_layout)
        val imgAppbar: LinearLayout = itemView.findViewById(R.id.img_appbar)
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatListItem>() {
        override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem) =
            oldItem.chatId == newItem.chatId

        override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem) =
            oldItem == newItem
    }
}