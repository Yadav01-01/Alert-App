package com.alert.app.fragment.main

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.base.BaseApplication
import com.alert.app.base.CustomDateDecorator
import com.alert.app.databinding.FragmentAddAlertBinding
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.AlertUtils.convertTo24HourTime
import com.alert.app.errormessage.AlertUtils.convertToStandardDateyyyyMMdd
import com.alert.app.errormessage.AlertUtils.showAlert
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.selfAlert.CreateSelfAlertRequest
import com.alert.app.viewmodel.selfalertviewmodel.SelfAlertViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AddAlertFragment : Fragment() {

    private lateinit var binding: FragmentAddAlertBinding
    private val viewModel: SelfAlertViewModel by viewModels()
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private var currectDate = ""
    private var selectDate = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        val activity = requireActivity() as MainActivity
        activity.setImageShowTv()?.visibility = View.GONE
        activity.setImgChatBoot().visibility = View.GONE

        binding.imgBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSetAlert.setOnClickListener {
            if (isValidation()) {
                val createSelfAlertRequest = CreateSelfAlertRequest(
                    binding.alertTitle.text.toString(),
                    convertToStandardDateyyyyMMdd(currectDate),
                    convertToStandardDateyyyyMMdd(selectDate),
                    convertTo24HourTime(binding.tvStartTime.text.toString()),
                    convertTo24HourTime(binding.tvEndTime.text.toString()),
                    binding.edText.text.toString()
                )
                addSelfAlert(createSelfAlertRequest)
            }
        }

        // Show calendar when dateTv is clicked
        binding.rlDt.setOnClickListener {
            if (binding.cal.visibility == View.VISIBLE){
                binding.cal.visibility = View.GONE
            }else{
                binding.cal.visibility = View.VISIBLE
                setupCalendar()
            }
        }

        // Show time picker for Start Time
        binding.tvStartTime.setOnClickListener {
            showTimePickerDialog { formattedTime ->
                binding.tvStartTime.text = formattedTime.uppercase()
                binding.timeTv.visibility = View.VISIBLE
            }
        }

        // Show time picker for End Time
        binding.tvEndTime.setOnClickListener { showTimePickerDialog { formattedTime ->
            binding.tvEndTime.text = formattedTime.uppercase()
        }}
    }

    private fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, hourOfDay, minuteOfHour ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minuteOfHour)
            val formattedTime = timeFormat.format(calendar.time)
            onTimeSelected(formattedTime)
        }, hour, minute, false).show()
    }

    private fun addSelfAlert(createSelfAlertRequest: CreateSelfAlertRequest) {
        if (BaseApplication.isOnline(requireContext())) {
            BaseApplication.openDialog()
            lifecycleScope.launch {
                viewModel.addSelfAlert(createSelfAlertRequest).collect {
                    BaseApplication.dismissDialog()
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.let {
                                if (it.has(getString(R.string.apiCode)) && it.get(getString(R.string.apiCode)).asInt==200) {
                                    findNavController().navigateUp()
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

    private fun setupCalendar() {
        val today = CalendarDay.today()
        val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
        // Setting the minimum date and showing all other dates
        val localDate = DateTime()
        binding.calenderView.state().edit().setMinimumDate(CalendarDay.from(localDate.year,
            localDate.monthOfYear, localDate.dayOfMonth)).commit()
        binding.calenderView.showOtherDates = MaterialCalendarView.SHOW_ALL
        binding.calenderView.setDateSelected(CalendarDay.today(), true)
        val selectedDate = binding.calenderView.selectedDate
        selectedDate?.let {
            val formattedDate = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                .format(Calendar.getInstance().apply {
                    set(it.year, it.month - 1, it.day)
                }.time)

            Log.d("******", "Selected Date: $formattedDate")
            currectDate = formattedDate
            binding.dateTv.text = formattedDate
        }
        with(binding.calenderView) {
            state().edit().setMinimumDate(today).commit()
            showOtherDates = MaterialCalendarView.SHOW_ALL
            setTitleFormatter { calendarDay ->
                val calendar = Calendar.getInstance().apply {
                    set(calendarDay.year, calendarDay.month - 1, calendarDay.day)
                }
                dateFormat.format(calendar.time)
            }

            // Adding the OnDateChangedListener to update the TextView with the selected date
            setOnDateChangedListener { widget, date, selected ->
                if (selected) {
                    val formattedDate = dateFormat.format(Date(date.year - 1900, date.month - 1, date.day)) // Date() constructor uses 1900 offset
                    binding.dateTv.text = formattedDate
                    selectDate = formattedDate
                    binding.cal.visibility = View.GONE
                }
            }
        }
    }

    private fun createRangeSelectedListener(): OnRangeSelectedListener {
        return OnRangeSelectedListener { widget, dates ->
            if (dates.size < 2) return@OnRangeSelectedListener

            val startDate = dates.first()
            val endDate = dates.last()
            val intermediateDates = dates.toMutableSet().apply {
                remove(startDate)
                remove(endDate)
            }

            applyDecorators(widget, startDate, endDate, intermediateDates)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun applyDecorators(widget: MaterialCalendarView,
                                startDate: CalendarDay, endDate: CalendarDay,
                                intermediateDates: Set<CalendarDay>) {
        val startDrawable = requireContext().getDrawable(R.drawable.startend_icon)
        val intermediateDrawable = requireContext().getDrawable(R.drawable.center_date_icon)

        widget.removeDecorators()
        widget.addDecorator(CustomDateDecorator(startDrawable, Color.WHITE, setOf(startDate)))
        widget.addDecorator(CustomDateDecorator(startDrawable, Color.WHITE, setOf(endDate)))
        widget.addDecorator(CustomDateDecorator(intermediateDrawable, Color.BLACK, intermediateDates))
    }


    private fun isValidation(): Boolean {
        if (binding.alertTitle.text.toString().trim().isEmpty()){
            showAlert(requireContext(),MessageClass.alertTitle,false)
            return false
        }else  if (binding.dateTv.text.toString().trim().isEmpty()) {
            showAlert(requireActivity(),MessageClass.selectdate,false)
            return false
        }else  if (binding.tvStartTime.text.toString().trim().isEmpty()) {
            showAlert(requireActivity(),MessageClass.selecttime,false)
            return false
        }/*else  if (binding.tvEndTime.text.toString().trim().isEmpty()) {
            showAlert(requireActivity(),MessageClass.selectend,false)
            return false
        }*/else  if (binding.edText.text.toString().trim().isEmpty()) {
            showAlert(requireActivity(),MessageClass.notes,false)
            return false
        }
        return true
    }
}
