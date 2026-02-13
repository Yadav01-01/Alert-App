package com.alert.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.model.Message
import com.alert.app.model.chatbot.ChatMessage
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/*class ChatAdapter(
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    companion object {
        private const val TYPE_SENDER = 1
        private const val TYPE_RECEIVER = 2
    }

    fun submitList(list: List<ChatMessage>) {
        messages.clear()
        messages.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId)
            TYPE_SENDER else TYPE_RECEIVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == TYPE_SENDER) {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is SenderVH -> holder.bind(msg)
            is ReceiverVH -> holder.bind(msg)
        }
    }

    override fun getItemCount() = messages.size

    class SenderVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(msg: ChatMessage) {
            itemView.findViewById<TextView>(R.id.tvMessage).text = msg.message
        }
    }

    class ReceiverVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(msg: ChatMessage) {
            itemView.findViewById<TextView>(R.id.tvMessage).text = msg.message
        }
    }
}*/

class ChatAdapter(
    private val currentUserId: String
, private var list : MutableList<Message> = mutableListOf()
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        private const val TYPE_SENDER = 1
        private const val TYPE_RECEIVER = 2
        private var receiverProfile =""
    }

    fun receiverProfile(img:String){
        receiverProfile = img
    }

    fun submitList(list:MutableList<Message>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (list.get(position).senderId.toInt() == currentUserId.toInt())
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

    class SenderVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(msg:Message) {
            itemView.findViewById<TextView>(R.id.tvMessage).text = msg.text
            msg.timestamp?.let {
                itemView.findViewById<TextView>(R.id.tvTime).text = getTimeAgo(msg.timestamp)
            }
        }

        fun getTimeAgo(timestamp: Timestamp?): String {
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


    }

    class ReceiverVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(msg: Message) {
            itemView.findViewById<TextView>(R.id.tvMessage).text = msg.text
            msg.timestamp?.let {
                itemView.findViewById<TextView>(R.id.tvTime).text = getTimeAgo(msg.timestamp)
            }
            Glide.with(itemView.context)
                .load(receiverProfile) // image URL
                .placeholder(R.drawable.img_not_found)
                .error(R.drawable.img_not_found)
                .into(itemView.findViewById(R.id.imgProfile))
        }

        fun getTimeAgo(timestamp: Timestamp?): String {
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

    }




}
