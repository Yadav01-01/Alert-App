package com.alert.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*

import androidx.recyclerview.widget.RecyclerView
import com.alert.app.databinding.ItemalertlistBinding


class AlertListAdapter(var context:Context) :
    RecyclerView.Adapter<AlertListAdapter.ViewHolder>() {


    class ViewHolder(var binding: ItemalertlistBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItemalertlistBinding = ItemalertlistBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(ItemalertlistBinding)
    }




    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        holder.binding.tvName.text=""+(position+1)+".Alert for Lorem ipsum dolor"

    }

    override fun getItemCount(): Int {
        return 4
    }

}