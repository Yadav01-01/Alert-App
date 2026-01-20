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
import com.alert.app.model.message.ChatListItem
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper

class SwipeAdapter(
    private val context: Context
) : ListAdapter<ChatListItem, SwipeAdapter.SwipeViewHolder>(DiffCallback()) {

    private val viewBinderHelper = ViewBinderHelper().apply {
        setOpenOnlyOne(true)
    }

    // Mutable copy of list to support updateOrAdd()
    private val currentListCopy = mutableListOf<ChatListItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_swipe_row, parent, false)
        return SwipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: SwipeViewHolder, position: Int) {
        val item = getItem(position)

        // Unique key for swipe
        viewBinderHelper.bind(holder.swipeLayout, item.chatId)
        viewBinderHelper.closeLayout(item.chatId)

        holder.tvName.text = item.otherUserName
        holder.tvLastMessage.text = item.lastMessage

        // Unread count
        if (item.unreadCount > 0) {
            holder.tvCount.visibility = View.VISIBLE
            holder.tvCount.text = item.unreadCount.toString()
        } else {
            holder.tvCount.visibility = View.GONE
        }

        // Click to open ChatActivity
        holder.imgAppbar.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("receiverId", item.otherUserId)
            }
            context.startActivity(intent)
        }
    }

    inner class SwipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        val tvCount: TextView = itemView.findViewById(R.id.tv_count)

        val imgNotification: ImageView = itemView.findViewById(R.id.img_notification)
        val imgDelete: ImageView = itemView.findViewById(R.id.img_delete)

        val swipeLayout: SwipeRevealLayout = itemView.findViewById(R.id.swipe_layout)
        val imgAppbar: LinearLayout = itemView.findViewById(R.id.img_appbar)

        init {
            imgDelete.setOnClickListener {
                // Optional: Firestore se chat delete / archive
            }

            swipeLayout.setSwipeListener(object : SwipeRevealLayout.SwipeListener {
                override fun onClosed(view: SwipeRevealLayout) {
                    imgAppbar.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.white)
                    )
                }

                override fun onOpened(view: SwipeRevealLayout) {
                    imgAppbar.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.teal_800)
                    )
                }

                override fun onSlide(view: SwipeRevealLayout, slideOffset: Float) {
                    val color = ColorUtils.blendARGB(
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.teal_800),
                        slideOffset
                    )
                    imgAppbar.setBackgroundColor(color)
                }
            })
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatListItem>() {
        override fun areItemsTheSame(
            oldItem: ChatListItem,
            newItem: ChatListItem
        ): Boolean = oldItem.chatId == newItem.chatId

        override fun areContentsTheSame(
            oldItem: ChatListItem,
            newItem: ChatListItem
        ): Boolean = oldItem == newItem
    }

    /**
     * Add new chat or update existing one
     * Preserves ListAdapter animation and swipe layout
     */
    fun updateOrAdd(chatItem: ChatListItem) {
        val index = currentListCopy.indexOfFirst { it.chatId == chatItem.chatId }

        if (index != -1) {
            // Update existing chat
            currentListCopy[index] = chatItem
        } else {
            // Add new chat at top
            currentListCopy.add(0, chatItem)
        }

        // Submit a **new list** to trigger DiffUtil
        submitList(currentListCopy.toList())
    }
}
