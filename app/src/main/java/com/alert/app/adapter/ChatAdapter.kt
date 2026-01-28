package com.alert.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.model.chatbot.ChatMessage

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
}
