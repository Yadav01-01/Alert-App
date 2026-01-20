/*
package com.yesitlabs.alertapp.fragment.main

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.yesitlabs.alertapp.R
import com.yesitlabs.alertapp.activity.MainActivity
import com.yesitlabs.alertapp.adapter.TimeArrayAdapter
import com.yesitlabs.alertapp.adapter.TimeArrayCustomListAdapter
import com.yesitlabs.alertapp.databinding.FragmentHomeBinding
import com.yesitlabs.alertapp.databinding.FragmentSetHealthBinding
import com.yesitlabs.alertapp.listener.OnClickEventDropDownType
import com.yesitlabs.alertapp.model.TimeModel


class SetHealthFragment : Fragment(), OnClickEventDropDownType {


    private lateinit var binding: FragmentSetHealthBinding

    val data: MutableList<TimeModel> = mutableListOf()

    lateinit var popupWindow: PopupWindow


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSetHealthBinding.inflate(layoutInflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setImageShowTv()?.visibility = View.GONE
        (requireActivity() as MainActivity).setImgChatBoot().visibility = View.GONE


        // This line use for system back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })




        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.healthAlertTypeFragment)
        }

        data.clear()
        data.add(TimeModel("For Your Self",false))
        data.add(TimeModel("For Friends & Family",false))

        binding.tvMinit.setOnClickListener {
            val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = inflater?.inflate(R.layout.item_select_layout, null)
            popupWindow = PopupWindow(popupView, binding.tvMinit.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAsDropDown(binding.tvMinit,  0, 0, Gravity.CENTER)

            // Access views inside the inflated layout using findViewById
            val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)

            rcyData?.adapter= TimeArrayCustomListAdapter(requireContext(),data,this,"time")

            binding.tvMinit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.left_arrow_top, 0)
            // Set the dismiss listener
            popupWindow.setOnDismissListener {
                binding.tvMinit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_bottom, 0)
            }



        }

    }

    override fun onClickDropDown(pos: String?, type: String?) {
        if (type.equals("time")){
            for (i in data.indices) {
                val item = data[i].copy() // Create a copy to avoid modifying the reference at `position`
                item.name = data[i].name ?: ""
                // Set the status based on position
                item.status = i == pos?.toInt()
                // Update the item in the list
                data[i] = item
            }
            // Set the text of the category at 'position'
            binding.tvMinit.text = data[pos?.toInt()!!].name
        }
        popupWindow.dismiss()
    }


}*/
package com.alert.app.fragment.main

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.TimeArrayCustomListAdapter
import com.alert.app.databinding.FragmentSetHealthBinding
import com.alert.app.listener.OnClickEventDropDownType
import com.alert.app.model.TimeModel

class SetHealthFragment : Fragment(), OnClickEventDropDownType {

    private lateinit var binding: FragmentSetHealthBinding
    private lateinit var popupWindow: PopupWindow
    private val data: MutableList<TimeModel> = mutableListOf()
    private var alertFor: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupBackNavigation()
        setupData()
        setupListeners()
    }

    private fun setupUI() {
        (requireActivity() as? MainActivity)?.apply {
            setImageShowTv()?.visibility = View.GONE
            setImgChatBoot().visibility = View.GONE
        }
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )
    }

    private fun setupData() {
        data.apply {
            clear()
            add(TimeModel("For Your Self", false))
            add(TimeModel("For Friends & Family", false))
        }
    }

    private fun setupListeners() {
        binding.apply {
            imgBack.setOnClickListener { findNavController().navigateUp() }
            binding.btnNext.setOnClickListener {
                if (alertFor == null) {
                    Toast.makeText(requireContext(), "Please select option", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val bundle = Bundle().apply {
                    putString("alertFor", alertFor)
                }

                findNavController().navigate(R.id.healthAlertTypeFragment,bundle)
            }


            tvMinit.setOnClickListener { showDropDownMenu() }
        }
    }

    private fun showDropDownMenu() {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val popupView = inflater?.inflate(R.layout.item_select_layout, null)
        popupView?.let { view ->
            popupWindow = PopupWindow(
                view, binding.tvMinit.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true
            )
            popupWindow.showAsDropDown(binding.tvMinit, 0, 0, Gravity.CENTER)

            val rcyData = view.findViewById<RecyclerView>(R.id.rcy_data)
            rcyData?.adapter = TimeArrayCustomListAdapter(requireContext(), data, this, "time")

            binding.tvMinit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.left_arrow_top, 0)

            popupWindow.setOnDismissListener {
                binding.tvMinit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_bottom, 0)
            }
        }
    }

    override fun onClickDropDown(pos: String?, type: String?) {
        if (type == "time" && pos != null) {

            val selectedIndex = pos.toInt()

            data.forEachIndexed { index, item ->
                data[index] = item.copy(status = index == selectedIndex)
            }


            alertFor = when (data[selectedIndex].name) {
                "For Your Self" -> "self"
                else -> "other"
            }

            binding.tvMinit.text = data[selectedIndex].name
        }

        popupWindow.dismiss()
    }



}
