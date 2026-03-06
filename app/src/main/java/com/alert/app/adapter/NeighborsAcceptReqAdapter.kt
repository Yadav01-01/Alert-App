package com.alert.app.adapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.ItemhelpingneighborsBinding
import com.alert.app.databinding.LayoutNeighborsAcceptInvitationBinding
import com.alert.app.listener.NeighborsAcceptDeclineListener
import com.alert.app.listener.OnClickContact
import com.alert.app.model.helpingneighbormodel.Contact
import com.alert.app.model.helpingneighbormodel.InvitationData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener

class NeighborsAcceptReqAdapter(var context: Context, var getContactList: MutableList<InvitationData>, var onClickContact: NeighborsAcceptDeclineListener) :
    RecyclerView.Adapter<NeighborsAcceptReqAdapter.ViewHolder1>() {
    class ViewHolder1(var binding: LayoutNeighborsAcceptInvitationBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder1 {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItemNeighborsAcceptInvitationBinding = LayoutNeighborsAcceptInvitationBinding.inflate(layoutInflater, parent, false)
        return ViewHolder1(ItemNeighborsAcceptInvitationBinding)
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder1, @SuppressLint("RecyclerView") position: Int) {

        val data = getContactList[position]

        if (data.name!=null){
            holder.binding.textFullName.text=data.name.toString()
        }
        if (data.profile_image != null) {

            holder.binding.imageProgress.visibility = View.VISIBLE

            Glide.with(context)
                .load(data.profile_image)
                .placeholder(R.drawable.user_img_icon)
                .error(R.drawable.user_img_icon)
                .listener(object : RequestListener<Drawable> {



                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.imageProgress.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.imageProgress.visibility = View.GONE
                        return false
                    }
                })
                .into(holder.binding.profileImage)
        }


        holder.binding.tvViewAlert.setOnClickListener {
            onClickContact.onClick("accept",position.toString())
        }
        holder.binding.CancelButton.setOnClickListener {
            onClickContact.onClick("decline",position.toString())
        }
       holder.binding.tvViewAlert.text = "Accept"

    }

    override fun getItemCount(): Int {
        return getContactList.size
    }

    fun update(contactList: MutableList<InvitationData>) {
        getContactList=contactList
        notifyDataSetChanged()

    }






}