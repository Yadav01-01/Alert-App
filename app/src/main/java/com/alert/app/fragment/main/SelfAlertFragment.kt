package com.alert.app.fragment.main


import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity

import com.alert.app.adapter.SelfAlertAdapter
import com.alert.app.base.BaseApplication

import com.alert.app.databinding.FragmentSelfAlertBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.SelfAlertClick
import com.alert.app.model.selfAlert.SelfAlert
import com.alert.app.model.selfAlert.SelfAlertsResponse
import com.alert.app.viewmodel.selfalertviewmodel.SelfAlertViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelfAlertFragment : Fragment(), SelfAlertClick {

    private lateinit var binding: FragmentSelfAlertBinding
    private val viewModel: SelfAlertViewModel by viewModels()
    private var selfAlert: MutableList<SelfAlert> = mutableListOf()
    private lateinit var adapter: SelfAlertAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSelfAlertBinding.inflate(layoutInflater, container, false)
        initView()
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setFooter("selfAlert")
        (requireActivity() as MainActivity).setImageShowTv()?.visibility=View.GONE
        (requireActivity() as MainActivity).setImgChatBoot().visibility =View.GONE

    }
    private fun setupRecyclerView() {
        adapter = SelfAlertAdapter(requireContext(), selfAlert,this)
        binding.rcyData.adapter = adapter
    }
    private fun initView() {
        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.showData.visibility=View.GONE
        binding.layHide.visibility=View.VISIBLE
        binding.tvTitle.visibility=View.GONE

       /* Handler().postDelayed({
            binding.tvTitle.visibility=View.VISIBLE
            binding.showData.visibility=View.VISIBLE
            binding.layHide.visibility=View.GONE
        }, 3000)*/


        binding.btnAlert.setOnClickListener {
            findNavController().navigate(R.id.addAlertFragment)
        }

        binding.btnAddAlert.setOnClickListener {
            findNavController().navigate(R.id.addAlertFragment)
        }
        getSelfAlerts()
    }

    private fun getSelfAlerts() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getSelfAlerts("self").collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                val selfAlertsResponse = Gson().fromJson(it, SelfAlertsResponse::class.java)
                                if (selfAlertsResponse.code==200) {
                                    selfAlert = selfAlertsResponse.data!!.toMutableList()
                                    if (selfAlert.size>0) {
                                        binding.tvTitle.visibility=View.VISIBLE
                                        binding.showData.visibility=View.VISIBLE
                                        binding.layHide.visibility=View.GONE
                                        adapter.update(selfAlert)
                                    }else{
                                        binding.showData.visibility=View.GONE
                                        binding.layHide.visibility=View.VISIBLE
                                        binding.tvTitle.visibility=View.GONE
                                    }
                                }else{
                                    binding.showData.visibility=View.GONE
                                    binding.layHide.visibility=View.VISIBLE
                                    binding.tvTitle.visibility=View.GONE
                                    Toast.makeText(
                                        requireContext(),
                                        selfAlertsResponse.message,
                                        Toast.LENGTH_LONG
                                    ).show()
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

    override fun onClick(type: String, selfAlert: SelfAlert, pos: Int) {
        alertBoxBlockAndDelete(type,selfAlert)
    }

    @SuppressLint("SetTextI18n")
    private fun alertBoxBlockAndDelete(type: String, selfAlert: SelfAlert){

        val dialog = Dialog(requireActivity())

        dialog.setContentView(R.layout.dialog_delete)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)

        dialog.window!!.attributes = layoutParams
        val tvOK = dialog.findViewById<TextView>(R.id.tvOK)
        val tvNo = dialog.findViewById<TextView>(R.id.tvNo)
        val tvHeading = dialog.findViewById<TextView>(R.id.tv_heading)
        val tvText = dialog.findViewById<TextView>(R.id.tv_text)
        val img = dialog.findViewById<ImageView>(R.id.img)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)

        if (type.equals("block",true)){
            img.setImageResource(R.drawable.block_icon)
            tvHeading.text="Block"
            tvText.text="Are you sure you want to Block the \nuser!"
            tvOK.text="Block"
            tvNo.text="Cancel"
        }else{
            img.setImageResource(R.drawable.deletc_block_icon)
            tvHeading.text="Delete"
            tvText.text="Are you sure you want to Delete the \nAlert!"
            tvOK.text="Delete"
            tvNo.text="Cancel"
        }

        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)


        tvOK.setOnClickListener {
            if (type.equals("Delete",true)){
                deleteUserAlert(type,selfAlert,dialog)
            }else{
                dialog.dismiss()
            }
        }

        tvNo.setOnClickListener {
            dialog.dismiss()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }


    }

    private fun deleteUserAlert(type: String, self: SelfAlert, dialog: Dialog) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.deleteUserAlert(self.id.toString(),"self").collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                if (it.has(getString(R.string.apiCode)) && it.get(getString(R.string.apiCode)).asInt==200) {
                                    selfAlert.remove(self)
                                    adapter.update(selfAlert)
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

}