package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.alert.app.R
import com.alert.app.activity.CallActivity
import com.alert.app.activity.ChatActivity
import com.alert.app.activity.ContactDetailScreenActivity
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.EmergencyContactAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentEmergencyContactsBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.OnClickContact
import com.alert.app.model.contact.AlertsResponse
import com.alert.app.model.contact.RelationResponse
import com.alert.app.model.emergencycontact.EmergencyContact
import com.alert.app.model.emergencycontact.GetEmergencyContactModel
import com.alert.app.model.emergencycontact.GetEmergencyContactModelData
import com.alert.app.model.helpingneighbormodel.CreateHelpingNeighbor
import com.alert.app.viewmodel.addemergencycontactviewmodel.AddEmergencyContactViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmergencyContactsFragment : Fragment(), OnClickContact {

    private lateinit var binding: FragmentEmergencyContactsBinding
    private lateinit var openBottomSheetDialog: BottomSheetDialog
    private lateinit var viewModel: AddEmergencyContactViewModel
    private val getEmergencyContactList: MutableList<EmergencyContact> = mutableListOf()
    private lateinit var adapter: EmergencyContactAdapter
    private var selectedAlertId = -1
    private var selectedRelationId = -1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmergencyContactsBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

  /*      binding.lay1.visibility=View.VISIBLE
        binding.lay2.visibility=View.GONE*/
        (requireActivity() as MainActivity).setImageShowTv()?.visibility=View.GONE
        (requireActivity() as MainActivity).setImgChatBoot().visibility =View.VISIBLE

        viewModel = ViewModelProvider(this)[AddEmergencyContactViewModel::class.java]

        adapter=  EmergencyContactAdapter(requireContext(), getEmergencyContactList,this)
        binding.rcyData.adapter= adapter

     /*   binding.btnAddNowShow.visibility=View.GONE
        Handler().postDelayed({
            binding.lay1.visibility=View.GONE
            binding.lay2.visibility=View.VISIBLE
            binding.btnAddNowShow.visibility=View.VISIBLE
        }, 3000)*/

        binding.btnAddNow.setOnClickListener {
            openAlertBox("add")
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        // This line use for system back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        binding.btnAddNowShow.setOnClickListener {
            alertBottom()
        }

        getEmergencyContacts()

    }

    private fun getEmergencyContacts() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getEmergencyContact().collect {
                    BaseApplication.dismissDialog()
                    handleApiResponse(it)
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun handleApiResponse(it: NetworkResult<JsonObject>) {
        when (it) {
            is NetworkResult.Success -> handleSuccessApiResponse(it.data.toString())
            is NetworkResult.Error -> showAlert(it.message, false)
            else -> showAlert(it.message, false)
        }
    }

    // This is common function for show the alert box
    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(context, message,status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessApiResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, GetEmergencyContactModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.status == true) {
                if (apiModel.data != null) {
                    showDataInUI(apiModel.data)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showDataInUI(data: GetEmergencyContactModelData) {
        try {
            getEmergencyContactList.clear()

            data.contactList?.let {
                getEmergencyContactList.addAll(it)
            }

            if (getEmergencyContactList.size > 0) {
                binding.lay1.visibility=View.GONE
                binding.lay2.visibility=View.VISIBLE
                binding.btnAddNowShow.visibility=View.VISIBLE
                binding.rcyData.visibility = View.VISIBLE
                adapter.update(getEmergencyContactList)
            } else {
                binding.rcyData.visibility = View.GONE
                binding.lay1.visibility=View.VISIBLE
                binding.lay2.visibility=View.GONE
                binding.btnAddNowShow.visibility=View.GONE
            }

        }catch (e:Exception){
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int?, message: String?) {
        if (code== MessageClass.deactivatedUser || code== MessageClass.deletedUser){
            showAlert(message, true)
        }else{
            showAlert(message, false)
        }
    }

    private fun saveEmergencyContacts(createHelpingNeighbor: CreateHelpingNeighbor) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                Log.d("saveEmergencyContacts", "saveEmergencyContacts")
                viewModel.addEmergencyContact(createHelpingNeighbor).collect {
                    BaseApplication.dismissDialog()
                    handleApiResponse1(it)
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun handleApiResponse1(it: NetworkResult<JsonObject>) {
        when (it) {
            is NetworkResult.Success -> {
                getEmergencyContacts()

            }
            is NetworkResult.Error -> showAlert(it.message, false)
            else -> showAlert(it.message, false)
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


        if (type.equals("edit",true)){
            tvHeading.text="Edit Contact"
            tvText.text="You can edit contact from here:"
            btnAdd.text="Save"
            btnAdd.setBackgroundResource(R.drawable.button_bg)
        }else{
            tvHeading.text="Add a Contact"
            tvText.text="You can add a contact here:"
            btnAdd.text="+ Add"
            btnAdd.setBackgroundResource(R.drawable.bg_button)
        }


        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)


        btnAdd.setOnClickListener {

            val createHelpingNeighbor = CreateHelpingNeighbor(edFullName.text.toString(),
                edLastName.text.toString(),
                edEmail.text.toString(),
                edPhone.text.toString(),
                selectedRelationId.toString(),
                selectedAlertId.toString(),
                null)
            saveEmergencyContacts(createHelpingNeighbor)
            dialog.dismiss()
        }
        getRelation(tvRelation)
        getAllAlerts(tvAlerts)

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
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
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
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    override fun onClick(data: String, id: String) {
        if (data.equals("view",true)){
            val intent = Intent(context, ContactDetailScreenActivity::class.java)
            startActivity(intent)
        }
        if(data.equals("call",true)){
            val channelName = "call_${System.currentTimeMillis()}"
            val intent = Intent(requireContext(), CallActivity::class.java).apply {
                putExtra("channelName", channelName)
            }
            startActivity(intent)
        }
        if(data.equals("chat",true)){
            val intent = Intent(context, ChatActivity::class.java)
            startActivity(intent)
        }
    }
}