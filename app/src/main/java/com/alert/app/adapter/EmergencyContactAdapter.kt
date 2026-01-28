package com.alert.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.BuildConfig
import com.alert.app.R
import com.alert.app.model.EmergencyContact
import com.alert.app.databinding.ItememergencycontactBinding
import com.alert.app.listener.OnClickContact
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class EmergencyContactAdapter(
    private val context: Context,
    private val list: MutableList<EmergencyContact>,
    private val onClickContact: OnClickContact
) : RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder>() {


    class ViewHolder(var binding: ItememergencycontactBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItememergencycontactBinding = ItememergencycontactBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(ItememergencycontactBinding)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val data=list[position]

        data.let { data ->
            // Set full name if available
            val fullName = listOfNotNull(data.firstName, data.lastName).joinToString(" ")
            if (fullName.isNotBlank()) {
                holder.binding.tvName.text = fullName
            }

            // Set distance if available
            data.distanceMiles?.let {
                holder.binding.textMilesAway.text = it.toString()
            }

            // Set relation if available
            data.relation?.let {
                holder.binding.tvRelationType.text = "Relation: $it"
            }

            // Set phone number if available
            data.phone?.let {
                holder.binding.tvPhoneNumber.text = it
            }

            // Load profile picture using Glide
            data.profilePic?.let { profilePic ->
                Glide.with(context)
                    .load("${BuildConfig.BASE_URL}$profilePic")
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.no_image)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            holder.binding.layProgess.root.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            holder.binding.layProgess.root.visibility = View.GONE
                            return false
                        }
                    })
                    .into(holder.binding.userImg)
            }
        }



        holder.binding.imgChat.setOnClickListener {
            onClickContact.onClick("chat",data.contactId.toString())
        }

        holder.binding.imgCall.setOnClickListener {
            onClickContact.onClick("call",data.contactId.toString())
        }

        holder.itemView.setOnClickListener {
            onClickContact.onClick("view",data.contactId.toString())
        }
    }



    override fun getItemCount(): Int {
        return list.size
    }

   /* fun update(emergencyContactList: MutableList<EmergencyContact>) {
        getEmergencyContactList=emergencyContactList
        notifyDataSetChanged()

    }*/
   @SuppressLint("NotifyDataSetChanged")
   fun update(newList: List<EmergencyContact>) {
       list.clear()
       list.addAll(newList)
       notifyDataSetChanged()
   }


}