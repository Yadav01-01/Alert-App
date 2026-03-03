package com.alert.app.adapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.ItemhelpingneighborsBinding
import com.alert.app.listener.OnClickContact
import com.alert.app.model.helpingneighbormodel.Contact

class NeighborsAcceptReqAdapter(var context: Context, var getContactList: MutableList<Contact>, var onClickContact: OnClickContact) :
    RecyclerView.Adapter<NeighborsAcceptReqAdapter.ViewHolder1>() {
    class ViewHolder1(var binding: ItemhelpingneighborsBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder1 {
        val layoutInflater = LayoutInflater.from(parent.context)
        val ItemhelpingneighborsBinding = ItemhelpingneighborsBinding.inflate(layoutInflater, parent, false)
        return ViewHolder1(ItemhelpingneighborsBinding)
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder1, @SuppressLint("RecyclerView") position: Int) {

        val data = getContactList[position]

        if (data.first_name!=null){
            if (data.last_name!=null){
                holder.binding.textFullName.text=data.first_name.toString()+" "+data.last_name.toString()
            }
        }

        if (data.created_at!=null){
            val (date, time)=BaseApplication.splitDateTime(data.created_at.toString())
            holder.binding.textDate.text=date
            holder.binding.textTime.text=time
        }

        if (data.address!=null){
            holder.binding.textAddress.text=data.address.toString()
        }

        holder.binding.tvViewAlert.setOnClickListener {
            onClickContact.onClick("openProfile",position.toString())
        }
       holder.binding.tvViewAlert.text = "Accept"

    }

    override fun getItemCount(): Int {
        return getContactList.size
    }

    fun update(contactList: MutableList<Contact>) {
        getContactList=contactList
        notifyDataSetChanged()

    }






}