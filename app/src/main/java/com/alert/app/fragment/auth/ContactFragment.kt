package com.alert.app.fragment.auth

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.AlertListAdapter
import com.alert.app.adapter.ContactListAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.base.SessionManagement
import com.alert.app.databinding.FragmentContactBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.ContactClick
import com.alert.app.model.contact.AddContactResponse
import com.alert.app.model.contact.Alert
import com.alert.app.model.contact.AlertsResponse
import com.alert.app.model.contact.Contact
import com.alert.app.model.contact.ContactListResponse
import com.alert.app.model.contact.Relation
import com.alert.app.model.contact.RelationResponse
import com.alert.app.model.contact.UserContactRequest
import com.alert.app.model.contact.UserEditContactRequest
import com.alert.app.viewmodel.contactsviewmodel.ContactsViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactFragment : Fragment(), ContactClick {

    private lateinit var binding: FragmentContactBinding
    private lateinit var adapter: ContactListAdapter
    private lateinit var sessionManagement: SessionManagement
    private val viewModel: ContactsViewModel by viewModels()
    private var contactList:MutableList<Contact> = mutableListOf()
    private lateinit var openBottomSheetDialog: BottomSheetDialog
    private var selectedAlertId = -1
    private var selectedRelationId = -1
    private var relation: List<Relation> = mutableListOf()
    private var alerts: List<Alert> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManagement = SessionManagement(requireContext())

        setupRecyclerView()
        setupClickListeners()
        getContactList()
        getRelation()
        getAllAlerts()
    }

    private fun setupRecyclerView() {
        adapter = ContactListAdapter(requireContext(), this, contactList, "signup")
        binding.itemRcy.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnAdd.setOnClickListener { alertBottom() }
        binding.btnproceed.setOnClickListener {
            sessionManagement.setLoginSession(true)
            sessionManagement.setProfileScreen("login")
            val intent = Intent(context, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
        // Set EditText text watcher for filtering
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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
                                    Toast.makeText(requireContext(), contactListResponse.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }else{
            showAlert(requireContext(), MessageClass.networkError,false)
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
            openContactDialog("add", null,null)
        }

        tvContacts?.setOnClickListener {
            openBottomSheetDialog.dismiss()
            val bundle=Bundle()
            bundle.putString("type","addContact")
            findNavController().navigate(R.id.mobileContactListFragment2,bundle)
        }

        tvMap?.setOnClickListener {
            openBottomSheetDialog.dismiss()
            val bundle=Bundle()
            bundle.putString("type","addContact")
            findNavController().navigate(R.id.fromMapFragment2,bundle)
        }
    }



    @SuppressLint("SetTextI18n")
    private fun openContactDialog(action: String, contact: Contact?,position:Int?) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_contact)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val btnAdd = dialog.findViewById<TextView>(R.id.btnAdd)
        val edFullName = dialog.findViewById<EditText>(R.id.ed_full_name)
        val edLastName = dialog.findViewById<EditText>(R.id.ed_last_name)
        val edEmail = dialog.findViewById<EditText>(R.id.ed_email)
        val edPhone = dialog.findViewById<EditText>(R.id.ed_phone)
        val tvRelation = dialog.findViewById<MaterialAutoCompleteTextView>(R.id.tvRelation)
        val tvAlerts = dialog.findViewById<MaterialAutoCompleteTextView>(R.id.tvAlerts)
        val tv_heading = dialog.findViewById<TextView>(R.id.tv_heading)
        val tv_text = dialog.findViewById<TextView>(R.id.tv_text)
        if (action=="edit"){
            contact?.let {
                tv_heading.text = "Edit Contact"
                tv_text.text = "You can edit contact from here:"
                edFullName.setText(it.first_name ?: "")
                edLastName.setText(it.last_name ?: "")
                edEmail.setText(it.email ?: "")
                edPhone.setText(it.phone ?: "")
                // Prefill value from name (if given)
                it.relation.let { name ->
                    val index = relation.indexOfFirst { it.name.equals(name, ignoreCase = true) }
                    if (index >= 0) {
                        tvRelation.setText(relation[index].name, false) // false prevents dropdown opening
                        selectedRelationId = relation[index].id          // also set the ID
                    }
                }
                it.alert.let { name ->
                    val index = alerts.indexOfFirst { it.title.equals(name, ignoreCase = true) }
                    if (index >= 0) {
                        tvAlerts.setText(alerts[index].title, false) // false prevents dropdown opening
                        selectedAlertId = alerts[index].id          // also set the ID
                    }
                }
                btnAdd.text = getString(R.string.save)
            }
        }
        btnAdd.setOnClickListener {
            if (btnAdd.text.toString() == getString(R.string.add)) {
                if (BaseApplication.cantactValidationError(requireContext(), edFullName, edLastName, edEmail, edPhone, selectedRelationId, selectedAlertId)) {
                    val userContactRequest = UserContactRequest(
                        edFullName.text.toString(),
                        edLastName.text.toString(),
                        edEmail.text.toString(),
                        edPhone.text.toString(),
                        selectedRelationId,
                        selectedAlertId,
                        null
                    )
                    addManual(userContactRequest, dialog)
                }
            }else{
                if (BaseApplication.cantactValidationError(
                        requireContext(), edFullName, edLastName, edEmail, edPhone,
                        selectedRelationId, selectedAlertId
                    )
                ) {
                    contact?.let {
                        val userEditContactRequest = UserEditContactRequest(it.contact_id.toString(),
                            edFullName.text.toString(),
                            edLastName.text.toString(),
                            edEmail.text.toString(),
                            edPhone.text.toString(),
                            selectedRelationId,
                            selectedAlertId
                        )
                        addEditManual(userEditContactRequest, dialog,it,position)
                    }
                }
            }
        }

        // Show the dialog
        dialog.show()
        val window = dialog.window
        if (window != null) {
            // Make dialog full screen
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

            setRelationData(tvRelation)
            setAllAlertsData(tvAlerts)
    }

    private fun setAllAlertsData(tvAllAlerts: MaterialAutoCompleteTextView?) {
        // Extract names
        val alertsTitle = alerts.map { it.title }
        // Set to dropdown
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            alertsTitle
        )
        tvAllAlerts?.setAdapter(adapter)
        // Handle click
        tvAllAlerts?.setOnItemClickListener { parent, view, position, id ->
            val selectedAlert = alerts[position]
            selectedAlertId = selectedAlert.id
        }
    }

    private fun setRelationData(tv_relation: MaterialAutoCompleteTextView) {
        // Extract names
        val relationNames = relation.map { it.name }
        // Set to dropdown
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            relationNames
        )
        tv_relation.setAdapter(adapter)
        // Handle click
        tv_relation.setOnItemClickListener { parent, view, position, id ->
            val selectedAlert = relation[position]
            selectedRelationId = selectedAlert.id
        }
    }


    private fun addEditManual(userEditContactRequest: UserEditContactRequest,
                              dialog: Dialog,contact: Contact,position:Int?) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.editManualContact(userEditContactRequest).collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val addContactResponse =
                                    Gson().fromJson(it, AddContactResponse::class.java)
                                if (addContactResponse.code==200) {
                                    position?.let {
                                        val contactNew = addContactResponse.data
                                        contactList.set(it,contactNew)
                                        adapter.updateList(contactList)
                                        dialog.dismiss()
                                    }
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
                                    dialog.dismiss()
                                    contactList.add(contact)
                                    adapter.updateList(contactList)
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

    private fun getRelation() {
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
                                    relation = relationResponse.data
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
            showAlert(requireContext(), MessageClass.networkError,false)
        }
    }

    private fun getAllAlerts() {
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
                                    alerts = alertsResponse.data
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
            showAlert(requireContext(), MessageClass.networkError,false)
        }
    }


    override fun onClick(action: String, contact: Contact,position:Int) {
        when (action.lowercase()) {
            "edit" -> openContactDialog("edit",contact,position)
            "delete"-> showDeleteDialog(contact)
            else -> openAlertDialog(contact)
        }
    }

    private fun showDeleteDialog(contact: Contact) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_delete_acc)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Set dialog size to match the window
        dialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)

        val tvOK = dialog.findViewById<TextView>(R.id.tvOK)
        val tvNo = dialog.findViewById<TextView>(R.id.tvNo)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        dialog.show()
        tvOK.setOnClickListener {
            deleteContact(contact,dialog)
        }
        tvNo.setOnClickListener {
            dialog.dismiss()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun deleteContact(contact: Contact, dialog: Dialog) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.deleteContact(contact.contact_id.toString()).collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                if (it.has("code") && it.get("code").asInt==200) {
                                    contactList.remove(contact)
                                    adapter.updateList(contactList)
                                    dialog.dismiss()
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

    @SuppressLint("SetTextI18n")
    private fun openAlertDialog(contactName: Contact) {
        val dialog = createDialog(R.layout.dialog_alert)
        val tvOkay = dialog.findViewById<TextView>(R.id.tvokay)
        val tvName = dialog.findViewById<TextView>(R.id.tv_name)
        val dataRcy = dialog.findViewById<RecyclerView>(R.id.data_rcy)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        dataRcy.adapter = AlertListAdapter(requireContext())
        tvName.text = "Alerts for ${contactName.first_name} ${contactName.last_name}"
        tvOkay.setOnClickListener { dialog.dismiss() }
        imgClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun createDialog(layoutRes: Int): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(layoutRes)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        return dialog
    }
}
