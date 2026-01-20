package com.alert.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*

import androidx.recyclerview.widget.RecyclerView
import com.alert.app.databinding.ItemcountrylanguageBinding
import com.alert.app.listener.OnClickEvent
import com.alert.app.model.CountryLanguageModel



class CountryLanguageAdapter(var context:Context,  var list: MutableList<CountryLanguageModel>,var OnClickEvent: OnClickEvent) :
    RecyclerView.Adapter<CountryLanguageAdapter.ViewHolder>() {


    class ViewHolder(var binding: ItemcountrylanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItemChatListBinding = ItemcountrylanguageBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(ItemChatListBinding)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val data=list[position]


        holder.binding.tvName.text = data.name


        holder.binding.tvName.setCompoundDrawablesWithIntrinsicBounds(data.image, 0, 0, 0)


        holder.itemView.setOnClickListener {
            OnClickEvent.onClick(data.name.toString()+"@"+data.image.toString())
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

}