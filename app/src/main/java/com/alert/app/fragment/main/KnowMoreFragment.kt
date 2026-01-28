package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.base.BaseApplication
import com.alert.app.base.SessionManagement
import com.alert.app.databinding.FragmentKnowmMoreBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.MessageClass
import com.alert.app.viewmodel.tutorialsviewmodel.TutorialsViewModel
import com.alert.app.viewmodel.tutorialsviewmodel.apiresponse.Data
import com.alert.app.viewmodel.tutorialsviewmodel.apiresponse.TutorialsApiModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class KnowMoreFragment : Fragment() {

    private var _binding: FragmentKnowmMoreBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TutorialsViewModel
    private lateinit var sessionManagement: SessionManagement


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKnowmMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManagement = SessionManagement(requireContext())

        viewModel = ViewModelProvider(this)[TutorialsViewModel::class.java]

        if (!sessionManagement.getProfileScreen().toString().equals("signup",true)){
            (requireActivity() as MainActivity).setImageShowTv()?.visibility=View.GONE
            (requireActivity() as MainActivity).setImgChatBoot().visibility =View.GONE
            binding.imgBack.visibility=View.VISIBLE
            binding.btnNext.visibility=View.GONE
        }else{
            binding.imgBack.visibility=View.GONE
            binding.btnNext.visibility=View.VISIBLE
        }


        binding.pullToRefresh.setOnRefreshListener {
            loadTutorials()
        }


        loadTutorials()

        // Handle back navigation
        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.tutorialsFragment2)
        }
    }


    private fun loadTutorials() {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.tutorialsDataRequest { response ->
                    BaseApplication.dismissDialog()
                    binding.pullToRefresh.isRefreshing=false
                    handleApiResponse(response)
                }
            }
        }else{
            binding.pullToRefresh.isRefreshing=false
            showAlert(MessageClass.networkError,false)
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message.toString(), false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String) {
        try {
            Log.d("@@@ Api Response", "message: $data")
            val apiModel = Gson().fromJson(data, TutorialsApiModel::class.java)
            if (apiModel.code == 200 && apiModel.status) {
                apiModel.data?.let { showDataUi(it) }?: run {
                    showAlert(MessageClass.apiError, false)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message.toString(), false)
        }
    }

    private fun showDataUi(data: Data) {
        try {
            data.description.let {
                binding.tvDes.loadData(it.toString(), "text/html", "UTF-8");
            }
        }catch (e:Exception){
            showAlert(e.message.toString(), false)
        }
    }

    private fun handleError(code:Int,msg:String){
        if (code== MessageClass.deactivatedUser || code== MessageClass.deletedUser){
            showAlert(msg, true)
        }else{
            showAlert(msg, false)
        }
    }
    private fun showAlert(message: String, status: Boolean) {
        BaseApplication.alertError(context, message, status)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

}
