package com.alert.app.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.alert.app.BuildConfig
import com.alert.app.R
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.ActivityContactDetailScreenBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.contactdetailsactivity.ContactDetailsScreenModel
import com.alert.app.model.contactdetailsactivity.ContactDetailsScreenModelData
import com.alert.app.model.helpingneighbormodel.GetNeighborModel
import com.alert.app.viewmodel.contactdetailsactivity.ContactDetailsScreenViewModel
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
class ContactDetailScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactDetailScreenBinding
    private lateinit var viewModel: ContactDetailsScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactDetailScreenBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ContactDetailsScreenViewModel::class.java]

        // Set listeners
        binding.imgBack.setOnClickListener { finish() }
        binding.tvOK.setOnClickListener { showDialog(DialogType.DEFAULT) }
        binding.tvNo.setOnClickListener { showDialog(DialogType.DEFAULT) }
        binding.imgChat.setOnClickListener { startActivity(Intent(this, ChatActivity::class.java)) }
        binding.imgCall.setOnClickListener {
         /*   startActivity(Intent(this, CallActivity::class.java))*/

            val channelName = "call_${System.currentTimeMillis()}"
            val intent = Intent(this, CallActivity::class.java).apply {
                putExtra("channelName", channelName)
            }
            startActivity(intent)
        }

        setupStatusBarAppearance()

        getContactDetailsScreen()
    }


    private fun getContactDetailsScreen() {
        if (BaseApplication.isOnline(this@ContactDetailScreenActivity)) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.getEmergencyContactProfile("").collect {
                    BaseApplication.dismissDialog()
                    handleApiResponse(it)
                }
            }
        } else {
            AlertUtils.showAlert(this@ContactDetailScreenActivity, MessageClass.networkError, false)
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
        BaseApplication.alertError(this@ContactDetailScreenActivity, message,status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessApiResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, ContactDetailsScreenModel::class.java)
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

    private fun showDataInUI(data: ContactDetailsScreenModelData) {
        try {
            data.let { user ->
                val fullName = listOfNotNull(user.first_name, user.last_name).joinToString(" ")
                if (fullName.isNotBlank()) {
                    binding.textNeighborName.text = fullName
                }

                user.address.let {
                    binding.textAddressDetails.text = it.toString()
                }

                user.phone?.let {
                    binding.textPhoneNumber.text = it
                }

                user.profile_pic.let { profilePic ->
                    Glide.with(this)
                        .load("${BuildConfig.BASE_URL}$profilePic")
                        .error(R.drawable.no_image)
                        .placeholder(R.drawable.no_image)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean
                            ): Boolean {
                                binding.layProgess.root.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean
                            ): Boolean {
                                binding.layProgess.root.visibility = View.GONE
                                return false
                            }
                        })
                        .into(binding.userImg)
                }
            }

        }catch (e:Exception){

        }
    }

    private fun handleError(code: Int?, message: String?) {
        if (code == MessageClass.deactivatedUser || code == MessageClass.deletedUser) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDialog(type: DialogType) {
        val dialog = Dialog(this).apply {
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        val layoutRes = when (type) {
            DialogType.DEFAULT -> R.layout.dialog_default
            DialogType.SUCCESS -> R.layout.dialog_success
            DialogType.EDIT -> R.layout.dialog_default_edit
        }

        dialog.setContentView(layoutRes)

        val tvSend = dialog.findViewById<TextView>(R.id.tvSend)
        val tvEdit = dialog.findViewById<TextView>(R.id.tvEdit)
        val tvSave = dialog.findViewById<TextView>(R.id.tvSave)
        val tvOK = dialog.findViewById<TextView>(R.id.tvOK)
        val imgClose = dialog.findViewById<ImageView>(R.id.img_close)

        // Set dynamic content for success dialog
        if (type == DialogType.SUCCESS) {
            dialog.findViewById<TextView>(R.id.tv_heading)?.text = "Success!"
            dialog.findViewById<TextView>(R.id.tv_text)?.text = "Message sent successfully."
        }

        imgClose.setOnClickListener { dialog.dismiss() }

        // Handle actions based on dialog type
        when (type) {
            DialogType.DEFAULT -> {
                tvSend?.setOnClickListener {
                    dialog.dismiss()
                    showDialog(DialogType.SUCCESS)
                }
                tvEdit?.setOnClickListener {
                    dialog.dismiss()
                    showDialog(DialogType.EDIT)
                }
            }
            DialogType.SUCCESS -> tvOK?.setOnClickListener { dialog.dismiss() }
            DialogType.EDIT -> tvSave?.setOnClickListener { dialog.dismiss() }


        }

        dialog.show()
    }

    // Enum to define dialog types
    private enum class DialogType {
        DEFAULT, SUCCESS, EDIT
    }


    override fun onDestroy() {
        super.onDestroy()

        resetStatusBarAppearance()

    }

    private fun setupStatusBarAppearance() {
        // Change status bar color and appearance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun resetStatusBarAppearance() {
        // Reset status bar color and appearance to default
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    0, // Clear all appearance flags
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }
    }


}

