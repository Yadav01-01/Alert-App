package com.alert.app.fragment.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.SwipeAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.base.SessionManagement
import com.alert.app.databinding.FragmentMessageBinding
import com.alert.app.di.NetworkResult
import com.alert.app.model.message.ChatListItem
import com.alert.app.viewmodel.ChatScreenViewModel
import com.alert.app.viewmodel.chatbot.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MessageFragment : Fragment() {

        private var _binding: FragmentMessageBinding? = null
        private val binding get() = _binding!!

        private lateinit var swipeAdapter: SwipeAdapter
        private val chattingViewModel: ChatScreenViewModel by viewModels()
        private val mainActivity: MainActivity?
            get() = activity as? MainActivity

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentMessageBinding.inflate(inflater, container, false)

            // UserId set karna
            chattingViewModel.currentUserId = SessionManagement(requireContext()).getUserId().toString()

            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            setupUI()
            setupRecyclerView()
            observeChatList()
            callingChatList()
        }

        private fun setupUI() {
            mainActivity?.setImageShowTv()?.visibility = View.GONE
            mainActivity?.setImgChatBoot()?.visibility = View.GONE

            binding.imgNotification.setOnClickListener {
                findNavController().navigate(R.id.notificationFragment)
            }

            binding.threeLine.setOnClickListener {
                mainActivity?.getDrawerLayout()?.let { drawer ->
                    if (drawer.isDrawerVisible(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START)
                    } else {
                        drawer.openDrawer(GravityCompat.START)
                    }
                }
            }
        }

        private fun setupRecyclerView() {
            swipeAdapter = SwipeAdapter(requireContext())
            binding.rcyData.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = swipeAdapter
            }
        }

        private fun observeChatList() {
            chattingViewModel.chatList.observe(viewLifecycleOwner) { list ->
                // Yahan type mismatch nahi hoga agar imports same hain
                swipeAdapter.submitList(list)
            }
        }

        private fun callingChatList() {
            viewLifecycleOwner.lifecycleScope.launch {
                BaseApplication.openDialog()
                chattingViewModel.getChatList().collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            BaseApplication.dismissDialog()
                            val apiList = result.data ?: emptyList()
                            if (apiList.isNotEmpty()) {
                                chattingViewModel.loadChatList(
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

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null // Memory leak se bachne ke liye
        }



}
