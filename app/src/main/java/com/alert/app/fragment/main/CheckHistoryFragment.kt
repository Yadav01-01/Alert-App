package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.CheckInHistoryListAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentCheckHistoryBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.OnClickContact
import com.alert.app.model.checkhistory.AlertResponseSuccess
import com.alert.app.model.checkhistory.CheckInHistoryAlertResponse
import com.alert.app.model.checkhistory.CheckInHistoryAlertResponseData
import com.alert.app.viewmodel.checkhistory.CheckHistoryViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CheckHistoryFragment : Fragment(), OnClickContact {

    private lateinit var binding: FragmentCheckHistoryBinding
    private lateinit var adapter: CheckInHistoryListAdapter
    private lateinit var viewModel: CheckHistoryViewModel
    private val activeColor = "#0B0202"
    private val inactiveColor = "#777777"
    private var type: String = "check_in"
    private val dataSelfAlert: MutableList<CheckInHistoryAlertResponseData> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckHistoryBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[CheckHistoryViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFooter()
        setupUI()
        setupAdapter()
    }

    private fun setupFooter() {
        (requireActivity() as MainActivity).apply {
            setFooter("checkHistory")
            setImageShowTv()?.visibility = View.GONE
            setImgChatBoot().visibility = View.GONE
        }
    }

    private fun setupUI() {
        binding.layCheckIns.setOnClickListener {
            updateTabState(isCheckInTab = true)
            getCheckHistoryAlerts("check_in")
        }

        binding.layResponds.setOnClickListener {
            updateTabState(isCheckInTab = false)
            getCheckHistoryAlerts("response")
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        getCheckHistoryAlerts("check_in")

    }

    private fun getCheckHistoryAlerts(type: String) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.checkInUserAlert(type).collect {
                    BaseApplication.dismissDialog()
                    handleApiResponse(it, type)
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun handleApiResponse(it: NetworkResult<JsonObject>, type: String) {
        when (it) {
            is NetworkResult.Success -> handleSuccessApiResponse(it.data.toString(), type)
            is NetworkResult.Error -> showAlert(it.message, false)
            else -> showAlert(it.message, false)
        }
    }



    // This is common function for show the alert box
    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(context, message, status)
    }


    @SuppressLint("SetTextI18n")
    private fun handleSuccessApiResponse(data: String, type: String) {
        try {
            val apiModel = Gson().fromJson(data, CheckInHistoryAlertResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.status) {
                if (apiModel.data != null) {
                    showDataInUI(apiModel.data, type)
                }
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showDataInUI(data: MutableList<CheckInHistoryAlertResponseData>, type: String) {
        dataSelfAlert.clear()
        try {
            data.let {
                dataSelfAlert.addAll(it)
            }

            if (dataSelfAlert.size > 0) {
                binding.rcyData.visibility = View.VISIBLE
                adapter.update(dataSelfAlert)
            } else {
                binding.rcyData.visibility = View.GONE
            }

        } catch (e: Exception) {
            showAlert(e.message, false)

        }

    }

    private fun handleError(code: Int?, message: String?) {
        if (code == MessageClass.deactivatedUser || code == MessageClass.deletedUser) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    private fun setupAdapter() {
        adapter = CheckInHistoryListAdapter(requireContext(), type,dataSelfAlert, this)
        binding.rcyData.adapter = adapter
    }

    private fun updateTabState(isCheckInTab: Boolean) {
        with(binding) {
            view1.visibility = if (isCheckInTab) View.VISIBLE else View.GONE
            view2.visibility = if (isCheckInTab) View.GONE else View.VISIBLE
            checkIns.setTextColor(Color.parseColor(if (isCheckInTab) activeColor else inactiveColor))
            responds.setTextColor(Color.parseColor(if (isCheckInTab) inactiveColor else activeColor))
        }
        adapter.listUpdate(if (isCheckInTab) "check_in" else "response")
    }


    @SuppressLint("SetTextI18n")
    private fun showAlertDialog(data: String, alertId:String, description:String, responseDate:String,alertType:String,userName:String) {
        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_check_in_response)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }

        with(dialog) {
            findViewById<ImageView>(R.id.img_close).setOnClickListener { dismiss() }

            val edText = findViewById<TextView>(R.id.ed_text)
            val tvAlertUserName = findViewById<TextView>(R.id.tvAlertUserName)
            val lay1 = findViewById<LinearLayout>(R.id.lay1)
            val lay2 = findViewById<LinearLayout>(R.id.lay2)
            val tvRespondedTime = findViewById<TextView>(R.id.tvRespondedTime)

            if (data.equals("check_in", ignoreCase = true)) {
                edText.isEnabled = true
                lay1.visibility = View.VISIBLE
                lay2.visibility = View.GONE
            } else {
                edText.isEnabled = false
                lay1.visibility = View.GONE
                lay2.visibility = View.VISIBLE
            }

            if (alertType.equals("self",true)){
                tvAlertUserName.text="Self Alert: $userName"
            }else{
                tvAlertUserName.text="Alert By: $userName"
            }

            edText.text=description.toString()
            tvRespondedTime.text= "Responded Date &Time - $responseDate"

            findViewById<TextView>(R.id.tvResponse).setOnClickListener {
                setResponseAlerts(dialog,alertId,edText.text.toString())
                updateTabState(isCheckInTab = false)
            }

            findViewById<TextView>(R.id.tvCancel).setOnClickListener {
                dialog.dismiss()
                cancelAlertDialog()
            }
        }

        dialog.show()
    }

    private fun setResponseAlerts(dialog: Dialog, alertId: String, description: String) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.responseAlert(alertId,description).collect {
                    BaseApplication.dismissDialog()
                    handleApiAlertResponse(it, type,dialog)
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun handleApiAlertResponse(it: NetworkResult<JsonObject>, type: String, dialog: Dialog) {
        when (it) {
            is NetworkResult.Success -> handleSuccessApiAlertResponse(it.data.toString(), type,dialog)
            is NetworkResult.Error -> showAlert(it.message, false)
            else -> showAlert(it.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessApiAlertResponse(data: String, type: String, dialog: Dialog) {
        try {
            val apiModel = Gson().fromJson(data, AlertResponseSuccess::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.status) {
                dialog.dismiss()
                getCheckHistoryAlerts(type)
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun cancelAlertDialog() {
        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_res_cancel)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }
        with(dialog) {
            findViewById<ImageView>(R.id.img_close).setOnClickListener { dismiss() }
            findViewById<TextView>(R.id.safe).setOnClickListener {
                dismiss()
            }
            findViewById<TextView>(R.id.help).setOnClickListener {
                dismiss()
            }
        }
        dialog.show()
    }

    override fun onClick(data: String, postion: String) {
        val position = postion.toIntOrNull()

        if (position != null && position in dataSelfAlert.indices) {
            val alert = dataSelfAlert[position]
            val alertId = alert.id?.toString().orEmpty()
            val description = alert.description?.toString().orEmpty()
            val alertType = alert.alert_type?.toString().orEmpty()
            val userName = alert.user_name?.toString().orEmpty()
            val responseDate = BaseApplication.logFormattedTime(alert.updated_at?.toString().orEmpty())

            showAlertDialog(data, alertId, description, responseDate,alertType,userName)
        } else {
            Log.e("AlertError", "Invalid position or out of bounds: $postion")
        }


    }
}
