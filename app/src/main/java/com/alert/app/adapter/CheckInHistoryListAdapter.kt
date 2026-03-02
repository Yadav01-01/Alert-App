package com.alert.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*

import androidx.recyclerview.widget.RecyclerView
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.ItemlistcheckinsBinding
import com.alert.app.listener.OnClickContact
import com.alert.app.listener.OnClickEvent
import com.alert.app.model.checkhistory.CheckInHistoryAlertResponseData
import com.alert.app.model.selfAlert.SelfAlert


class CheckInHistoryListAdapter(
    var context: Context, var type: String,
    var dataSelfAlert: MutableList<CheckInHistoryAlertResponseData>,
    var OnClickEvent: OnClickContact
) : RecyclerView.Adapter<CheckInHistoryListAdapter.ViewHolder>() {

    class ViewHolder(var binding: ItemlistcheckinsBinding) : RecyclerView.ViewHolder(binding.root) {

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItemlistcheckinsBinding = ItemlistcheckinsBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(ItemlistcheckinsBinding)
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val data = dataSelfAlert[position]
        data.sender_name?.let {
            userName -> if (data.alert_type.equals("self", ignoreCase = true)) {
                holder.binding.tvRelation.visibility =   View.GONE
                holder.binding.tvAlertOrSelfName.text =  "Self Alert: $userName"
            }
        else {
                holder.binding.tvRelation.visibility=View.VISIBLE
                holder.binding.tvAlertOrSelfName.text = "Alert By: $userName"
            }
        }

        data.start_date?.let {
            holder.binding.tvDate.text = data.start_date+if(data.end_date != null )" - "+data.end_date else ""
        }

        data.start_time?.let {
            holder.binding.tvTime.text = data.start_time + if(data.end_time != null )" - "+data.end_time else ""
        }

        data.relation?.let {
            holder.binding.tvRelation.text = it
        }

        data.sender_address?.let {
            holder.binding.tvAddress.text = it
        }

        holder.binding.tvViewAlert.setOnClickListener {
            OnClickEvent.onClick(type,position.toString())
        }
    }

    override fun getItemCount(): Int {
        return dataSelfAlert.size
    }


    fun listUpdate(type:String){
        this.type=type
    }

    fun update(dataHistoryAlert: MutableList<CheckInHistoryAlertResponseData>) {
        dataSelfAlert=dataHistoryAlert
        notifyDataSetChanged()
    }

}