package com.alert.app.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.databinding.ItemalertrowBinding
import com.alert.app.errormessage.AlertUtils.formatDateTimeRange
import com.alert.app.errormessage.AlertUtils.formatDateTimeRangeSingle
import com.alert.app.listener.SelfAlertClick
import com.alert.app.model.selfAlert.SelfAlert


class SelfAlertAdapter(var context:Context, var selfAlert: List<SelfAlert>,
                       private val selfAlertClick: SelfAlertClick) :
    RecyclerView.Adapter<SelfAlertAdapter.ViewHolder>() {


    class ViewHolder(var binding: ItemalertrowBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItemalertrowBinding = ItemalertrowBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(ItemalertrowBinding)
    }
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        selfAlert[position].title.let {
            holder.binding.tvTitle.text = "$it"
        }
        selfAlert[position].start_date?.let { sd->
                    selfAlert[position].start_time?.let { st->
                        holder.binding.tvTime.text = formatDateTimeRangeSingle("$sd, $st")
                    }
                }
        selfAlert[position].end_date?.let { ed ->
            selfAlert[position].end_time?.let { et ->
                selfAlert[position].start_date?.let { sd->
                    selfAlert[position].start_time?.let { st->
                        holder.binding.tvTime.text = formatDateTimeRange("$sd, $st","$ed, $et")
                    }
                }
            }
        }
        holder.binding.imgThree.setOnClickListener {
            alertBox(holder.binding.imgThree, selfAlert[position],position)

        }

    }

    @SuppressLint("SetTextI18n")
    private fun alertBox(imgThree: LinearLayout, get: SelfAlert, position: Int) {
        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View? = inflater?.inflate(R.layout.item_layout, null)

        // Access views inside the inflated layout using findViewById
        val tvBlock = popupView?.findViewById<TextView>(R.id.tv_block)
        val tvRemove = popupView?.findViewById<TextView>(R.id.tv_remove)

        tvBlock?.text = "Delete"
        tvRemove?.text = "Block"

        val popupWindow = PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.showAsDropDown(imgThree,  0, 0, Gravity.END)

        tvBlock?.setOnClickListener {
            selfAlertClick.onClick("Delete", get,position)
            popupWindow.dismiss()
        }

        tvRemove?.setOnClickListener {
            selfAlertClick.onClick("Block", get,position)
            popupWindow.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return selfAlert.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(selfAlert: List<SelfAlert>){
        this.selfAlert = selfAlert
        notifyDataSetChanged()
    }


}