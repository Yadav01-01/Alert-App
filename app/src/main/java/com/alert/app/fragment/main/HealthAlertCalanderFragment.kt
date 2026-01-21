package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentHealthAlertCalanderBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils
import com.alert.app.model.AddHealthAlertResponse
import com.alert.app.model.contact.ContactListResponse
import com.alert.app.viewmodel.healthviewmodel.HealthAlertViewModel
import com.google.gson.Gson
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.core.view.isVisible

@AndroidEntryPoint
class HealthAlertCalanderFragment : Fragment() {

    private lateinit var binding: FragmentHealthAlertCalanderBinding
    private lateinit var viewModel: HealthAlertViewModel

    // Arguments
    private lateinit var type: String
    private var alertFor: String? = null
    private var selectedTimeMinutes: String? = null
    private var selectedAlertType: String? = null
    private var contactId: String? = null

    private var startDate: String? = null
    private var endDate: String? = null


    // Stored values
    private var selectedDate: String? = null
    private var startTime: String? = null
    private var endTime: String? = null
    private var notes: String? = null

    private val apiDateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[HealthAlertViewModel::class.java]

        arguments?.let {
            type = it.getString("type").orEmpty()
            alertFor = it.getString("alertFor")
            selectedTimeMinutes = it.getString("selected_time")
            selectedAlertType = it.getString("AlertType")
            contactId = it.getString("CONTACT_ID")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHealthAlertCalanderBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingInflatedId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupBackPress()
        setupCalendar()
        setupTimePickers()
        setupNotes()
        setupClicks()
    }

    private fun setupToolbar() {
        (requireActivity() as MainActivity).setImageShowTv()?.visibility = View.GONE
        (requireActivity() as MainActivity).setImgChatBoot().visibility = View.GONE
    }

    private fun setupBackPress() {
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

    private fun setupCalendar() {

        val calendarView = binding.calenderView

        // MUST be called before selection
        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE

        calendarView.showOtherDates = MaterialCalendarView.SHOW_ALL

        // Clear any previous state
        calendarView.clearSelection()

        // IMPORTANT: Reset listeners before setting again
        calendarView.setOnRangeSelectedListener(null)

        calendarView.setOnRangeSelectedListener { _, dates ->

            if (dates.size >= 2) {

                val start = dates.first()
                val end = dates.last()

                startDate = formatDate(start)
                endDate = formatDate(end)

                binding.datetime.text = "$startDate to $endDate"

                // Hide calendar after selection
                calendarView.visibility = View.GONE
            }
        }
    }




    private fun formatDate(day: CalendarDay): String {
        val calendar = Calendar.getInstance().apply {
            set(day.year, day.month - 1, day.day)
        }
        return apiDateFormat.format(calendar.time)
    }

    private fun setupTimePickers() {
        binding.tvStartTime.setOnClickListener {
            showTimePicker {
                startTime = it
                binding.tvStartTime.text = it
            }
        }

        binding.tvEndTime.setOnClickListener {
            showTimePicker {
                endTime = it
                binding.tvEndTime.text = it
            }
        }
    }

    private fun setupNotes() {
        // If EditText exists in layout
        binding.edText.doAfterTextChanged {
            notes = it.toString()
        }
    }

    private fun setupClicks() {
        Log.d(
            "AlertData",
            """
                CONTACT_ID: $contactId
                TYPE: $type
                ALERT_FOR: $alertFor
                ALERT_TYPE: $selectedAlertType
                Duration : $selectedTimeMinutes
                DATE: $selectedDate
                START_TIME: $startTime
                END_TIME: $endTime
                NOTES: $notes
                """.trimIndent()
        )

        binding.llOpenCalender.setOnClickListener {

            binding.calenderView.apply {
                clearSelection() // VERY IMPORTANT
                visibility = View.VISIBLE
            }
        }



        binding.btnSetAlert.setOnClickListener {

            // if (selectedDate.isNullOrEmpty() || startTime.isNullOrEmpty()) {
            if (startDate.isNullOrEmpty() || endDate.isNullOrEmpty() || startTime.isNullOrEmpty()) {
                AlertUtils.showAlert(
                    requireContext(),
                    "Please select date and start time",
                    false
                )
                return@setOnClickListener
            }

            val timeFormatted = convertTimeToHourMinute(startTime)


            lifecycleScope.launch {
                BaseApplication.openDialog()
                viewModel.addHealthAlert(
                    alertFor = alertFor ?: "",
                    alertDuration = selectedTimeMinutes.toString(),
                    healthAlert = selectedAlertType ?: "",
                    startDate = startDate ?: "",
                    endDate = endDate ?:"",
                    time = timeFormatted,          // HH:mm
                    note = notes ?: "",
                    contact = listOfNotNull(contactId)
                ).collect { result ->

                    when (result) {

                        is NetworkResult.Success -> {
                            BaseApplication.dismissDialog()
                            val response =
                                Gson().fromJson(result.data, AddHealthAlertResponse::class.java)
                            // success UI
                            findNavController().navigate(R.id.healthFragment)
                        }

                        is NetworkResult.Error -> {
                            BaseApplication.dismissDialog()
                            AlertUtils.showAlert(
                                requireContext(),
                                result.message ?: "Something went wrong",
                                false
                            )
                        }
                    }
                }
            }
        }
    }

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




    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, h, m ->
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                }
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(cal.time)
                    .uppercase()

                onTimeSelected(time)
            },
            hour,
            minute,
            false
        ).show()
    }
}
