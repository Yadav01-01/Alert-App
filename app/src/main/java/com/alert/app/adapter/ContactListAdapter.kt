package com.alert.app.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.BuildConfig
import com.alert.app.R
import com.alert.app.databinding.ItemcontactBinding
import com.alert.app.listener.ContactClick
import com.alert.app.listener.OnClickContact
import com.alert.app.model.ContactListModel
import com.alert.app.model.contact.Contact
import com.bumptech.glide.Glide

class ContactListAdapter(
    private val context: Context,
    private val onClickContact: ContactClick,
    private var list: MutableList<Contact>,
    private val type: String) : RecyclerView.Adapter<ContactListAdapter.ViewHolder>()
{
    private var originalList: MutableList<Contact> = ArrayList(list)
    class ViewHolder(val binding: ItemcontactBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemcontactBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: MutableList<Contact>) {
        this.list = newList
        this.originalList = ArrayList(newList) // update master list too
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.binding.tvName.text = "${data.first_name} ${data.last_name.orEmpty()}".trim()
        holder.binding.tvRelation.text = data.relation
        data.profile_pic.let {
            Glide.with(context)
                .load(BuildConfig.BASE_URL+it)
                .placeholder(R.drawable.user_img_icon)
                .error(R.drawable.user_img_icon)
                .into(holder.binding.userImg)
        }
        if (type.equals("signup", true) || type.equals("home", true)) {
            holder.binding.layEnd.visibility = View.VISIBLE
            if (type.equals("home", true)) {
                holder.binding.layEdit.visibility = View.GONE
                holder.binding.view.visibility = View.GONE
                holder.binding.tvAlert.setTextColor(Color.parseColor("#FFFFFF"))
                holder.binding.view.setBackgroundResource(R.drawable.view_alert_bg)
            } else {
                holder.binding.layEdit.visibility = View.VISIBLE
                holder.binding.tvAlert.setTextColor(Color.parseColor("#000000"))
                holder.binding.view.setBackgroundResource(R.drawable.frame_24)
            }
        } else {
            holder.binding.layEnd.visibility = View.GONE
        }

        holder.binding.imgEdit.setOnClickListener {
            onClickContact.onClick("edit", list[position],position)
        }

        holder.binding.view.setOnClickListener {
            onClickContact.onClick("view", list[position],position)
        }

        holder.itemView.setOnClickListener {
            if (!type.equals("signup", true)) {
                onClickContact.onClick(type, list[position],position)
            }
        }

        holder.binding.imgDelete.setOnClickListener {
            onClickContact.onClick("delete", list[position],position)
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    // 🔍 Filter method
    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        list = if (query.isEmpty()) {
            originalList.toMutableList()
        } else {
            originalList.filter {
                it.first_name?.contains(query, ignoreCase = true) == true ||
                        it.last_name?.contains(query, ignoreCase = true) == true ||
                        it.relation?.contains(query, ignoreCase = true) == true
            }.toMutableList()
        }
        notifyDataSetChanged()
    }


}
