package com.alert.app.adapter

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.util.Log
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
import com.alert.app.base.AppConstant
import com.alert.app.model.ChatUserModel
import com.alert.app.model.message.ChatListItem
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.firebase.Timestamp
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class SwipeAdapter(private val context: Context, private val onDeleteClick: (ChatListItem) -> Unit) :
    ListAdapter<ChatListItem, SwipeAdapter.SwipeViewHolder>(DiffCallback()) {


    private val viewBinderHelper = ViewBinderHelper().apply { setOpenOnlyOne(true) }

    // Master list (used for filtering)
    private val fullList = mutableListOf<ChatListItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_swipe_row, parent, false)
        return SwipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: SwipeViewHolder, position: Int) {
        val item = getItem(position)

        // Swipe handling
        viewBinderHelper.bind(holder.swipeLayout, item.chatId)
        holder.imgDelete.setOnClickListener {
            onDeleteClick(item)
        }
        holder.tvName.text = item.fullName

        holder.tvLastMessage.text = if (item.isLiveLocation) {
            "Live Location"
        } else {
            item.lastMessage ?: "Say hi 👋"
        }

        // Unread count
        if (item.unreadCount > 0) {
            holder.tvCount.visibility = View.VISIBLE
            holder.tvCount.text = item.unreadCount.toString()
        } else {
            holder.tvCount.visibility = View.GONE
        }

        Glide.with(context)
            .load(item.profile)
            .placeholder(R.drawable.user_img_icon)
            .error(R.drawable.user_img_icon)
            .into(holder.userImage)

        // Click to open chat
        holder.imgAppbar.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra(AppConstant.NAME, item.fullName)
                putExtra(AppConstant.PROFILE, item.profile)
                putExtra(AppConstant.CHAT_ID, item.chatId)
            }
            context.startActivity(intent)
        }

        holder.timeAgo.text = getTimeAgo(item.lastMessageTime)
    }

    // ---------------------------
    // Public methods
    // ---------------------------

    /** Call this once when data is loaded */
    fun setData(list: List<ChatListItem>) {
        fullList.clear()
        fullList.addAll(list)
        submitList(list)
    }


    fun filter(query: String) {
        if (query.isBlank()) {
            submitList(fullList)
            return
        }

        val filteredList = fullList.filter {
            it.fullName.contains(query, ignoreCase = true)
        }

        submitList(filteredList)
    }


    private fun getTimeAgo(timestamp: Timestamp?): String {
        if (timestamp == null) return ""

        val now = System.currentTimeMillis()
        val time = timestamp.toDate().time
        val diff = now - time

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr${if (hours > 1) "s" else ""} ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
            else -> {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(time))
            }
        }
    }

    // ---------------------------
    // ViewHolder
    // ---------------------------

    inner class SwipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        val tvCount: TextView = itemView.findViewById(R.id.tv_count)
        val swipeLayout: SwipeRevealLayout = itemView.findViewById(R.id.swipe_layout)
        val imgAppbar: LinearLayout = itemView.findViewById(R.id.img_appbar)
        val userImage: CircleImageView = itemView.findViewById(R.id.user_img_message)
        val timeAgo: TextView = itemView.findViewById(R.id.tv_time_ago)
        val imgDelete: ImageView = itemView.findViewById(R.id.img_delete)
    }

    // ---------------------------
    // DiffUtil
    // ---------------------------

    class DiffCallback : DiffUtil.ItemCallback<ChatListItem>() {
        override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem) =
            oldItem.chatId == newItem.chatId

        override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem) =
            oldItem == newItem
    }




//    private val viewBinderHelper = ViewBinderHelper().apply { setOpenOnlyOne(true) }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeViewHolder {
//        val view = LayoutInflater.from(context).inflate(R.layout.item_swipe_row, parent, false)
//        return SwipeViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: SwipeViewHolder, position: Int) {
//        val item = getItem(position)
//
//        // Swipe handling
//        viewBinderHelper.bind(holder.swipeLayout, item.chatId)
//
//        holder.tvName.text = item.fullName
//
//        holder.tvLastMessage.text = if(item.isLiveLocation){
//            "Live Location"
//        }else {
//            item.lastMessage ?: "Say hi 👋"
//        }
//
//
//        // Unread count
//        if (item.unreadCount > 0) {
//            holder.tvCount.visibility = View.VISIBLE
//            holder.tvCount.text = item.unreadCount.toString()
//        } else {
//            holder.tvCount.visibility = View.GONE
//        }
//        Glide.with(context)
//            .load(item.profile)
//            .placeholder(R.drawable.user_img_icon)
//            .error(R.drawable.user_img_icon)
//            .into(holder.userImage);
//
//
//
//        // Click to open chat
//        holder.imgAppbar.setOnClickListener {
//            val intent = Intent(context, ChatActivity::class.java)
//            intent.putExtra(AppConstant.NAME,item.fullName)
//            intent.putExtra(AppConstant.PROFILE, item.profile)
//            intent.putExtra(AppConstant.CHAT_ID, item.chatId)
//            context.startActivity(intent)
//        }
//        holder.timeAgo.setText(getTimeAgo(item.lastMessageTime))
//    }
//
//    fun getTimeAgo(timestamp: Timestamp?): String {
//        if (timestamp == null) return ""
//
//        val now = System.currentTimeMillis()
//        val time = timestamp.toDate().time
//        val diff = now - time
//
//        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
//        val hours = TimeUnit.MILLISECONDS.toHours(diff)
//        val days = TimeUnit.MILLISECONDS.toDays(diff)
//
//        return when {
//            minutes < 1 -> "Just now"
//            minutes < 60 -> "$minutes min ago"
//            hours < 24 -> "$hours hr${if (hours > 1) "s" else ""} ago"
//            days == 1L -> "Yesterday"
//            days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
//            else -> {
//                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
//                sdf.format(Date(time))
//            }
//        }
//    }
//
//    inner class SwipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvName: TextView = itemView.findViewById(R.id.tv_name)
//        val tvLastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
//        val tvCount: TextView = itemView.findViewById(R.id.tv_count)
//        val swipeLayout: SwipeRevealLayout = itemView.findViewById(R.id.swipe_layout)
//        val imgAppbar: LinearLayout = itemView.findViewById(R.id.img_appbar)
//        val userImage : CircleImageView = itemView.findViewById<CircleImageView>(R.id.user_img_message)
//        val timeAgo : TextView = itemView.findViewById<TextView>(R.id.tv_time_ago)
//    }
//
//    class DiffCallback : DiffUtil.ItemCallback<ChatListItem>() {
//
//        override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem) =
//            oldItem.chatId == newItem.chatId
//        override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem) =
//            oldItem == newItem
//    }


}