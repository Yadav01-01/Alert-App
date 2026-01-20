package com.alert.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.databinding.ItemlistotherthealthBinding
import com.alert.app.model.TimeModel
import com.alert.app.model.selfAlert.SelfAlert

class OtherHealthListAdapter(var context: Context,var dataSelfAlert: MutableList<SelfAlert>) :
    RecyclerView.Adapter<OtherHealthListAdapter.ViewHolder>() {


    class ViewHolder(var binding: ItemlistotherthealthBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItemlistotherthealthBinding = ItemlistotherthealthBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(ItemlistotherthealthBinding)
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {


        holder.binding.tvAlert.setOnClickListener {
            val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as? LayoutInflater
            val popupView = inflater?.inflate(R.layout.time_layout, null)
            if (popupView != null) {
                // Access views inside the inflated layout using findViewById
                val rcyData = popupView.findViewById<RecyclerView>(R.id.rcy_data)
                val data: MutableList<TimeModel> = mutableListOf()

                data.clear()
                data.add(TimeModel("2 Minutes",true))
                data.add(TimeModel("3 Minutes",false))
                data.add(TimeModel("4 Minutes",false))
                data.add(TimeModel("5 Minutes",false))

                // Define the width dynamically or use a dimension resource for better scalability
                val popupWidth = 600 // Consider using a resource value here
                val popupHeight = RelativeLayout.LayoutParams.WRAP_CONTENT

                // Create the PopupWindow
                val popupWindow = PopupWindow(popupView, popupWidth, popupHeight, true)

                // Display the PopupWindow
                popupWindow.showAsDropDown(holder.binding.tvAlert, 0, 0, Gravity.END)
                rcyData.adapter= HealthTimeListAdapter(context, data) { value ->
                    popupWindow.dismiss()
                }
            } else {
                Log.e("PopupWindow", "Failed to inflate popup view.")
            }
        }
    }


    override fun getItemCount(): Int {
        return dataSelfAlert.size
    }

    fun update(dataOtherAlerts: MutableList<SelfAlert>) {
        dataSelfAlert=dataOtherAlerts
        notifyDataSetChanged()

    }

}