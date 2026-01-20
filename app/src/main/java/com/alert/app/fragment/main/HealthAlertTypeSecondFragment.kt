package com.alert.app.fragment.main

import android.annotation.SuppressLint
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
import com.alert.app.R
import com.alert.app.activity.MainActivity

import com.alert.app.adapter.TimeArrayCustomListAdapter
import com.alert.app.databinding.FragmentHealthAlertTypeBinding

import com.alert.app.listener.OnClickEventDropDownType
import com.alert.app.model.TimeModel


class HealthAlertTypeSecondFragment : Fragment() , OnClickEventDropDownType {

    private lateinit var binding: FragmentHealthAlertTypeBinding

    val data: MutableList<TimeModel> = mutableListOf()
    private var selectedTimeMinutes: String? = null
    private var alertFor: String? = null
    private var selectedAlertType: String? = null
    lateinit var popupWindow:PopupWindow


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHealthAlertTypeBinding.inflate(layoutInflater, container, false)

        alertFor = arguments?.getString("alertFor")
        selectedTimeMinutes = arguments?.getString("selected_time")
        return binding.root
    }


    @SuppressLint("SetTextI18n")
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


        binding.img.setImageResource(R.drawable.amico_icon)


        binding.btnNext.setOnClickListener {
            val bundle=Bundle()
            bundle.putString("type","health")
            bundle.putString("alertFor",alertFor)
            bundle.putString("AlertType",selectedAlertType)
            bundle.putString("selected_time",selectedTimeMinutes)

            if (alertFor == "other"){
                findNavController().navigate(R.id.userContactListFragment,bundle)
            }else{
                findNavController().navigate(R.id.healthAlertCalanderFragment,bundle)
            }

        }

        data.clear()
        data.add(TimeModel("Medical Appointment",false))
        data.add(TimeModel("X-Ray Appointment",false))
        data.add(TimeModel("Ultra-Sound Appointment",false))
        data.add(TimeModel("Appointment with Joe Goldburg",false))

        binding.tvMinit.text="Select Health Alert"

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
        if (type == "time" && pos != null) {

            val index = pos.toInt()

            data.forEachIndexed { i, item ->
                data[i] = item.copy(status = i == index)
            }

            selectedAlertType = data[index].name

            binding.tvMinit.text = selectedAlertType
        }

        popupWindow.dismiss()
    }


}