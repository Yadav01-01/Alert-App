package com.alert.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.databinding.ItemFaqBinding
import com.alert.app.model.setting.Faq

class FaqAdapter(var context : Context , var list : MutableList<Faq>) :
    RecyclerView.Adapter<FaqAdapter.FaqViewHolder>(){


    inner class FaqViewHolder( var binding : ItemFaqBinding) :RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ItemFaqBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FaqViewHolder(binding)
    }

    override fun getItemCount()= list.size

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
       val currentItem = list[position]
        holder.binding.textFaqQuestion.text = currentItem.question
        holder.binding.textFaqAnswer.text = currentItem.answer
        var open = true
        holder.binding.llmain.setOnClickListener {
            if (open){
                holder.binding.textFaqAnswer.visibility = View.VISIBLE
            }else{
                open = true
                holder.binding.textFaqAnswer.visibility = View.GONE
            }
        }

    }


}