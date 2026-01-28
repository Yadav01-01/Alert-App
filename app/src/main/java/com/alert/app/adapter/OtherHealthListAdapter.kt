package com.alert.app.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.adapter.SelfHealthListAdapter.ViewHolder
import com.alert.app.databinding.ItemlistotherthealthBinding
import com.alert.app.model.TimeModel
import com.alert.app.model.selfAlert.SelfAlert

class OtherHealthListAdapter(
    private val context: Context,
    private var dataSelfAlert: MutableList<SelfAlert>
) : RecyclerView.Adapter<OtherHealthListAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemlistotherthealthBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemlistotherthealthBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alertData = dataSelfAlert[position]

        holder.binding.apply {
            title.text = alertData.title
            contactName.text = alertData.title
            time.text = alertData.start_time
            relation.text = alertData.end_date
            description.text = alertData.description
        }

        holder.binding.tvAlert.setOnClickListener {
            showTimePopup(holder.binding.tvAlert)
        }
    }

    override fun getItemCount(): Int = dataSelfAlert.size

    fun update(list: MutableList<SelfAlert>) {
        dataSelfAlert = list
        notifyDataSetChanged()
    }
    fun refresh() {
        notifyDataSetChanged()
    }

    private fun showTimePopup(anchor: View) {
        val popupView = LayoutInflater.from(context)
            .inflate(R.layout.time_layout, null, false)

        val recyclerView = popupView.findViewById<RecyclerView>(R.id.rcy_data)

        val timeList = mutableListOf(
            TimeModel("2 Minutes", true),
            TimeModel("3 Minutes", false),
            TimeModel("4 Minutes", false),
            TimeModel("5 Minutes", false)
        )
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = HealthTimeListAdapter(context, timeList) {
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 0, 0, Gravity.END)
    }
}
