package com.alert.app.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.UserContactListAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentUserContactListBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.OnClickContact
import com.alert.app.model.contact.Contact
import com.alert.app.model.contact.ContactListResponse
import com.alert.app.viewmodel.contactsviewmodel.ContactsViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserContactListFragment : Fragment(), OnClickContact {

    private lateinit var binding: FragmentUserContactListBinding
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var adapter: UserContactListAdapter
    private val contactList: MutableList<Contact> = mutableListOf()

    private var type: String = ""
    private var selectedTimeMinutes: String? = null
    private var alertFor: String? = null
    private var selectedAlertType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserContactListBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        getArgumentsData()
        setupRecyclerView()
//        loadDummyData()
         getContactList()
    }

    private fun setupUI() {
        (requireActivity() as MainActivity).apply {
            setImageShowTv()?.visibility = View.GONE
            setImgChatBoot().visibility = View.GONE
        }

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )
    }

    private fun getArgumentsData() {
        type = arguments?.getString("type").orEmpty()
        alertFor = arguments?.getString("alertFor")
        selectedTimeMinutes = arguments?.getString("selected_time")
        selectedAlertType = arguments?.getString("AlertType")
    }

    private fun setupRecyclerView() {
        adapter = UserContactListAdapter(requireContext(), this, contactList, type)

        binding.rcyData.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = this@UserContactListFragment.adapter
        }
    }


    private fun getContactList() {
        if (!BaseApplication.isOnline(requireContext())) {
            showAlert(requireContext(), MessageClass.networkError, false)
            return
        }

        BaseApplication.openDialog()
        lifecycleScope.launch {
            viewModel.getContactList().collect { result ->
                BaseApplication.dismissDialog()
                when (result) {
                    is NetworkResult.Success -> {
                        val response =
                            Gson().fromJson(result.data, ContactListResponse::class.java)
                        handleContactListResponse(response)
                    }
                    is NetworkResult.Error -> {
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun handleContactListResponse(response: ContactListResponse) {
        if (response.code == 200) {
            contactList.clear()
            contactList.addAll(response.data)
            adapter.notifyDataSetChanged()

            binding.rcyData.visibility = View.VISIBLE
//            binding.tvNoData.visibility = View.GONE
        } else {
            binding.rcyData.visibility = View.GONE
//            binding.tvNoData.visibility = View.VISIBLE
            Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onClick(data: String, id: String) {
        val bundle = Bundle().apply {
            putString("type", type) // agar yeh variable already hai
            putString("alertFor", alertFor)
            putString("selected_time", selectedTimeMinutes)
            putString("AlertType", selectedAlertType)
            putString("CONTACT_ID", id)
        }
        findNavController().navigate(R.id.healthAlertCalanderFragment,bundle)
    }
}
