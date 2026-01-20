package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.ContactListAdapter
import com.alert.app.adapter.TimeArrayCustomListAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentAddedContactsBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.ContactClick
import com.alert.app.listener.OnClickContact
import com.alert.app.listener.OnClickEventDropDownType
import com.alert.app.model.ContactListModel
import com.alert.app.model.TimeModel
import com.alert.app.model.contact.AddContactResponse
import com.alert.app.model.contact.AlertsResponse
import com.alert.app.model.contact.Contact
import com.alert.app.model.contact.ContactListResponse
import com.alert.app.model.contact.RelationResponse
import com.alert.app.model.contact.UserContactRequest
import com.alert.app.viewmodel.contactsviewmodel.ContactsViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddedContactsFragment : Fragment() , ContactClick, OnClickEventDropDownType {
    private lateinit var binding: FragmentAddedContactsBinding
    private lateinit var openBottomSheetDialog: BottomSheetDialog
    private lateinit var adapter: ContactListAdapter
    private var contactList:MutableList<Contact> = mutableListOf()
    private lateinit var tvTime:TextView
    private lateinit var tvAlert:TextView

    val data: MutableList<TimeModel> = mutableListOf()
    val dataAlert: MutableList<TimeModel> = mutableListOf()

    lateinit var popupWindow:PopupWindow
    private val viewModel: ContactsViewModel by viewModels()
    private var selectedAlertId = -1
    private var selectedRelationId = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddedContactsBinding.inflate(layoutInflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        getContactList()


    }
    private fun initView() {
        (requireActivity() as MainActivity).setImageShowTv()?.visibility=View.GONE
        (requireActivity() as MainActivity).setImgChatBoot().visibility =View.GONE
        (requireActivity() as MainActivity).setFooter().visibility =View.GONE
        (requireActivity() as MainActivity).setCircle().visibility =View.GONE

        // This line use for system back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (requireActivity() as MainActivity).setFooter().visibility =View.VISIBLE
                    (requireActivity() as MainActivity).setCircle().visibility =View.VISIBLE
                    findNavController().navigateUp()
                }
            })
        adapter= ContactListAdapter(requireContext(),this,contactList,"home")
        binding.itemRcy.adapter= adapter

        // Set EditText text watcher for filtering
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.btnAdd.setOnClickListener {
            alertBottom()
        }
    }

    private fun getContactList() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getContactList().collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val contactListResponse = Gson().fromJson(it, ContactListResponse::class.java)
                                if (contactListResponse.code==200) {
                                    contactList = contactListResponse.data.toMutableList()
                                    adapter.updateList(contactList)
                                }else{
                                    Toast.makeText(
                                        requireContext(),
                                        contactListResponse.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }else{
            showAlert(requireContext(),MessageClass.networkError,false)
        }
    }


    private fun alertBottom() {
        openBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        openBottomSheetDialog.setContentView(R.layout.bottom_sheet_scanner)
        val cancel: ImageView? = openBottomSheetDialog.findViewById(R.id.img_cross)
        val tvManually: TextView? = openBottomSheetDialog.findViewById(R.id.tv_manually)
        val tvContacts: TextView? = openBottomSheetDialog.findViewById(R.id.tv_contacts)
        val tvMap: TextView? = openBottomSheetDialog.findViewById(R.id.tv_map)

        cancel?.setOnClickListener {
            openBottomSheetDialog.dismiss()
        }
        openBottomSheetDialog.show()

        tvManually?.setOnClickListener {
            openBottomSheetDialog.dismiss()
            openAlertBox("add")
        }

        tvContacts?.setOnClickListener {
            openBottomSheetDialog.dismiss()
            val bundle=Bundle()
            bundle.putString("type","addContact")
            findNavController().navigate(R.id.mobileContactListFragment,bundle)

        }

        tvMap?.setOnClickListener {
            openBottomSheetDialog.dismiss()
            val bundle=Bundle()
            bundle.putString("type","addContact")
            findNavController().navigate(R.id.fromMapFragment,bundle)
        }

    }


    @SuppressLint("SetTextI18n")
    private fun openAlertBox(type:String){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_contact)

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams


        val btnAdd = dialog.findViewById<TextView>(R.id.btnAdd)
        val tvHeading = dialog.findViewById<TextView>(R.id.tv_heading)
        val tvText = dialog.findViewById<TextView>(R.id.tv_text)
        val edFullName = dialog.findViewById<EditText>(R.id.ed_full_name)
        val edLastName = dialog.findViewById<EditText>(R.id.ed_last_name)
        val edEmail = dialog.findViewById<EditText>(R.id.ed_email)
        val edPhone = dialog.findViewById<EditText>(R.id.ed_phone)
        val tvRelation = dialog.findViewById<MaterialAutoCompleteTextView>(R.id.tvRelation)
        val tvAlerts = dialog.findViewById<MaterialAutoCompleteTextView>(R.id.tvAlerts)
        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        btnAdd.setOnClickListener {
            if (BaseApplication.cantactValidationError(requireContext(),edFullName,edLastName,edEmail,edPhone,
                    selectedRelationId,selectedAlertId)){
                val userContactRequest = UserContactRequest(edFullName.text.toString(),
                    edLastName.text.toString(),
                    edEmail.text.toString(),
                    edPhone.text.toString(),
                    selectedRelationId,
                    selectedAlertId,
                    null)
                addManual(userContactRequest,dialog)
            }
        }
        getRelation(tvRelation)
        getAllAlerts(tvAlerts)
    }

    private fun addManual(userContactRequest: UserContactRequest, dialog: Dialog) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.manualContact(userContactRequest).collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val addContactResponse =
                                    Gson().fromJson(it, AddContactResponse::class.java)
                                if (addContactResponse.code==200) {
                                    val contact = addContactResponse.data
                                    contactList.add(contact)
                                    adapter.updateList(contactList)
                                    dialog.dismiss()
                                    alertBoxSuccess()
                                }else{
                                    Toast.makeText(
                                        requireContext(),
                                        addContactResponse.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }else{
            showAlert(requireContext(), MessageClass.networkError,false)
        }
    }

    private fun getRelation(tv_relation: MaterialAutoCompleteTextView) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                    viewModel.getRelation().collect {
                        BaseApplication.dismissDialog()
                        when (it) {
                            is NetworkResult.Success -> {
                                it.data?.let {
                                    val relationResponse =
                                        Gson().fromJson(it, RelationResponse::class.java)
                                    if (relationResponse.code==200) {
                                        val relationList = relationResponse.data
                                        // Extract names
                                        val relationNames = relationList.map { it.name }
                                        // Set to dropdown
                                        val adapter = ArrayAdapter(
                                            requireContext(),
                                            android.R.layout.simple_dropdown_item_1line,
                                            relationNames
                                        )
                                        tv_relation.setAdapter(adapter)
                                        // Handle click
                                        tv_relation.setOnItemClickListener { parent, view, position, id ->
                                            val selectedAlert = relationList[position]
                                            selectedRelationId = selectedAlert.id
                                        }
                                    }else{
                                        Toast.makeText(
                                            requireContext(),
                                            relationResponse.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                            is NetworkResult.Error -> {
                                Toast.makeText(
                                    requireContext(),
                                    it.message.toString(),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
        }else{
            showAlert(requireContext(),MessageClass.networkError,false)
        }
    }

    private fun getAllAlerts(tvAllAlerts: MaterialAutoCompleteTextView) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getAllAlerts().collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val alertsResponse =
                                    Gson().fromJson(it, AlertsResponse::class.java)
                                if (alertsResponse.code==200) {
                                    val alerts = alertsResponse.data
                                    // Extract names
                                    val alertsTitle = alerts.map { it.title }
                                    // Set to dropdown
                                    val adapter = ArrayAdapter(
                                        requireContext(),
                                        android.R.layout.simple_dropdown_item_1line,
                                        alertsTitle
                                    )
                                    tvAllAlerts.setAdapter(adapter)

                                    // Handle click
                                    tvAllAlerts.setOnItemClickListener { parent, view, position, id ->
                                        val selectedAlert = alerts[position]
                                        selectedAlertId = selectedAlert.id
                                    }
                                }else{
                                    Toast.makeText(
                                        requireContext(),
                                        alertsResponse.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }else{
            showAlert(requireContext(),MessageClass.networkError,false)
        }
    }

   @SuppressLint("SetTextI18n")
   private fun alertBoxSuccess(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_success)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams

        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        val tvOK = dialog.findViewById<TextView>(R.id.tvOK)
        val text = dialog.findViewById<TextView>(R.id.tv_text)

        text.text = "Your Contact has been added\nSuccessfully."

        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvOK.setOnClickListener {
            dialog.dismiss()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun onClick(data: String, contact: Contact,pos: Int) {
        getAlert(contact)
    }

    private fun getAlert(contact: Contact) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getAlert(contact.contact_id.toString()).collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                if (it.has(getString(R.string.apiCode)) && it.get(getString(R.string.apiCode)).asInt==200) {
                                    openAlertBox(it.getAsJsonObject("data").asJsonObject)
                                }else{
                                    Toast.makeText(
                                        requireContext(),
                                        it.get(getString(R.string.apiMessahe)).asString,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }else{
            showAlert(requireContext(), MessageClass.networkError,false)
        }
    }

    private fun openAlertBox(jsonObject: JsonObject){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_added_contact)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams

        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        val tvEdit = dialog.findViewById<TextView>(R.id.tv_edit)
        val tvName = dialog.findViewById<TextView>(R.id.tvName)
        val tvAlert = dialog.findViewById<TextView>(R.id.tvAlert)
        val tvRelation = dialog.findViewById<TextView>(R.id.tvRelation)
        val tvDuration = dialog.findViewById<TextView>(R.id.tvDuration)

        jsonObject.let {
            val name = it.get("name")?.asString
            tvName.text = name?:""
            val alert = it.get("alert")?.asString
            tvAlert.text = alert?:""
            val relation = it.get("relation")?.asString
            tvRelation.text = relation?:""
            val duration = it.get("alert duration")?.asString
            tvDuration.text = duration?:""
        }

        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvEdit.setOnClickListener {
            dialog.dismiss()
            editAlertBox()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }
    }
    @SuppressLint("InflateParams")
    private fun editAlertBox(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_added_edit_contact)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams

        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        val tvSave = dialog.findViewById<TextView>(R.id.tv_save)
        tvTime = dialog.findViewById(R.id.tv_Time)
        tvAlert = dialog.findViewById(R.id.tv_Alert)
        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)


        data.clear()
        dataAlert.clear()
        data.add(TimeModel("15 Minutes",false))
        data.add(TimeModel("30 Minutes",false))
        data.add(TimeModel("45 Minutes",false))
        data.add(TimeModel("1 Hour",false))
        data.add(TimeModel("2 Hour",false))
        data.add(TimeModel("8 Hour",false))
        data.add(TimeModel("12 Hour",false))
        data.add(TimeModel("24 Hour",false))



        // Add Alert
        dataAlert.add(TimeModel("Lorem ipsum dolor sit, sit sectetur adipiscing elit.",false))
        dataAlert.add(TimeModel("Lorem ipsum dolor sit, sit sectetur adipiscing elit.",false))
        dataAlert.add(TimeModel("Lorem ipsum dolor sit, sit sectetur adipiscing elit.",false))
        dataAlert.add(TimeModel("Lorem ipsum dolor sit, sit sectetur adipiscing elit.",false))


        tvTime.setOnClickListener {
            tvTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.left_arrow_top, 0)
            val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = inflater?.inflate(R.layout.item_select_layout, null)
            popupWindow = PopupWindow(popupView, tvTime.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAsDropDown(tvTime,  0, 0, Gravity.CENTER)

            // Access views inside the inflated layout using findViewById
            val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)

            rcyData?.adapter= TimeArrayCustomListAdapter(requireContext(),data,this,"time")


            // Set the dismiss listener
            popupWindow.setOnDismissListener {
                tvTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_bottom, 0)
            }

        }

        tvAlert.setOnClickListener {
            tvAlert.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.left_arrow_top, 0)
            val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = inflater?.inflate(R.layout.item_select_layout, null)
            popupWindow = PopupWindow(popupView, tvAlert.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAsDropDown(tvAlert,  0, 0, Gravity.CENTER)

            // Access views inside the inflated layout using findViewById
            val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)

            rcyData?.adapter= TimeArrayCustomListAdapter(requireContext(),dataAlert,this,"Menu Label")


            // Set the dismiss listener
            popupWindow.setOnDismissListener {
                tvAlert.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_bottom, 0)
            }

        }



        tvSave.setOnClickListener {
            dialog.dismiss()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun onClickDropDown(pos: String?, type: String?) {
        if (type.equals("time")){
            for (i in data.indices) {
                val item = data[i].copy() // Create a copy to avoid modifying the reference at `position`
                item.name = data[i].name ?: ""
                // Set the status based on position
                item.status = i == pos?.toInt()
                // Update the item in the list
                data[i] = item
            }
            // Set the text of the category at 'position'
            tvTime.text = data[pos?.toInt()!!].name
        }

        if (type.equals("Menu Label")){
            for (i in dataAlert.indices) {
                val item = dataAlert[i].copy() // Create a copy to avoid modifying the reference at `position`
                item.name = dataAlert[i].name ?: ""
                // Set the status based on position
                item.status = i == pos?.toInt()
                // Update the item in the list
                dataAlert[i] = item
            }
            // Set the text of the category at 'position'
            tvAlert.text = dataAlert[pos?.toInt()!!].name
        }

        popupWindow.dismiss()
    }

}