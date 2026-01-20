package com.alert.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.listener.OnClickEventMobileContact
import com.alert.app.model.ListItem


class UserMobileNumberListAdapter(var context:Context,var items:MutableList<ListItem>
    , var onClickEventMobileContact: OnClickEventMobileContact)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private var originalItems: MutableList<ListItem> = items.toMutableList()
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CONTACT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.Contact -> TYPE_CONTACT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.itemcontactlist, parent, false)
            ContactViewHolder(view)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(items:MutableList<ListItem>){
        this.items=items
        this.originalItems = items.toMutableList()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is HeaderViewHolder && item is ListItem.Header) {
            holder.bind(item.letter)
        } else if (holder is ContactViewHolder && item is ListItem.Contact) {
            holder.bind(item.name)
            holder.itemView.setOnClickListener {
                onClickEventMobileContact.onClick(item,position)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerText: TextView = itemView.findViewById(R.id.headerText)
        fun bind(letter: String?) {
            headerText.text = letter
        }
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contactName: TextView = itemView.findViewById(R.id.tv_name)
        private val lay_click: RelativeLayout = itemView.findViewById(R.id.lay_click)
        fun bind(name: String?) {
            contactName.text = name
        }

    }
    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        val filteredList = mutableListOf<ListItem>()

        if (query.isEmpty()) {
            filteredList.addAll(originalItems)
        } else {
            var currentHeader: ListItem.Header? = null

            for (item in originalItems) {
                when (item) {
                    is ListItem.Header -> {
                        currentHeader = item
                    }
                    is ListItem.Contact -> {
                        if (item.name?.contains(query, ignoreCase = true)==true) {
                            // Add header before matching contacts (if not already added)
                            if (filteredList.lastOrNull() != currentHeader) {
                                currentHeader?.let { filteredList.add(it) }
                            }
                            filteredList.add(item)
                        }
                    }
                }
            }
        }

        items.clear()
        items.addAll(filteredList)
        notifyDataSetChanged()
    }




}