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

    private var startDateApi: CalendarDay? = null

    private var endDateApi: CalendarDay? = null

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

    private fun formatCalendarDay(day: CalendarDay?): String {
        if (day == null) return "" // or return some default value

        val calendar = Calendar.getInstance().apply {
            set(day.year, day.month - 1, day.day) // month is 0-based
        }

        return apiDateFormat.format(calendar.time)
    }


    private fun setupCalendar() {

        val today = CalendarDay.today()
        val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())

        with(binding.calenderView) {

            // Enable RANGE selection
            selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE

            // Set minimum selectable date
            state().edit()
                .setMinimumDate(today)
                .commit()

            showOtherDates = MaterialCalendarView.SHOW_ALL

            // Custom title formatter
            setTitleFormatter { calendarDay ->
                val calendar = Calendar.getInstance().apply {
                    set(calendarDay.year, calendarDay.month - 1, calendarDay.day)
                }
                dateFormat.format(calendar.time)
            }

            // Listen for date range selection
            setOnRangeSelectedListener { _, dates ->
                if (dates.isNotEmpty()) {

                    val start = dates.first()
                    val end = dates.last()

                    // Save selected dates
                    startDateApi = start
                    endDateApi = end

                    startDate = formatCalendarDay(start, dateFormat)
                    endDate = formatCalendarDay(end, dateFormat)

                    binding.dateTv.text = "$startDate to $endDate"

                    Log.d("CALENDAR", "Start Date: $startDate")
                    Log.d("CALENDAR", "End Date: $endDate")


                    //   binding.cal.visibility = View.GONE
                }
            }
        }
    }

    // Restore previously selected range (if any)
    private fun restoreSelectedRange() {
        binding.calenderView.post {
            binding.calenderView.clearSelection()

            if (startDateApi != null && endDateApi != null) {
                binding.calenderView.selectRange(startDateApi!!, endDateApi!!)
            }
        }
    }


    private fun formatCalendarDay(
        day: CalendarDay,
        formatter: SimpleDateFormat
    ): String {
        val calendar = Calendar.getInstance().apply {
            set(day.year, day.month - 1, day.day)
        }
        return formatter.format(calendar.time)
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
