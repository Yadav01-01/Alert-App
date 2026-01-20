package com.alert.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.*

import androidx.recyclerview.widget.RecyclerView
import com.alert.app.BuildConfig
import com.alert.app.R
import com.alert.app.databinding.ItemlistnearbypepoleBinding
import com.alert.app.model.mapView.UserData
import com.bumptech.glide.Glide


class NearByPepoleAdapter(var context:Context,val userData:List<UserData>) :
    RecyclerView.Adapter<NearByPepoleAdapter.ViewHolder>() {

    class ViewHolder(var binding: ItemlistnearbypepoleBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItemlistnearbypepoleBinding = ItemlistnearbypepoleBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(ItemlistnearbypepoleBinding)
    }






    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        userData.get(position).name.let {
            holder.binding.tvName.text = it
        }
        userData.get(position).address.let {
            holder.binding.tvAddress.text = it
        }
        userData.get(position).phone_number.let {
            holder.binding.tvPhone.text = "+91 $it"
        }
        userData.get(position).email.let {
            holder.binding.tvEmail.text = it
        }
        userData.get(position).profile_pic.let {
            Glide.with(context)
                .load(BuildConfig.BASE_URL+it)
                .placeholder(R.drawable.user_img_icon)
                .error(R.drawable.user_img_icon)
                .into(holder.binding.userImg)
        }
    }

    override fun getItemCount(): Int {
        return userData.size
    }

}