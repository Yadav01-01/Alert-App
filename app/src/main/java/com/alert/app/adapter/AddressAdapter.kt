package com.alert.app.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.ItemaddressrowBinding
import com.alert.app.errormessage.MessageClass


class AddressAdapter(var context:Context) :
    RecyclerView.Adapter<AddressAdapter.ViewHolder>() {


    class ViewHolder(var binding: ItemaddressrowBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItemaddressrowBinding = ItemaddressrowBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(ItemaddressrowBinding)
    }




    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {


        holder.binding.imgThree.setOnClickListener {

            alertBox(holder.binding.imgThree)

        }

    }

    @SuppressLint("SetTextI18n")
    private fun alertBox(imgThree: LinearLayout) {
        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View? = inflater?.inflate(R.layout.item_layout, null)

        // Access views inside the inflated layout using findViewById
        val tvEdit = popupView?.findViewById<TextView>(R.id.tv_block)
        val tvDelete = popupView?.findViewById<TextView>(R.id.tv_remove)

        tvEdit?.text = "Edit"

        tvDelete?.text = "Delete"

        val popupWindow = PopupWindow(popupView, 600, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.showAsDropDown(imgThree,  0, 0, Gravity.END)

        tvDelete?.setOnClickListener {
            popupWindow.dismiss()
            alertBoxBlockAndDelete()
        }

        tvEdit?.setOnClickListener {
            popupWindow.dismiss()
            addAlertBoxAddress()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }


    @SuppressLint("SetTextI18n")
    private fun alertBoxBlockAndDelete(){

        val dialog = Dialog(context)

        dialog.setContentView(R.layout.dialog_delete)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)

        dialog.window!!.attributes = layoutParams
        val tvOK = dialog.findViewById<TextView>(R.id.tvOK)
        val tvNo = dialog.findViewById<TextView>(R.id.tvNo)
        val tvHeading = dialog.findViewById<TextView>(R.id.tv_heading)
        val tvText = dialog.findViewById<TextView>(R.id.tv_text)
        val img = dialog.findViewById<ImageView>(R.id.img)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)

        img.setImageResource(R.drawable.deletc_block_icon)
        tvHeading.text="Delete"
        tvText.text="Are you sure you want to Delete the \nuser!"
        tvOK.text="Delete"
        tvNo.text="Cancel"

        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)


        tvOK.setOnClickListener {
            dialog.dismiss()
        }

        tvNo.setOnClickListener {
            dialog.dismiss()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }


    }


    private fun addAlertBoxAddress(){
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_add_address)

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams


        val edText = dialog.findViewById<EditText>(R.id.ed_text)
        val btnSave = dialog.findViewById<TextView>(R.id.btnSave)
        val imgCross = dialog.findViewById<ImageView>(R.id.img_cross)

        val home = dialog.findViewById<RelativeLayout>(R.id.rl_home)
        val office = dialog.findViewById<RelativeLayout>(R.id.rl_office)
        val hotel = dialog.findViewById<RelativeLayout>(R.id.rl_hotel)
        val other = dialog.findViewById<RelativeLayout>(R.id.rl_other)

        var worktype = ""


        if (worktype == "Home") {
            home.setBackgroundResource(R.drawable.bg_selected_address_btn)
            office.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            hotel.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            other.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
        }

        if (worktype == "Office") {
            home.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            office.setBackgroundResource(R.drawable.bg_selected_address_btn)
            hotel.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            other.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
        }

        if (worktype == "Hotel") {
            home.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            office.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            hotel.setBackgroundResource(R.drawable.bg_selected_address_btn)
            other.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
        }

        if (worktype == "Other") {
            home.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            office.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            hotel.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            other.setBackgroundResource(R.drawable.bg_selected_address_btn)
        }

        home.setOnClickListener {
            worktype = "Home"
            home.setBackgroundResource(R.drawable.bg_selected_address_btn)
            office.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            hotel.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            other.setBackgroundResource(R.drawable.bg_four_side_corner_grey)

        }
        office.setOnClickListener {
            worktype = "Office"
            home.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            office.setBackgroundResource(R.drawable.bg_selected_address_btn)
            hotel.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            other.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
        }
        hotel.setOnClickListener {
            worktype = "Hotel"
            home.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            office.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            hotel.setBackgroundResource(R.drawable.bg_selected_address_btn)
            other.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
        }
        other.setOnClickListener {
            worktype = "Other"
            home.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            office.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            hotel.setBackgroundResource(R.drawable.bg_four_side_corner_grey)
            other.setBackgroundResource(R.drawable.bg_selected_address_btn)
        }

        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        imgCross.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            if (worktype == "") {
                BaseApplication.alertError(context, MessageClass.typeError,false)
            }else{
                if (edText.text.toString().trim().isEmpty()) {
                    BaseApplication.alertError(context, MessageClass.addressError,false)
                } else {
                    dialog.dismiss()
                }
            }
        }


    }

}