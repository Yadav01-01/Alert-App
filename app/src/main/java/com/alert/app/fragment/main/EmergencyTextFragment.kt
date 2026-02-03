package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentEmergencyTextBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.emergencytextmessage.EmergencyTextMszModel
import com.alert.app.viewmodel.emergencytextmszviewmodel.EmergencyTextMessageViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmergencyTextFragment : Fragment() {

    private var _binding: FragmentEmergencyTextBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EmergencyTextMessageViewModel
    private var isEditing = false
    private var emergencyText :String =""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEmergencyTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[EmergencyTextMessageViewModel::class.java]

        setupUI()
        setupBackButton()
        setupListeners()
        getEmergencyMessage()
    }

    private fun setupUI() {
        (requireActivity() as? MainActivity)?.apply {
            setImageShowTv()?.visibility = View.GONE
            setImgChatBoot().visibility = View.GONE
        }
    }

    private fun setupBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun setupListeners() {
        binding.btnOkay.setOnClickListener {
            val bundle = Bundle().apply {
                putString("emergency_text","")
            }
            findNavController().navigate(R.id.emergencyTextSubmitFragment,bundle)
        }

//        binding.backBtn.setOnClickListener {
//            setupBackButton()
//        }
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
                   val bundle = Bundle().apply {
                       putString("emergency_text",apiModel.data.message)
                   }
                    findNavController().navigate(R.id.emergencyTextSubmitFragment,bundle)
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
                    findNavController().navigate(R.id.homeFragment)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
