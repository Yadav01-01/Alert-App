package com.alert.app.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.databinding.ItemcontactBinding
import com.alert.app.listener.OnClickContact
import com.alert.app.model.contact.Contact


class UserContactListAdapter(var context:Context, var OnClickContact: OnClickContact, var list:MutableList<Contact>, var type:String) :
    RecyclerView.Adapter<UserContactListAdapter.ViewHolder>() {


    class ViewHolder(var binding: ItemcontactBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItemcContactBinding = ItemcontactBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(ItemcContactBinding)
    }


    fun updateList(list:MutableList<Contact>){
        this.list=list
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val data=list[position]


        holder.binding.tvName.text = "${data.first_name.orEmpty()} ${data.last_name}".trim()

        holder.binding.tvRelation.text = data.relation

        if (type.equals("health",true)){
            holder.binding.layEnd.visibility=View.GONE
        }else{
            holder.binding.layEnd.visibility=View.VISIBLE
        }


        holder.binding.imgEdit.setOnClickListener {
            OnClickContact.onClick("edit",data.contact_id.toString())
        }

        holder.binding.view.setOnClickListener {
            OnClickContact.onClick("view",data.contact_id.toString())
        }

        holder.itemView.setOnClickListener {
            OnClickContact.onClick(type,data.contact_id.toString())
        }

        holder.binding.imgDelete.setOnClickListener {
            AlertBoxDelete(position)
        }

    }

    private fun AlertBoxDelete(pos:Int){
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_delete)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams
        val tvOK = dialog.findViewById<TextView>(R.id.tvOK)
        val tvNo = dialog.findViewById<TextView>(R.id.tvNo)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)


        tvOK.setOnClickListener {
            list.removeAt(pos)
            notifyDataSetChanged()
            dialog.dismiss()

        }

        tvNo.setOnClickListener {
            dialog.dismiss()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }



    }

    override fun getItemCount(): Int {
        return list.size
    }

}