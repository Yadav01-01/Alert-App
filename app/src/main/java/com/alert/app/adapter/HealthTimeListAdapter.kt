package com.alert.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*

import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.databinding.DropDownItemHealthBinding
import com.alert.app.model.TimeModel


class HealthTimeListAdapter(
    var context:Context,
    var items: MutableList<TimeModel>,
    var OnClickEvent: (TimeModel) -> Unit
) :
    RecyclerView.Adapter<HealthTimeListAdapter.ViewHolder>() {


    class ViewHolder(var binding: DropDownItemHealthBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val DropDownItemHealthBinding = DropDownItemHealthBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(DropDownItemHealthBinding)
    }




    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        holder.binding.tvData.text=items[position].name

        if (items[position].status!!) {
            holder.binding.tvData.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_icon, 0, 0, 0)
        } else {
            holder.binding.tvData.setCompoundDrawablesWithIntrinsicBounds(R.drawable.uncheck_icon, 0, 0, 0)
        }

        holder.itemView.setOnClickListener {
            OnClickEvent(TimeModel("hii",true)) // Pass the clicked item to the event handler
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

}