package com.alert.app.fragment.auth

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.alert.app.R
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentSetAlertBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.contact.AddContactResponse
import com.alert.app.model.contact.AlertsResponse
import com.alert.app.model.contact.RelationResponse
import com.alert.app.model.contact.UserContactRequest
import com.alert.app.viewmodel.contactsviewmodel.ContactsViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class SetAlertFragment : Fragment() {
    private lateinit var binding: FragmentSetAlertBinding
    private lateinit var openBottomSheetDialog: BottomSheetDialog
    private var selectedAlertId = -1
    private var selectedRelationId = -1
    private val viewModel: ContactsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetAlertBinding.inflate(layoutInflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvadd.setOnClickListener {
            alertBottom()
        }


        binding.imgCopy.setOnClickListener {
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", binding.tvLink.text.toString())
            clipboard.setPrimaryClip(clip)
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

        tvManually!!.setOnClickListener {
            openBottomSheetDialog.dismiss()
            openAlertBox("add")
        }

        tvContacts!!.setOnClickListener {
            openBottomSheetDialog.dismiss()
            val bundle=Bundle()
            bundle.putString("type","singup")
            findNavController().navigate(R.id.mobileContactListFragment2,bundle)
        }

        tvMap!!.setOnClickListener {
            openBottomSheetDialog.dismiss()
            val bundle=Bundle()
            bundle.putString("type","singup")
            findNavController().navigate(R.id.fromMapFragment2,bundle)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun openAlertBox(data:String){
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
        val edFullName = dialog.findViewById<EditText>(R.id.ed_full_name)
        val edLastName = dialog.findViewById<EditText>(R.id.ed_last_name)
        val edEmail = dialog.findViewById<EditText>(R.id.ed_email)
        val edPhone = dialog.findViewById<EditText>(R.id.ed_phone)
        val tvRelation = dialog.findViewById<MaterialAutoCompleteTextView>(R.id.tvRelation)
        val tvAlerts = dialog.findViewById<MaterialAutoCompleteTextView>(R.id.tvAlerts)
        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        btnAdd.setOnClickListener {
         //   dialog.dismiss()
         //   findNavController().navigate(R.id.contactFragment)
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
                                    dialog.dismiss()
                                    findNavController().navigate(R.id.contactFragment)
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
}