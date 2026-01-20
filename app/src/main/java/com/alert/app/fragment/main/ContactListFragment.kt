package com.alert.app.fragment.main

import android.app.Dialog
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.ContactListAdapter
import com.alert.app.adapter.TimeArrayCustomListAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentContactListBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.ContactClick
import com.alert.app.listener.OnClickEventDropDownType
import com.alert.app.model.TimeModel
import com.alert.app.model.contact.Contact
import com.alert.app.model.contact.ContactListResponse
import com.alert.app.viewmodel.contactsviewmodel.ContactsViewModel
import com.alert.app.viewmodel.locationmain.LocationViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactListFragment : Fragment(), ContactClick, OnClickEventDropDownType {

    private lateinit var binding: FragmentContactListBinding
    private lateinit var adapter: ContactListAdapter
    private var contactList: MutableList<Contact> = mutableListOf()
    private lateinit var tvTime: TextView
    private lateinit var popupWindow: PopupWindow
    private val data: MutableList<TimeModel> = mutableListOf()
    private lateinit var viewModel: ContactsViewModel
    private lateinit var locationViewModel: LocationViewModel
    private var selectedDuration: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactListBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this)[ContactsViewModel::class.java]
        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setFooter("locationShare")
        (requireActivity() as MainActivity).setImageShowTv()?.visibility = View.GONE
        (requireActivity() as MainActivity).setImgChatBoot().visibility = View.GONE

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        adapter = ContactListAdapter(requireContext(), this, contactList, "home")
        binding.rcyData.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        getContactList()
    }

    private fun getContactList() {
        if (!BaseApplication.isOnline(requireContext())) {
            showAlert(requireContext(), MessageClass.networkError, false)
            return
        }

        BaseApplication.openDialog()
        lifecycleScope.launch {
            viewModel.getContactList().collect { result ->
                BaseApplication.dismissDialog()
                when (result) {
                    is NetworkResult.Success -> {
                        val response = Gson().fromJson(result.data, ContactListResponse::class.java)
                        handleContactListResponse(response)
                    }
                    is NetworkResult.Error -> {
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun handleContactListResponse(response: ContactListResponse) {
        if (response.code == 200) {
            contactList = response.data.toMutableList()
            adapter.updateList(contactList)
            binding.rcyData.visibility = if (contactList.isNotEmpty()) View.VISIBLE else View.GONE
            binding.tvNoData.visibility = if (contactList.isEmpty()) View.VISIBLE else View.GONE
        } else {
            Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
            binding.rcyData.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
        }
    }

    private fun openAlertBox(contact: Contact) {
        selectedDuration = ""
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_long_time)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes = WindowManager.LayoutParams().apply {
            copyFrom(dialog.window!!.attributes)
        }
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvTime = dialog.findViewById(R.id.tvMinit)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        val tvShare = dialog.findViewById<TextView>(R.id.tvshare)

        dialog.show()

        // Time options
        data.clear()
        data.addAll(listOf(
            TimeModel("15 Minutes", false),
            TimeModel("30 Minutes", false),
            TimeModel("45 Minutes", false),
            TimeModel("1 Hour", false),
            TimeModel("2 Hour", false),
            TimeModel("8 Hour", false),
            TimeModel("12 Hour", false),
            TimeModel("24 Hour", false)
        ))

        tvTime.setOnClickListener {
            val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView = inflater?.inflate(R.layout.item_select_layout, null)
            popupWindow = PopupWindow(popupView, tvTime.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAsDropDown(tvTime, 0, 0, Gravity.CENTER)

            val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)
            rcyData?.adapter = TimeArrayCustomListAdapter(requireContext(), data, this, "time")
        }

        tvShare.setOnClickListener {
            if (selectedDuration.isBlank()) {
                Toast.makeText(requireContext(), "Please select duration", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            callShareLocation(contact.contact_id.toString(), selectedDuration)
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun callShareLocation(contactId: String, duration: String) {
        locationViewModel.locationData.observe(viewLifecycleOwner) { (lat, lng) ->
            if (!BaseApplication.isOnline(requireContext())) {
                AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
                return@observe
            }

            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.shareLocationApi(contactId, duration, lat.toString(), lng.toString())
                    .collect {result ->
                        BaseApplication.dismissDialog()
                        when (result) {
                            is NetworkResult.Success -> {
                                alertBoxSuccess()
                            }
                            is NetworkResult.Error -> {
                                Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
            }
        }
    }

    private fun alertBoxSuccess() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_success)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes = WindowManager.LayoutParams().apply {
            copyFrom(dialog.window!!.attributes)
        }

        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        val tvOK = dialog.findViewById<TextView>(R.id.tvOK)
        dialog.show()

        tvOK.setOnClickListener { dialog.dismiss() }
        imgClose.setOnClickListener { dialog.dismiss() }
    }

    override fun onClickDropDown(pos: String?, type: String?) {
        if (type == "time" && pos != null) {
            val index = pos.toIntOrNull()
            if (index != null && index in data.indices) {
                data.forEachIndexed { i, model ->
                    data[i] = model.copy(status = i == index)
                }

                val selectedItem = data[index]
                tvTime.text = selectedItem.name
                selectedDuration = selectedItem.name ?: ""
            }
        }

        popupWindow.dismiss()
    }


    override fun onClick(data: String, contact: Contact, pos: Int) {
        openAlertBox(contact)
    }
}
