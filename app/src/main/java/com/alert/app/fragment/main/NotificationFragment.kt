package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.app.Dialog
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.AlertListAdapter
import com.alert.app.adapter.MessageNotificationAdapter
import com.alert.app.adapter.NotificationAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.DialogNotificationBinding
import com.alert.app.databinding.FragmentNotificationBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.OnNotificationClickListener
import com.alert.app.model.notification.AlertModel
import com.alert.app.viewmodel.notifications.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.alert.app.adapter.SwipeAdapter
import com.alert.app.base.SessionManagement
import com.alert.app.viewmodel.ChatScreenViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class NotificationFragment : Fragment(), OnNotificationClickListener {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var swipeAdapter: SwipeAdapter
    private val chattingViewModel: ChatScreenViewModel by viewModels()
    private lateinit var notificationAdapter : NotificationAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        chattingViewModel.currentUserId = SessionManagement(requireContext()).getUserId().toString()
        setupRecyclerView()
        observeChatList()
        callingChatList()
        getNotificationData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).apply {
            setImageShowTv()?.visibility = View.GONE
            setImgChatBoot().visibility = View.GONE
            setFooter("notification")
        }

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupTabs()

        toggleTabs(true)

        observeNotifications("alert")

    }

    private fun getNotificationData(){

        lifecycleScope.launch {
            BaseApplication.openDialog()

            notificationViewModel.getInAppNotification().collect {
                when(it){
                    is NetworkResult.Success ->{
                      BaseApplication.dismissDialog()
                        it.data?.let {
                            notificationAdapter.update(it)
                        }
                    }
                    is NetworkResult.Error ->{
                      BaseApplication.dismissDialog()
                    }
                    else ->{
                      BaseApplication.dismissDialog()
                    }

                }
            }
        }

    }


    private fun setupTabs() {
        binding.layAlerts.setOnClickListener {
            toggleTabs(true)
          //  observeNotifications("alert")
            binding.rcyData.visibility =View.GONE
            binding.rcyDataAlert.visibility =View.VISIBLE
        }

        binding.layMessages.setOnClickListener {
            toggleTabs(false)

            binding.rcyData.visibility =View.VISIBLE
            binding.rcyDataAlert.visibility =View.GONE
            observeNotifications("message")

        }
    }


    private fun toggleTabs(showAlerts: Boolean) {
        with(binding) {
            view1.visibility = if (showAlerts) View.VISIBLE else View.GONE
            view2.visibility = if (showAlerts) View.GONE else View.VISIBLE
            alerts.setTextColor(if (showAlerts) Color.BLACK else Color.GRAY)
            messages.setTextColor(if (showAlerts) Color.GRAY else Color.BLACK)
        }
    }

    private fun showNotificationDialog(alert: AlertModel) {
        val dialog = Dialog(requireContext())
        val binding = DialogNotificationBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setCancelable(true)

        binding.tvView.setOnClickListener {
            dialog.dismiss()
            showAlertViewDialog()
        }

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.imgClose.setOnClickListener {
            dialog.dismiss()
        }

        if (alert.image.isNotEmpty()) {
            Glide.with(requireContext())
                .load(alert.image)
                .placeholder(R.drawable.dummy_image)
                .into(binding.userImg)
        }
        if (alert.name.isNotEmpty()){
            binding.textUserName.text = alert.name
        }
        if (alert.alert.isNotEmpty()){
            binding.tvAlertName.text = alert.alert
        }
        if (alert.relation.isNotEmpty()){
            binding.tvRelationType.text = alert.relation
        }
        if (alert.time.isNotEmpty()){
            binding.tvTime.text = alert.time
        }
        if (alert.description.isNotEmpty()){
            binding.tvDescription.text = alert.description
        }

        dialog.show()
    }


    @SuppressLint("SetTextI18n")
    private fun showAlertViewDialog() {
        val dialog = createDialog(R.layout.dialog_alert)
        dialog.findViewById<TextView>(R.id.tv_name).text = "Alerts for Alex Flinders"
        dialog.findViewById<RecyclerView>(R.id.data_rcy).adapter = AlertListAdapter(requireContext())
        dialog.findViewById<TextView>(R.id.tvokay).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<ImageView>(R.id.img_close).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun observeNotifications(type: String) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()

            lifecycleScope.launchWhenStarted {
                notificationViewModel.loadNotifications(type).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            val list = result.data
                            BaseApplication.dismissDialog()

                            if (list.isNullOrEmpty()) {
                                binding.rcyData.visibility = View.GONE
                            } else {
                                binding.rcyData.visibility = View.VISIBLE

                                if (type == "alert") {
//                                    binding.rcyData.adapter = NotificationAdapter(
//                                        list,
//                                        this@NotificationFragment
//                                    )
                                } else if (type == "message") {
                                    binding.rcyData.adapter = MessageNotificationAdapter(list)
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            BaseApplication.dismissDialog()
                            binding.rcyData.visibility = View.GONE
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        } else {
            AlertUtils.showAlert(requireContext(), MessageClass.networkError, false)
        }
    }


    private fun setupRecyclerView() {

        swipeAdapter = SwipeAdapter(requireContext())

        binding.rcyData.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = swipeAdapter
        }

        binding.rcyDataAlert.apply {
            layoutManager = LinearLayoutManager(requireContext())
            notificationAdapter =  NotificationAdapter(mutableListOf(),
                this@NotificationFragment)

        }



//        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)

  //      binding.rcyData.addItemDecoration(divider)
    }

    private fun observeChatList() {
        chattingViewModel.chatList.observe(viewLifecycleOwner) { list ->
            swipeAdapter.submitList(list)
            swipeAdapter.setData(list)
        }
    }

    private fun callingChatList() {
        viewLifecycleOwner.lifecycleScope.launch {
            BaseApplication.openDialog()
            chattingViewModel.getChatList().collect {
                result -> when (result) {
                    is NetworkResult.Success -> {
                        BaseApplication.dismissDialog()
                        val apiList = result.data ?: emptyList()
                        if (apiList.isNotEmpty()) {
                            chattingViewModel.loadChatReadList(
                                usersFromApi = apiList,
                                myUserId = SessionManagement(requireContext()).getUserId().toString()
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        BaseApplication.dismissDialog()
                    }
                    else -> { /* Handle Loading if needed */ }
                }
            }
        }
    }

    private fun createDialog(layoutRes: Int): Dialog {
        return Dialog(requireContext()).apply {
            setContentView(layoutRes)
            setCancelable(false)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                attributes = attributes?.apply {
                    width = WindowManager.LayoutParams.MATCH_PARENT
                    height = WindowManager.LayoutParams.WRAP_CONTENT
                }
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }
    }

    override fun onClick(alert: AlertModel) {
        showNotificationDialog(alert)
    }

}
