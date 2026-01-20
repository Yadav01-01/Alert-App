package com.alert.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*

import androidx.recyclerview.widget.RecyclerView
import com.alert.app.databinding.ItemcontactlistBinding
import com.alert.app.model.UserMobileContactListModel


class ContactSearchUserAdapter(var context:Context,var lis:MutableList<UserMobileContactListModel>) :
    RecyclerView.Adapter<ContactSearchUserAdapter.ViewHolder>() {


    class ViewHolder(var binding: ItemcontactlistBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItemcontactlistBinding = ItemcontactlistBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(ItemcontactlistBinding)
    }




    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {



    }

    override fun getItemCount(): Int {
        return lis.size
    }

}