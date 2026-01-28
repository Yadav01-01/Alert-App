package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.OtherHealthListAdapter
import com.alert.app.adapter.SelfHealthListAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentHelthBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.selfAlert.SelfAlert
import com.alert.app.model.selfAlert.SelfAlertsResponse
import com.alert.app.viewmodel.healthviewmodel.HealthAlertViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

@AndroidEntryPoint
class HealthFragment : Fragment() {

    private lateinit var binding: FragmentHelthBinding
    private lateinit var viewModel: HealthAlertViewModel
    private lateinit var adapterSelfHealthList: SelfHealthListAdapter
    private lateinit var adapterOtherHealthList: OtherHealthListAdapter

    private val selfAlertList: MutableList<SelfAlert> = mutableListOf()
    private val otherAlertList: MutableList<SelfAlert> = mutableListOf()

    private val dataSelfAlert:MutableList<SelfAlert> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHelthBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HealthAlertViewModel::class.java]

        initView()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {

        (requireActivity() as MainActivity).setImageShowTv()?.visibility = View.GONE
        (requireActivity() as MainActivity).setImgChatBoot().visibility = View.GONE

        adapterSelfHealthList = SelfHealthListAdapter(requireContext(), dataSelfAlert)
        adapterOtherHealthList = OtherHealthListAdapter(requireContext(), dataSelfAlert)

       /* adapterSelfHealthList = SelfHealthListAdapter(requireContext(), selfAlertList)

 adapterOtherHealthList = OtherHealthListAdapter(requireContext(), otherAlertList)*/
     //   adapterOtherHealthList = OtherHealthListAdapter(requireContext(), mutableListOf())


//        binding.rcyData.adapter = adapterSelfHealthList  // default

        getSelfAlerts("self")

//        initializeAdapterAlerts()

        // This line use for system back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })


        binding.laySelf.setOnClickListener {
            binding.view1.visibility = View.VISIBLE
            binding.view2.visibility = View.GONE
            binding.self.setTextColor("#0B0202".toColorInt())
            binding.other.setTextColor("#777777".toColorInt())
            binding.rcyData.adapter = adapterSelfHealthList
            getSelfAlerts("self")
        }

        binding.layOther.setOnClickListener {
            binding.view1.visibility = View.GONE
            binding.view2.visibility = View.VISIBLE
            binding.self.setTextColor("#777777".toColorInt())
            binding.other.setTextColor("#0B0202".toColorInt())

            otherAlertList.clear()
            adapterOtherHealthList.notifyDataSetChanged()

            binding.rcyData.adapter = adapterOtherHealthList
            getSelfAlerts("health")
        }


        binding.btnSetAlert.setOnClickListener {
            findNavController().navigate(R.id.setHealthFragment)
        }

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }


    private fun getSelfAlerts(type:String) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getSelfAlerts(type).collect {
                    BaseApplication.dismissDialog()
                    handleApiResponse(it,type)
                }
            }
        } else {
            showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun handleApiResponse(it: NetworkResult<JsonObject>,type:String) {
        when (it) {
            is NetworkResult.Success -> handleSuccessApiResponse(it.data.toString(),type)
            is NetworkResult.Error -> showAlert(it.message, false)
            else -> showAlert(it.message, false)
        }
    }

    // This is common function for show the alert box
    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(context, message,status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessApiResponse(data: String,type:String) {
        try {
            val apiModel = Gson().fromJson(data, SelfAlertsResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.status) {
                if (apiModel.data != null) {
                    showDataInUI(apiModel.data,type)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

 /*   private fun showDataInUI(data: MutableList<SelfAlert>,type:String) {
        dataSelfAlert.clear()
        try {
            data.let {
                dataSelfAlert.addAll(it)
            }

            if (dataSelfAlert.size>0){
                binding.rcyData.visibility=View.VISIBLE
                if (type.equals("self",true)) {
                    adapterSelfHealthList.update(dataSelfAlert)
                }else{
                    adapterOtherHealthList.update(dataSelfAlert)
                }
            }else{
                binding.rcyData.visibility=View.GONE
            }

        }catch (e:Exception){
            showAlert(e.message, false)

        }

    }*/

    private fun showDataInUI(data: MutableList<SelfAlert>, type: String) {
        try {
            if (type.equals("self", true)) {
                selfAlertList.clear()
                selfAlertList.addAll(data)
                adapterSelfHealthList.update(selfAlertList)
                binding.rcyData.adapter = adapterSelfHealthList
            } else {
                otherAlertList.clear()
                otherAlertList.addAll(data)
                adapterOtherHealthList.refresh()
             //   adapterOtherHealthList.update(data)
                binding.rcyData.adapter = adapterOtherHealthList
            }

            binding.rcyData.visibility =
                if (data.isNotEmpty()) View.VISIBLE else View.GONE

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



}