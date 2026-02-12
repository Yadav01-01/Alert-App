package com.alert.app.fragment.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.UserMobileNumberListAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.base.BaseApplication.alertError
import com.alert.app.databinding.FragmentMobileContactListBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.OnClickEventDropDownType
import com.alert.app.listener.OnClickEventMobileContact
import com.alert.app.model.ListItem
import com.alert.app.model.TimeModel
import com.alert.app.model.UserMobileContactListModel
import com.alert.app.model.contact.AddContactResponse
import com.alert.app.model.contact.AlertsResponse
import com.alert.app.model.contact.RelationResponse
import com.alert.app.model.contact.UserContactRequest
import com.alert.app.model.helpingneighbormodel.CreateHelpingNeighbor
import com.alert.app.viewmodel.contactsviewmodel.MobileContactsViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MobileContactListFragment : Fragment() , OnClickEventMobileContact, OnClickEventDropDownType {


    private lateinit var binding: FragmentMobileContactListBinding
    private lateinit var adapter: UserMobileNumberListAdapter

    private var type:String=""
    var groupedContacts:MutableList<ListItem> = mutableListOf()

    companion object {
        const val CONTACTS_PERMISSION_REQUEST_CODE = 1
    }


    val data: MutableList<TimeModel> = mutableListOf()

    private var selectedAlertId = -1
    private var selectedRelationId = -1
    private val viewModel: MobileContactsViewModel by viewModels()


    lateinit var popupWindow: PopupWindow

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMobileContactListBinding.inflate(layoutInflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        clickEvent()

    }

    private fun clickEvent() {
        // This line use for system back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        binding.pullToRefresh.setOnRefreshListener {
            // Check and request permission
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), CONTACTS_PERMISSION_REQUEST_CODE)
            } else {
                // Permission already granted
                fetchContacts()
            }
        }


        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.edSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {


            }

            override fun afterTextChanged(editable: Editable) {
                adapter.filter(editable.toString())
                /*else{
                    if (groupedContacts.size>0){
                        adapter.updateList(groupedContacts) // Pass the filtered list to the adapter
                        binding.rcyData.visibility = View.VISIBLE
                        binding.layNodata.visibility = View.GONE
                    }else{
                        binding.rcyData.visibility = View.GONE
                        binding.layNodata.visibility = View.VISIBLE
                    }
                }*/
            }
        })
    }

    private fun initView() {
        type=arguments?.getString("type","").toString()
        val mainActivity = requireActivity() as? MainActivity
        if (type.equals("helpingNeighbors", true)
            || type.equals("addContact", true)) {
            mainActivity?.setImageShowTv()?.visibility = View.GONE
            mainActivity?.setImgChatBoot()?.visibility = View.GONE
            binding.shadow.root.visibility = View.VISIBLE
        }

        adapter=UserMobileNumberListAdapter(requireContext(),groupedContacts,this)
        binding.rcyData.adapter= adapter
        // Check and request permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
       //     requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), CONTACTS_PERMISSION_REQUEST_CODE)
            requestContactsPermission()
        } else {
            // Permission already granted
            fetchContacts()
        }
    }


    private fun requestContactsPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            // User already denied once
            showContactsRationaleDialog()
        } else {
            // First time OR "Don't ask again" not yet selected
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                CONTACTS_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showContactsRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Contacts permission required")
            .setMessage(
                "We need access to your contacts so you can easily find and connect with your friends."
            )
            .setPositiveButton("Allow") { _, _ ->
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    CONTACTS_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        binding.pullToRefresh.isRefreshing=false
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                fetchContacts()
            } else {
                // Permission denied
                if (isVivoPhone()) {
                    showVivoPermissionHint()
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                    showGoToSettingsDialog()
                }
            }
        }


    }

    fun isVivoPhone(): Boolean {
        return Build.MANUFACTURER.equals("vivo", true) ||
                Build.BRAND.equals("vivo", true)
    }
    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission required")
            .setMessage(
                "This feature won’t work unless you allow Contacts permission. Please go to Settings and enable the permission manually."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().packageName, null)
                )
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fetchContacts() {
        binding.pullToRefresh.isRefreshing = true

        lifecycleScope.launch(Dispatchers.IO) {

            val contactList = mutableListOf<UserMobileContactListModel>()
            val resolver = requireContext().contentResolver

            val cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            if (cursor == null) {
                Log.d("Contacts", "Cursor is NULL — permission blocked by OEM")
            }

            cursor?.use {
                while (it.moveToNext()) {
                    val name = it.getString(
                        it.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                        )
                    )
                    val number = it.getString(
                        it.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        )
                    )

                    contactList.add(
                        UserMobileContactListModel(name, number, null)
                    )
                }
            }

            withContext(Dispatchers.Main) {
                groupedContacts.clear()
                groupedContacts.addAll(groupContacts(contactList))

                if (groupedContacts.isNotEmpty()) {
                    adapter.updateList(groupedContacts)
                    binding.rcyData.visibility = View.VISIBLE
                    binding.layNodata.visibility = View.GONE
                } else {
                    binding.rcyData.visibility = View.GONE
                    binding.layNodata.visibility = View.VISIBLE
                }

                binding.pullToRefresh.isRefreshing = false
            }
        }
    }


    private fun showVivoPermissionHint() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage(
                "Please allow Contacts permission from system settings. " +
                        "On some devices, this permission needs to be enabled manually."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", requireContext().packageName, null)
                    )
                )
            }
            .show()
    }


    private fun groupContacts(contacts: MutableList<UserMobileContactListModel>): MutableList<ListItem> {
        val grouped = mutableListOf<ListItem>()

        // Sort contacts alphabetically by name
        val sortedContacts = contacts.sortedBy { it.name }

        var currentHeader = ""
        for (contact in sortedContacts) {
            // Get the first letter of the contact's name
            val firstLetter = contact.name?.first()?.uppercaseChar().toString()

            // Check if the header has changed
            if (firstLetter != currentHeader) {
                currentHeader = firstLetter
                grouped.add(ListItem.Header(currentHeader)) // Add header for the letter
            }

            // Add the contact under the current header
            grouped.add(ListItem.Contact(contact.name, contact.number,contact.email))
        }
        return grouped
    }



    override fun onClick(data: ListItem.Contact?, pos:Int?) {
        addAlert(data)
    }

    private fun addAlert(data: ListItem.Contact?) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_addalert)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        dialog.window!!.attributes = layoutParams
        val btnOkay = dialog.findViewById<TextView>(R.id.btnokay)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)
        val tvRelation = dialog.findViewById<MaterialAutoCompleteTextView>(R.id.tvRelation)
        val tvAlerts = dialog.findViewById<MaterialAutoCompleteTextView>(R.id.tvAlerts)
        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)


     /*   tvRelation.setOnClickListener {
            val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = inflater?.inflate(R.layout.item_select_layout, null)
            popupWindow = PopupWindow(popupView, tvRelation.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAsDropDown(tvRelation,  0, 0, Gravity.CENTER)

            // Access views inside the inflated layout using findViewById
            val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)

            rcyData?.adapter= TimeArrayCustomListAdapter(requireContext(),data,this,"time")


            tvRelation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.left_arrow_top, 0)
            // Set the dismiss listener
            popupWindow.setOnDismissListener {
                tvRelation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_bottom, 0)
            }


        }*/

        btnOkay.setOnClickListener {
            data?.let {
                if (it.number.isNullOrEmpty()){
                    alertError(requireContext(), MessageClass.phoneError,false)
                }

                else if (selectedRelationId==-1){
                    alertError(requireContext(), MessageClass.relation,false)
                }
                else if (selectedAlertId==-1){
                    alertError(requireContext(), MessageClass.alert,false)
                }  else  if (!hasCountryCode(it.number.toString())) {
                // ❗ country code missing
                showCountryCodeDialog(requireContext(), it.number.toString()) { updatedNumber ->
                    it.number = updatedNumber
                    if (type== "addEmergency"){
                        val fullName = it.name?.trim().orEmpty()

                        val nameParts = fullName.split("\\s+".toRegex(), limit = 2)

                        val firstName = nameParts.getOrNull(0) ?: ""
                        val lastName = nameParts.getOrNull(1) ?: ""
                        val createHelpingNeighbor = CreateHelpingNeighbor(firstName,
                            lastName,
                            it.email?:"",
                            toValidNumberWithCountryCode(it.number),
                            selectedRelationId.toString(),
                            selectedAlertId.toString(),
                            "device")
                        Log.d("createHelpingNeighbor","$createHelpingNeighbor")
                        addContact1(createHelpingNeighbor,dialog)
                    }else{
                        val fullName = it.name?.trim().orEmpty()

                        val nameParts = fullName.split("\\s+".toRegex(), limit = 2)

                        val firstName = nameParts.getOrNull(0) ?: ""
                        val lastName = nameParts.getOrNull(1) ?: ""
                        val userContactRequest = UserContactRequest(
                            first_name = firstName?:"",
                            last_name= lastName?:"",
                            email= it.email?:"",
                            phone= toValidNumberWithCountryCode(it.number.toString()),
                            relation_id=  selectedRelationId,
                            alert_id=  selectedAlertId,
                            type= "contact",
                            contact_type= "device")
                        addContact(userContactRequest,dialog)
                    }

                }
                return@setOnClickListener
            }
                else{
                    if (type== "addEmergency"){
                        val fullName = it.name?.trim().orEmpty()

                        val nameParts = fullName.split("\\s+".toRegex(), limit = 2)

                        val firstName = nameParts.getOrNull(0) ?: ""
                        val lastName = nameParts.getOrNull(1) ?: ""
                        val createHelpingNeighbor = CreateHelpingNeighbor(firstName,
                            lastName,
                            it.email?:"",
                            toValidNumberWithCountryCode(it.number),
                            selectedRelationId.toString(),
                            selectedAlertId.toString(),
                            "device")
                        Log.d("createHelpingNeighbor","$createHelpingNeighbor")
                        addContact1(createHelpingNeighbor,dialog)
                    }else{
                        val fullName = it.name?.trim().orEmpty()

                        val nameParts = fullName.split("\\s+".toRegex(), limit = 2)

                        val firstName = nameParts.getOrNull(0) ?: ""
                        val lastName = nameParts.getOrNull(1) ?: ""
                        val userContactRequest = UserContactRequest(
                        /*    it.name?:"",
                            "",
                            it.email?:"",
                            it.number!!,
                            selectedRelationId,
                            selectedAlertId,
                            "device"*/
                            first_name = firstName?:"",
                            last_name= lastName?:"",
                            email= it.email?:"",
                            phone= toValidNumberWithCountryCode(it.number.toString()),
                            relation_id=  selectedRelationId,
                            alert_id=  selectedAlertId,
                            type= "contact",
                            contact_type= "device"
                        )
                        addContact(userContactRequest,dialog)
                    }

                }
            }
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
           /* if (type.equals("helpingNeighbors",true) || type.equals("addContact",true)){
                findNavController().navigateUp()
            }else{
                findNavController().navigate(R.id.contactFragment)
            }*/
        }
        tvRelation.post {
            val width = tvRelation.width
            tvRelation.dropDownWidth = width
            tvRelation.dropDownHorizontalOffset = 0
        }
        getRelation(tvRelation)
        getAllAlerts(tvAlerts)
    }

    fun toValidNumberWithCountryCode(input: String?): String {
        if (input.isNullOrBlank()) return ""

        // Remove all non-digit/non-plus characters
        var cleaned = input.trim().replace(Regex("[^0-9+]"), "")

        // Keep + only at the start
        if (cleaned.indexOf('+') > 0) {
            cleaned = cleaned.replace("+", "")
        }

        return cleaned
    }

    private fun addContact(userContactRequest: UserContactRequest, dialog: Dialog) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.addContact(userContactRequest).collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val addContactResponse =
                                    Gson().fromJson(it, AddContactResponse::class.java)
                                if (addContactResponse.code==200) {
                                    if (type.equals("helpingNeighbors",true) ||
                                        type.equals("addContact",true)){
                                        findNavController().navigateUp()
                                    }else{
                                        findNavController().navigate(R.id.contactFragment)
                                    }
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
    private fun addContact1(userContactRequest: CreateHelpingNeighbor, dialog: Dialog) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.addEmergencyContact(userContactRequest).collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val addContactResponse =
                                    Gson().fromJson(it, AddContactResponse::class.java)
                                if (addContactResponse.code==200) {
                                /*    if (type.equals("helpingNeighbors",true) ||
                                        type.equals("addContact",true)){*/
                                        findNavController().navigateUp()
                                  /*  }else{
                                        findNavController().navigate(R.id.contactFragment)
                                    }*/
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
            showAlert(requireContext(), MessageClass.networkError,false)
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
            showAlert(requireContext(), MessageClass.networkError,false)
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

    override fun onClickDropDown(postion: String?, type: String?) {
       /* if (type.equals("time")){
            for (i in data.indices) {
                val item = data[i].copy() // Create a copy to avoid modifying the reference at `position`
                item.name = data[i].name ?: ""
                // Set the status based on position
                item.status = i == postion?.toInt()
                // Update the item in the list
                data[i] = item
            }
            // Set the text of the category at 'position'
            tvRelation.text = data[postion?.toInt()!!].name
        }*/
        popupWindow.dismiss()
    }
    fun showCountryCodeDialog(
        context: Context,
        phone: String,
        onResult: (String) -> Unit
    ) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_country_code)
        dialog.setCancelable(true)
        val window = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        // Optional: Background transparent करें
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val ccp = dialog.findViewById<CountryCodePicker>(R.id.ccp)
        val etPhone = dialog.findViewById<EditText>(R.id.etPhone)
        val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmit)

        etPhone.setText(phone)

        btnSubmit.setOnClickListener {
            val countryCode = ccp.selectedCountryCodeWithPlus
            val number = etPhone.text.toString().trim()

            if (number.isEmpty()) {
                Toast.makeText(context, "Enter phone number", Toast.LENGTH_SHORT).show()
            } else {
                val finalNumber = "$countryCode$number"
                dialog.dismiss()
                onResult(finalNumber)
            }
        }

        dialog.show()
    }
    fun hasCountryCode(number: String): Boolean {
        return number.trim().startsWith("+")
    }
}