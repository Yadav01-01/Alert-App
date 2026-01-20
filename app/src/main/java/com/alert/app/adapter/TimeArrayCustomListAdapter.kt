package com.alert.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*

import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.databinding.DropDownItemTimeBinding
import com.alert.app.listener.OnClickEventDropDownType
import com.alert.app.model.TimeModel


class TimeArrayCustomListAdapter(var context: Context, var data: MutableList<TimeModel> , var OnClickEventDropDownType: OnClickEventDropDownType,var type:String) : RecyclerView.Adapter<TimeArrayCustomListAdapter.ViewHolder>() {


    class ViewHolder(var binding: DropDownItemTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val DropDownItemTimeBinding = DropDownItemTimeBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(DropDownItemTimeBinding)
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        holder.binding.text.text=data[position].name

        if (type.equals("Alert",true)){
            holder.binding.text.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }else{
            if (data[position]!!.status!!) {
                holder.binding.text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_icon, 0, 0, 0)
            } else {
                holder.binding.text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.uncheck_icon, 0, 0, 0)
            }
        }

        holder.itemView.setOnClickListener {
            OnClickEventDropDownType.onClickDropDown(position.toString(),type)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

}