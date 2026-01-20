package com.alert.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*

import androidx.recyclerview.widget.RecyclerView
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.ItemlistselfthealthBinding
import com.alert.app.model.selfAlert.SelfAlert


class SelfHealthListAdapter(var context: Context, private var dataSelfAlert: MutableList<SelfAlert>) : RecyclerView.Adapter<SelfHealthListAdapter.ViewHolder>() {

    class ViewHolder(var binding: ItemlistselfthealthBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItemlistselfthealthBinding = ItemlistselfthealthBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(ItemlistselfthealthBinding)
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val data = dataSelfAlert[position]

        data.title?.let {
            holder.binding.tvTitle.text = it
        }

        data.description?.let {
            holder.binding.tvDescription.text = it
        }

        data.start_date?.let {
            holder.binding.tvDate.text = BaseApplication.formatDate(it)
        }

        data.start_time?.let {
            holder.binding.tvTime.text = it
        }


    }

    override fun getItemCount(): Int {
        return dataSelfAlert.size
    }

    fun update(data: MutableList<SelfAlert>) {
        dataSelfAlert=data
        notifyDataSetChanged()
    }

}