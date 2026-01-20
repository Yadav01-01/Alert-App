/*
package com.yesitlabs.alertapp.fragment.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.yesitlabs.alertapp.R
import com.yesitlabs.alertapp.activity.AuthActivity
import com.yesitlabs.alertapp.activity.CallActivity
import com.yesitlabs.alertapp.activity.ChatActivity
import com.yesitlabs.alertapp.databinding.FragmentHomeBinding
import com.yesitlabs.alertapp.databinding.FragmentNeighborProfileBinding


class NeighborProfileFragment : Fragment() {


    private lateinit var binding: FragmentNeighborProfileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNeighborProfileBinding.inflate(layoutInflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.btnSetAlert.setOnClickListener {
            val bundle=Bundle()
            bundle.putString("type","Set Alert")
            findNavController().navigate(R.id.sendAlertFragment,bundle)
        }

        binding.btnSendAlert.setOnClickListener {
            val bundle=Bundle()
            bundle.putString("type","Send Alert")
            findNavController().navigate(R.id.sendAlertFragment,bundle)
        }

        binding.imgChat.setOnClickListener {
            val intent = Intent(requireContext(), ChatActivity::class.java)
            startActivity(intent)
        }

        binding.imgCall.setOnClickListener {
            val intent = Intent(requireContext(), CallActivity::class.java)
            startActivity(intent)
        }


        binding.imgThree.setOnClickListener {
            val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = inflater?.inflate(R.layout.item_layout, null)

            // Access views inside the inflated layout using findViewById
            val tvBlock = popupView?.findViewById<TextView>(R.id.tv_block)
            val tvRemove = popupView?.findViewById<TextView>(R.id.tv_remove)


            val popupWindow = PopupWindow(popupView, 600, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAsDropDown(binding.imgThree,  0, 0, Gravity.END)


            tvBlock?.setOnClickListener {
                popupWindow.dismiss()
                alertBoxBlockAndDelete("block")

            }

            tvRemove?.setOnClickListener {
                popupWindow.dismiss()
                alertBoxBlockAndDelete("delete")
            }

        }

    }


    @SuppressLint("SetTextI18n")
    private fun alertBoxBlockAndDelete(type:String){

        val dialog = Dialog(requireContext())

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
            tvText.text="Are you sure you want to Delete the \nuser!"
            tvOK.text="Delete"
            tvNo.text="Cancel"
        }

        dialog.show()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)


        tvOK.setOnClickListener {
            dialog.dismiss()
        }

        tvNo.setOnClickListener {
            dialog.dismiss()
        }

        imgClose.setOnClickListener {
            dialog.dismiss()
        }


    }


}*/
package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.BuildConfig
import com.alert.app.R
import com.alert.app.activity.CallActivity
import com.alert.app.activity.ChatActivity
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentNeighborProfileBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.neighborprofile.NeighborBlockProfile
import com.alert.app.model.neighborprofile.NeighborDeleteProfile
import com.alert.app.model.neighborprofile.NeighborProfileModel
import com.alert.app.model.neighborprofile.NeighborProfileModelData
import com.alert.app.viewmodel.neighborprofile.NeighborProfileViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NeighborProfileFragment : Fragment() {

    private lateinit var binding: FragmentNeighborProfileBinding
    private var contactId:String=""
    private lateinit var viewModel: NeighborProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNeighborProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactId= arguments?.getString("contactId").toString()

        viewModel = ViewModelProvider(this)[NeighborProfileViewModel::class.java]

        setupStatusBarAppearance()
        setupClickListeners()
        getNeighborProfile()

    }

    private fun getNeighborProfile() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.neighborProfileDetails(contactId).collect {
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
            val apiModel = Gson().fromJson(data, NeighborProfileModel::class.java)
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

    @SuppressLint("SetTextI18n")
    private fun showDataInUI(data: NeighborProfileModelData) {
        try {
            data.let {
                data.first_name
                    ?.takeIf { it.isNotBlank() }?.let { first -> val last = data.last_name.orEmpty()
                        binding.textNeighborName.text = "$first $last".trim()
                    }

                data.address?.let { binding.textAddressDetails.text = it }

                data.phone?.let { binding.textPhoneNumber.text = it }

                data.email?.let { binding.textEmailAddress.text = it }

                data.profile_pic?.let { picPath ->
                        Glide.with(requireActivity())
                            .load(BuildConfig.BASE_URL + picPath)
                            .placeholder(R.drawable.no_image)
                            .error(R.drawable.no_image)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    binding.layProgess.root.visibility = View.GONE
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    binding.layProgess.root.visibility = View.GONE
                                    return false
                                }
                            })
                            .into(binding.userImg)
                    } ?: run {
                    // Optionally, hide progress or set default state here
                    binding.layProgess.root.visibility = View.GONE
                    binding.userImg.setImageResource(R.drawable.no_image)
                }

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


    private fun setupClickListeners() {
        binding.imgBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnSetAlert.setOnClickListener { navigateToSendAlert("Set Alert") }
        binding.btnSendAlert.setOnClickListener { navigateToSendAlert("Send Alert") }

        binding.imgChat.setOnClickListener {
            startActivity(Intent(requireContext(), ChatActivity::class.java))
        }

        binding.imgCall.setOnClickListener {
            val channelName = "call_${System.currentTimeMillis()}"
            val intent = Intent(requireContext(), CallActivity::class.java).apply {
                putExtra("channelName", channelName)
            }
            startActivity(intent)
            /*startActivity(Intent(requireContext(), CallActivity::class.java))*/
        }

        binding.imgThree.setOnClickListener { showPopupMenu() }
    }

    private fun navigateToSendAlert(type: String) {
        val bundle = Bundle().apply { putString("type", type) }
        findNavController().navigate(R.id.sendAlertFragment, bundle)
    }

    private fun showPopupMenu() {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.item_layout, null)
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        popupView.findViewById<View>(R.id.tv_block)?.setOnClickListener {
            popupWindow.dismiss()
            showAlertDialog("block")
        }

        popupView.findViewById<View>(R.id.tv_remove)?.setOnClickListener {
            popupWindow.dismiss()
            showAlertDialog("delete")
        }

        popupWindow.showAsDropDown(binding.imgThree, 0, 0, Gravity.END)
    }

    @SuppressLint("SetTextI18n")
    private fun showAlertDialog(actionType: String) {
        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_delete)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.attributes = window?.attributes?.apply {
                softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            }
        }

        with(dialog) {
            val isBlockAction = actionType.equals("block", ignoreCase = true)
            val img = findViewById<ImageView>(R.id.img)
            val tvHeading = findViewById<TextView>(R.id.tv_heading)
            val tvText = findViewById<TextView>(R.id.tv_text)
            val tvOK = findViewById<TextView>(R.id.tvOK)
            val tvNo = findViewById<TextView>(R.id.tvNo)

            img?.setImageResource(if (isBlockAction) R.drawable.block_icon else R.drawable.deletc_block_icon)
            tvHeading?.text = if (isBlockAction) "Block" else "Delete"
            tvText?.text = "Are you sure you want to ${if (isBlockAction) "Block" else "Delete"} the user!"
            tvOK?.text = if (isBlockAction) "Block" else "Delete"
            tvNo?.text = "Cancel"

            tvOK?.setOnClickListener {
                if (isBlockAction) {
                    // Perform block action
                    /*blockUser()*/
                    blockNeighborProfile()
                } else {
                    // Perform delete action
                    deleteNeighborProfile()
                }

                dismiss()
            }
            tvNo?.setOnClickListener { dismiss() }
            findViewById<ImageView>(R.id.img_close)?.setOnClickListener { dismiss() }
        }

        dialog.show()
    }

    private fun blockNeighborProfile() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.neighborProfileBlock(contactId).collect {
                    BaseApplication.dismissDialog()
                    handleBlockApiResponse(it)
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun deleteNeighborProfile() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.neighborProfileDelete(contactId).collect {
                    BaseApplication.dismissDialog()
                    handleDeleteApiResponse(it)
                }
            }
        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }

    private fun handleBlockApiResponse(it: NetworkResult<JsonObject>) {
        when (it) {
            is NetworkResult.Success -> handleSuccessBlockApiResponse(it.data.toString())
            is NetworkResult.Error -> showAlert(it.message, false)
            else -> showAlert(it.message, false)
        }
    }

    private fun handleDeleteApiResponse(it: NetworkResult<JsonObject>) {
        when (it) {
            is NetworkResult.Success -> handleSuccessDeleteApiResponse(it.data.toString())
            is NetworkResult.Error -> showAlert(it.message, false)
            else -> showAlert(it.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessBlockApiResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, NeighborBlockProfile::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.status) {
                findNavController().navigateUp()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessDeleteApiResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, NeighborDeleteProfile::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.status) {
                findNavController().navigateUp()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()

        resetStatusBarAppearance()

    }

    private fun setupStatusBarAppearance() {
        // Change status bar color and appearance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun resetStatusBarAppearance() {
        // Reset status bar color and appearance to default
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().window.insetsController?.setSystemBarsAppearance(
                    0, // Clear all appearance flags
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                requireActivity().window.decorView.systemUiVisibility =
                    requireActivity().window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }
    }
}
