package com.alert.app.fragment.main
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.AuthActivity
import com.alert.app.activity.MainActivity
import com.alert.app.base.BaseApplication
import com.alert.app.base.SessionManagement
import com.alert.app.databinding.FragmentSettingBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.viewmodel.tutorialsviewmodel.TutorialsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var sessionManagement: SessionManagement

    private val viewModel: TutorialsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManagement = SessionManagement(requireContext())
        setupUI()
        setupClickListeners()
        handleSystemBackPress()
    }

    private fun setupUI() {
        (requireActivity() as MainActivity).apply {
            setFooter("setting")
            setImageShowTv()?.visibility = View.GONE
            setImgChatBoot().visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            threeLine.setOnClickListener {
                val drawer = (requireActivity() as MainActivity).getDrawerLayout()
                if (drawer.isDrawerVisible(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                } else {
                    drawer.openDrawer(GravityCompat.START)
                }
            }

            layMessage.setOnClickListener { navigateTo(R.id.messageFragment) }
            imgNotification.setOnClickListener { navigateTo(R.id.notificationFragment) }
            tvMyProfile.setOnClickListener { navigateTo(R.id.homeProfileFragment) }
            tvHelp.setOnClickListener { navigateTo(R.id.helpFragment) }
            tvFaq.setOnClickListener { navigateTo(R.id.FAQFragment) }
            tvTermsAmpConditions.setOnClickListener { navigateTo(R.id.termsAndConditionFragment) }
            tvPrivacyPolicy.setOnClickListener { navigateTo(R.id.privacyPolicyFragment) }
            tvSupport.setOnClickListener { navigateTo(R.id.supportFragment) }
            deleteMyAccount.setOnClickListener { showConfirmationDialog(true) }
            tvSignOut.setOnClickListener { showConfirmationDialog(false) }
        }
    }

    private fun handleSystemBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateTo(R.id.homeProfileFragment)
                }
            }
        )
    }

    private fun navigateTo(destination: Int) {
        findNavController().navigate(destination)
    }

    private fun showConfirmationDialog(isDelete: Boolean) {
        val dialog = Dialog(requireContext()).apply {
            setContentView(if (isDelete) R.layout.dialog_delete else R.layout.dialog_logout)
            setCancelable(false)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                attributes = attributes?.apply { copyFrom(attributes) }
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }

        dialog.apply {
            findViewById<TextView>(R.id.tvOK).setOnClickListener {
                dialog.dismiss()
                if (isDelete) {
                    deleteUser()
                } else {
                    userLogout()
                }
            }
            findViewById<TextView>(R.id.tvNo).setOnClickListener { dismiss() }
            findViewById<ImageView>(R.id.img_close).setOnClickListener { dismiss() }
            show()
        }
    }

    private fun navigateToLogin() {
        val intent=Intent(requireContext(), AuthActivity::class.java)
        intent. putExtra("openScreen", "Login")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun userLogout() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.userLogout().collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                if (it.has(getString(R.string.apiCode)) && it.get(getString(R.string.apiCode)).asInt==200) {
                                    sessionManagement.logOut()
                                    navigateToLogin()
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


    private fun deleteUser() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.deleteUser().collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                if (it.has(getString(R.string.apiCode))
                                    && it.get(getString(R.string.apiCode)).asInt==200) {
                                    sessionManagement.logOut()
                                    navigateToLogin()
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
