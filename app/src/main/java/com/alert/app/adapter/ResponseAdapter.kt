package com.alert.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.databinding.ItemlistcheckinsBinding
import com.alert.app.listener.OnClickContact
import com.alert.app.model.RespondModel
import com.alert.app.adapter.ResponseDetailDialog
import com.alert.app.model.ResponseModelParent

class ResponseAdapter(
    var context: Context, var type: String,
    var list: MutableList<RespondModel>,
    var onClickEvent: OnClickContact
) : RecyclerView.Adapter<ResponseAdapter.ViewHolder>() {

    class ViewHolder(var binding: ItemlistcheckinsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemlistcheckinsBinding = ItemlistcheckinsBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(itemlistcheckinsBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.binding.tvAlertOrSelfName.text = context.getString(
            com.alert.app.R.string.response_by,
            data.responder_name
        )
        holder.binding.tvDate.text = buildString {
            append(data.start_date)
            if (data.end_date.isNotBlank()) {
                append(" - ")
                append(data.end_date)
            }
        }
        holder.binding.tvTime.text = if (data.end_time?.isNotBlank() ?: false) {
            "${data.start_time} - ${data.end_time}"
        } else {
            data.start_time
        }
        holder.binding.tvRelation.text = data.relation
        holder.binding.tvAddress.text = data.responder_address
        holder.binding.tvViewAlert.text ="View Response"
        holder.binding.tvViewAlert.setOnClickListener {
            openDialogForResponse(data)
        }
    }

    private fun openDialogForResponse(data: RespondModel) {
        val dialog = ResponseDetailDialog(context, data)
        dialog.show()
    }

    override fun getItemCount(): Int {
        return list.size
    }


    fun updateData(newList: MutableList<RespondModel>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

}