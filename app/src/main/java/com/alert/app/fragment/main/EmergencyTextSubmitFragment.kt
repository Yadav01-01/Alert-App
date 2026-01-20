package com.alert.app.fragment.main
import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentEmergencyTextSubmitBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.emergencytextmessage.EmergencyTextMszModel
import com.alert.app.model.neighborprofile.NeighborProfileModel
import com.alert.app.viewmodel.emergencytextmszviewmodel.EmergencyTextMessageViewModel
import com.alert.app.viewmodel.neighborprofile.NeighborProfileViewModel
import com.google.android.material.internal.ViewUtils.showKeyboard
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmergencyTextSubmitFragment : Fragment() {

    private var _binding: FragmentEmergencyTextSubmitBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EmergencyTextMessageViewModel
    private var isEditing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencyTextSubmitBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[EmergencyTextMessageViewModel::class.java]

        initialize()

        setupMainActivityViews()
        setupBackButtonHandler()
        setupSaveButton()

    }

    private fun initialize() {

        // Initial setup (EditText disabled if it has content)
        if (binding.edText.text.toString().trim().isEmpty()) {
            binding.edText.isEnabled = true
            binding.btnSave.text = "Save"
        } else {
            binding.edText.isEnabled = false
            binding.btnSave.text = "Edit"
        }

        getEmergencyMessage()

    }


    private fun getEmergencyMessage() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getEmergencyMessage().collect {
                    BaseApplication.dismissDialog()
                    handleGetApiResponse(it)
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun handleGetApiResponse(it: NetworkResult<JsonObject>) {
        when (it) {
            is NetworkResult.Success -> handleSuccessGetApiResponse(it.data.toString())
            is NetworkResult.Error -> showAlert(it.message, false)
            else -> showAlert(it.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessGetApiResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, EmergencyTextMszModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.status == true) {
                if (apiModel.data!=null){
                    if (apiModel.data.message!=null){
                        binding.edText.isEnabled = false
                        binding.btnSave.text = "Edit"
                        binding.edText.setText(apiModel.data.message.toString())
                    }else{
                        binding.edText.hint= MessageClass.emergencyMessageHint
                        binding.edText.isEnabled = true
                        binding.btnSave.text = "Save"
                    }
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun addEmergencyMessage() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.addEmergencyMessage(binding.edText.text.toString().trim()).collect {
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

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(context, message,status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessApiResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, EmergencyTextMszModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.status == true) {

                binding.edText.isEnabled = false
                binding.btnSave.text = "Edit"

            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
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

    private fun setupMainActivityViews() {
        val activity = requireActivity() as MainActivity
        activity.setImageShowTv()?.visibility = View.GONE
        activity.setImgChatBoot().visibility = View.GONE
    }

    private fun setupBackButtonHandler() {
        // This line use for system back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (binding.btnSave.text.toString().equals("Save",true)) {
                // Save operation
                val message = binding.edText.text.toString().trim()
                if (message.isEmpty()) {
                    BaseApplication.alertError(context, MessageClass.emergencyMessage, false)
                    return@setOnClickListener
                }else{
                    // Call API to save
                    addEmergencyMessage()
                    isEditing = false
                }
            } else {
                // Start editing
                isEditing = true
                binding.edText.isEnabled = true
                binding.edText.requestFocus()
               /* showKeyboard()*/
                binding.btnSave.text = "Save" // Change to Save when clicked
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
