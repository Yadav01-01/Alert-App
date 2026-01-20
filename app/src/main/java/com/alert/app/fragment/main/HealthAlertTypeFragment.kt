package com.alert.app.fragment.main

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.TimeArrayCustomListAdapter
import com.alert.app.databinding.FragmentHealthAlertTypeBinding
import com.alert.app.listener.OnClickEventDropDownType
import com.alert.app.model.TimeModel


class HealthAlertTypeFragment : Fragment(), OnClickEventDropDownType {

    private lateinit var binding: FragmentHealthAlertTypeBinding
    private val data: MutableList<TimeModel> = mutableListOf()
    private lateinit var popupWindow: PopupWindow

    private var selectedTime: TimeModel? = null
    private var alertFor: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHealthAlertTypeBinding.inflate(inflater, container, false)
        alertFor = arguments?.getString("alertFor")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupData()
        setupClicks()
    }

    private fun setupUI() {
        (requireActivity() as MainActivity).apply {
            setImageShowTv()?.visibility = View.GONE
            setImgChatBoot().visibility = View.GONE
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupData() {
        data.clear()
        data.addAll(
            listOf(
                TimeModel("2 Minutes", false),
                TimeModel("5 Minutes", false),
                TimeModel("10 Minutes", false),
                TimeModel("15 Minutes", false),
                TimeModel("20 Minutes", false),
                TimeModel("25 Minutes", false),
                TimeModel("30 Minutes", false),
                TimeModel("45 Minutes", false),
                TimeModel("1 Hour", false)
            )
        )
    }

    private fun setupClicks() {

        binding.btnNext.setOnClickListener {

            if (selectedTime == null) {
                // Optional validation
                return@setOnClickListener
            }

            val timeFormatted = convertTimeToHourMinute(selectedTime!!.name)

            val bundle = Bundle().apply {
                putString("selected_time", timeFormatted) // HH:mm
                putString("alertFor", alertFor)
            }

            findNavController().navigate(R.id.healthAlertTypeSecondFragment, bundle)
        }

        binding.tvMinit.setOnClickListener {
            showDropDown()
        }
    }

    private fun showDropDown() {
        val inflater =
            requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.item_select_layout, null)

        popupWindow = PopupWindow(
            popupView,
            binding.tvMinit.width,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.showAsDropDown(binding.tvMinit, 0, 0, Gravity.CENTER)

        val rcyData = popupView.findViewById<RecyclerView>(R.id.rcy_data)
        rcyData.adapter =
            TimeArrayCustomListAdapter(requireContext(), data, this, "time")

        binding.tvMinit.setCompoundDrawablesWithIntrinsicBounds(
            0, 0, R.drawable.left_arrow_top, 0
        )

        popupWindow.setOnDismissListener {
            binding.tvMinit.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.arrow_bottom, 0
            )
        }
    }

    override fun onClickDropDown(pos: String?, type: String?) {
        if (type == "time" && pos != null) {

            val index = pos.toInt()

            data.forEachIndexed { i, item ->
                data[i] = item.copy(status = i == index)
            }

            selectedTime = data[index]
            binding.tvMinit.text = selectedTime?.name
        }

        popupWindow.dismiss()
    }

    // 🔥 FINAL TIME CONVERTER → HH:mm
    private fun convertTimeToHourMinute(time: String?): String {
        if (time.isNullOrEmpty()) return "00:00"

        val value = time.split(" ")[0].toIntOrNull() ?: return "00:00"

        return when {
            time.contains("Hour", true) -> {
                String.format("%02d:00", value)
            }
            time.contains("Minute", true) -> {
                String.format("00:%02d", value)
            }
            else -> "00:00"
        }
    }
}
