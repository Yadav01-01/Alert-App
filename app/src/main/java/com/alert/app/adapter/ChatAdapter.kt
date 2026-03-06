package com.alert.app.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.model.Message
import com.alert.app.model.MessageType
import com.alert.app.model.chatbot.ChatMessage
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.Calendar


class ChatAdapter(
    private val currentUserId: String, private var from: String = "main"
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    sealed class ChatItem {
        data class MessageItem(val message: Message) : ChatItem()
        data class DateHeader(val dateText: String, val timestamp: Long) : ChatItem()
    }

    companion object {
        private const val TYPE_SENDER = 1
        private const val TYPE_RECEIVER = 2
        private const val TYPE_DATE_HEADER = 3
        private var receiverProfile = ""

        // Date format constants
        private val todayFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    }

    private var chatItems: MutableList<ChatItem> = mutableListOf()

    fun receiverProfile(img: String) {
        receiverProfile = img
    }

    fun submitList(messages: MutableList<Message>) {
        chatItems = groupMessagesByDate(messages)
        notifyDataSetChanged()
    }

    private fun groupMessagesByDate(messages: List<Message>): MutableList<ChatItem> {
        val groupedItems = mutableListOf<ChatItem>()
        val calendar = Calendar.getInstance()

        // Clear time part for date comparison
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        // Tomorrow
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowStart = calendar.timeInMillis

        var lastDateHeader: String? = null

        // Sort messages by timestamp (oldest first for proper grouping)
        val sortedMessages = messages.sortedBy { it.timestamp?.toDate()?.time ?: 0 }

        sortedMessages.forEach { message ->
            val messageTime = message.timestamp?.toDate()?.time ?: 0L
            val dateHeader = getDateHeaderText(messageTime, todayStart, tomorrowStart)

            // Add header if it's different from the last one
            if (dateHeader != lastDateHeader) {
                groupedItems.add(ChatItem.DateHeader(dateHeader, messageTime))
                lastDateHeader = dateHeader
            }

            groupedItems.add(ChatItem.MessageItem(message))
        }

        return groupedItems
    }

    private fun getDateHeaderText(messageTime: Long, todayStart: Long, tomorrowStart: Long): String {
        return when {
            messageTime >= todayStart && messageTime < tomorrowStart -> "Today"
            messageTime >= tomorrowStart && messageTime < tomorrowStart + 86400000 -> "Tomorrow"
            else -> {
                // For other dates, show the date in DD-MM-YYYY format
                val date = Date(messageTime)
                todayFormat.format(date)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = chatItems[position]) {
            is ChatItem.DateHeader -> TYPE_DATE_HEADER
            is ChatItem.MessageItem -> {
                if (item.message.senderId == currentUserId) TYPE_SENDER else TYPE_RECEIVER
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SENDER -> SenderVH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_sender, parent, false)
            )
            TYPE_RECEIVER -> ReceiverVH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_receiver, parent, false)
            )
            TYPE_DATE_HEADER -> DateHeaderVH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_date_header, parent, false)
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = chatItems[position]) {
            is ChatItem.DateHeader -> (holder as DateHeaderVH).bind(item.dateText)
            is ChatItem.MessageItem -> {
                when (holder) {
                    is SenderVH -> holder.bind(item.message)
                    is ReceiverVH -> holder.bind(item.message)
                }
            }
        }
    }

    // FIXED: Use chatItems.size instead of list.size
    override fun getItemCount() = chatItems.size

    // -------------------- DATE HEADER VIEWHOLDER --------------------

    inner class DateHeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDateHeader = view.findViewById<TextView>(R.id.tvDateHeader)

        fun bind(dateText: String) {
            tvDateHeader.text = dateText
        }
    }

    // -------------------- SENDER --------------------

    inner class SenderVH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        private val tvTime = view.findViewById<TextView>(R.id.tvTime)
        private val tvLocationStatus = view.findViewById<TextView>(R.id.tvLocationStatus)
        private val btnOpenMap = view.findViewById<View>(R.id.btnOpenMap)

        fun bind(msg: Message) {
            tvTime.text = getTimeAgo(msg.timestamp)

            if (msg.type == MessageType.TEXT) {
                tvMessage.visibility = View.VISIBLE
                tvLocationStatus.visibility = View.GONE
                btnOpenMap.visibility = View.GONE

                tvMessage.text = msg.text
            } else {
                tvMessage.visibility = View.GONE
                tvLocationStatus.visibility = View.VISIBLE
                btnOpenMap.visibility = View.VISIBLE

                tvLocationStatus.text =
                    if (isLiveLocation(msg)) "📍 Live location"
                    else "📍 Location"

                btnOpenMap.setOnClickListener {
                    openMap(msg, itemView.context)
                }
            }
        }
    }

    // -------------------- RECEIVER --------------------

    inner class ReceiverVH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        private val tvTime = view.findViewById<TextView>(R.id.tvTime)
        private val tvLocationStatus = view.findViewById<TextView>(R.id.tvLocationStatus)
        private val btnOpenMap = view.findViewById<View>(R.id.btnOpenMap)
        private val imgProfile = view.findViewById<ImageView>(R.id.imgProfile)

        fun bind(msg: Message) {
            tvTime.text = getTimeAgo(msg.timestamp)

            if (from != "AiBox"){
                Glide.with(itemView.context)
                    .load(receiverProfile)
                    .placeholder(R.drawable.img_not_found)
                    .error(R.drawable.img_not_found)
                    .into(imgProfile)
            }else{
                Glide.with(itemView.context)
                    .load(R.drawable.ai_bot_icon)
                    .placeholder(R.drawable.ai_bot_icon)
                    .error(R.drawable.ai_bot_icon)
                    .into(imgProfile)
            }


            if (msg.type == MessageType.TEXT) {
                tvMessage.visibility = View.VISIBLE
                tvLocationStatus.visibility = View.GONE
                btnOpenMap.visibility = View.GONE

                tvMessage.text = msg.text
            } else {
                tvMessage.visibility = View.GONE
                tvLocationStatus.visibility = View.VISIBLE
                btnOpenMap.visibility = View.VISIBLE

                tvLocationStatus.text =
                    if (isLiveLocation(msg)) "📍 Live location • updating"
                    else "📍 Location"

                btnOpenMap.setOnClickListener {
                    openMap(msg, itemView.context)
                }
            }
        }
    }

    // -------------------- HELPERS --------------------

    private fun isLiveLocation(message: Message): Boolean {
        val now = System.currentTimeMillis()
        val expired = message.expiresAt != null &&
                message.expiresAt.seconds * 1000 < now

        return message.type == MessageType.LOCATION &&
                message.isLive &&
                !expired
    }

    private fun openMap(message: Message, context: Context) {
        if (message.location == null) return

        val uri = Uri.parse(
            "geo:${message.location.latitude},${message.location.longitude}"
        )

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        context.startActivity(intent)
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
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(time))
        }
    }
}
/*
class ChatAdapter(
    private val currentUserId: String
, private var list : MutableList<Message> = mutableListOf()
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{


    companion object {
        private const val TYPE_SENDER = 1
        private const val TYPE_RECEIVER = 2
        private var receiverProfile = ""
    }

    fun receiverProfile(img: String) {
        receiverProfile = img
    }

    fun submitList(list: MutableList<Message>) {
        this.list = list
        notifyDataSetChanged()

    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].senderId == currentUserId)
            TYPE_SENDER else TYPE_RECEIVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENDER) {
            SenderVH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_sender, parent, false)
            )
        } else {
            ReceiverVH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_receiver, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = list[position]
        when (holder) {
            is SenderVH -> holder.bind(msg)
            is ReceiverVH -> holder.bind(msg)
        }
    }

    override fun getItemCount() = list.size

    // -------------------- SENDER --------------------

    inner class SenderVH(view: View) : RecyclerView.ViewHolder(view) {

        private val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        private val tvTime = view.findViewById<TextView>(R.id.tvTime)
        private val tvLocationStatus = view.findViewById<TextView>(R.id.tvLocationStatus)
        private val btnOpenMap = view.findViewById<View>(R.id.btnOpenMap)

        fun bind(msg: Message) {
            tvTime.text = getTimeAgo(msg.timestamp)

            if (msg.type == MessageType.TEXT) {
                tvMessage.visibility = View.VISIBLE
                tvLocationStatus.visibility = View.GONE
                btnOpenMap.visibility = View.GONE

                tvMessage.text = msg.text
            } else {
                tvMessage.visibility = View.GONE
                tvLocationStatus.visibility = View.VISIBLE
                btnOpenMap.visibility = View.VISIBLE

                tvLocationStatus.text =
                    if (isLiveLocation(msg)) "📍 Live location"
                    else "📍 Location"

                btnOpenMap.setOnClickListener {
                    openMap(msg, itemView.context)
                }
            }
        }
    }

    // -------------------- RECEIVER --------------------

    inner class ReceiverVH(view: View) : RecyclerView.ViewHolder(view) {

        private val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        private val tvTime = view.findViewById<TextView>(R.id.tvTime)
        private val tvLocationStatus = view.findViewById<TextView>(R.id.tvLocationStatus)
        private val btnOpenMap = view.findViewById<View>(R.id.btnOpenMap)
        private val imgProfile = view.findViewById<ImageView>(R.id.imgProfile)

        fun bind(msg: Message) {
            tvTime.text = getTimeAgo(msg.timestamp)

            Glide.with(itemView.context)
                .load(receiverProfile)
                .placeholder(R.drawable.img_not_found)
                .error(R.drawable.img_not_found)
                .into(imgProfile)

            if (msg.type == MessageType.TEXT) {
                tvMessage.visibility = View.VISIBLE
                tvLocationStatus.visibility = View.GONE
                btnOpenMap.visibility = View.GONE

                tvMessage.text = msg.text
            } else {
                tvMessage.visibility = View.GONE
                tvLocationStatus.visibility = View.VISIBLE
                btnOpenMap.visibility = View.VISIBLE

                tvLocationStatus.text =
                    if (isLiveLocation(msg)) "📍 Live location • updating"
                    else "📍 Location"

                btnOpenMap.setOnClickListener {
                    openMap(msg, itemView.context)
                }
            }
        }
    }

    // -------------------- HELPERS --------------------

    private fun isLiveLocation(message: Message): Boolean {
        val now = System.currentTimeMillis()
        val expired = message.expiresAt != null &&
                message.expiresAt.seconds * 1000 < now

        return message.type == MessageType.LOCATION &&
                message.isLive &&
                !expired
    }

    private fun openMap(message: Message, context: Context) {
        if (message.location == null) return

        val uri = Uri.parse(
            "geo:${message.location.latitude},${message.location.longitude}"
        )

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        context.startActivity(intent)
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
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(time))
        }
    }
}
*/

//    companion object {
//        private const val TYPE_SENDER = 1
//        private const val TYPE_RECEIVER = 2
//        private var receiverProfile =""
//    }
//
//    fun receiverProfile(img:String){
//        receiverProfile = img
//    }
//
//    fun submitList(list:MutableList<Message>) {
//        this.list = list
//        notifyDataSetChanged()
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return if (list.get(position).senderId.toInt() == currentUserId.toInt())
//            TYPE_SENDER else TYPE_RECEIVER
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == TYPE_SENDER) {
//            SenderVH(
//                LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_chat_sender, parent, false)
//            )
//        } else {
//            ReceiverVH(
//                LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_chat_receiver, parent, false)
//            )
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val msg = list[position]
//        when (holder) {
//            is SenderVH -> holder.bind(msg)
//            is ReceiverVH -> holder.bind(msg)
//        }
//    }
//
//    override fun getItemCount() = list.size
//
//    class SenderVH(view: View) : RecyclerView.ViewHolder(view) {
//        fun bind(msg:Message) {
//            itemView.findViewById<TextView>(R.id.tvMessage).text = msg.text
//            msg.timestamp?.let {
//                itemView.findViewById<TextView>(R.id.tvTime).text = getTimeAgo(msg.timestamp)
//            }
//        }
//
//        fun getTimeAgo(timestamp: Timestamp?): String {
//            if (timestamp == null) return ""
//
//            val now = System.currentTimeMillis()
//            val time = timestamp.toDate().time
//            val diff = now - time
//
//            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
//            val hours = TimeUnit.MILLISECONDS.toHours(diff)
//            val days = TimeUnit.MILLISECONDS.toDays(diff)
//
//            return when {
//                minutes < 1 -> "Just now"
//                minutes < 60 -> "$minutes min ago"
//                hours < 24 -> "$hours hr${if (hours > 1) "s" else ""} ago"
//                days == 1L -> "Yesterday"
//                days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
//                else -> {
//                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
//                    sdf.format(Date(time))
//                }
//            }
//        }
//
//
//    }
//
//    class ReceiverVH(view: View) : RecyclerView.ViewHolder(view) {
//        fun bind(msg: Message) {
//            itemView.findViewById<TextView>(R.id.tvMessage).text = msg.text
//            msg.timestamp?.let {
//                itemView.findViewById<TextView>(R.id.tvTime).text = getTimeAgo(msg.timestamp)
//            }
//            Glide.with(itemView.context)
//                .load(receiverProfile) // image URL
//                .placeholder(R.drawable.img_not_found)
//                .error(R.drawable.img_not_found)
//                .into(itemView.findViewById(R.id.imgProfile))
//        }
//
//        fun getTimeAgo(timestamp: Timestamp?): String {
//            if (timestamp == null) return ""
//
//            val now = System.currentTimeMillis()
//            val time = timestamp.toDate().time
//            val diff = now - time
//
//            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
//            val hours = TimeUnit.MILLISECONDS.toHours(diff)
//            val days = TimeUnit.MILLISECONDS.toDays(diff)
//
//            return when {
//                minutes < 1 -> "Just now"
//                minutes < 60 -> "$minutes min ago"
//                hours < 24 -> "$hours hr${if (hours > 1) "s" else ""} ago"
//                days == 1L -> "Yesterday"
//                days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
//                else -> {
//                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
//                    sdf.format(Date(time))
//                }
//            }
//        }
//
//    }
//    fun isLiveLocation(message: Message): Boolean {
//        val now = System.currentTimeMillis()
//        val expired = message.expiresAt != null &&
//                message.expiresAt.seconds * 1000 < now
//
//        return message.type == MessageType.LOCATION &&
//                message.isLive &&
//                !expired
//    }




