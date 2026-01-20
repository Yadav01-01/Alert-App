package com.alert.app.fragment.main


import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener
import com.alert.app.R
import com.alert.app.activity.MainActivity
import com.alert.app.adapter.TimeArrayCustomListAdapter
import com.alert.app.base.CustomDateDecorator
import com.alert.app.databinding.FragmentSendAlertBinding
import com.alert.app.listener.OnClickEventDropDownType
import com.alert.app.model.TimeModel
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SendAlertFragment : Fragment() , OnClickEventDropDownType {

    private var _binding: FragmentSendAlertBinding?=null
    private val binding get() = _binding!!
    val data: MutableList<TimeModel> = mutableListOf()
    private val dataAlert: MutableList<TimeModel> = mutableListOf()
    lateinit var popupWindow:PopupWindow

    var type=""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSendAlertBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        (requireActivity() as MainActivity).setImageShowTv()?.visibility=View.GONE
        (requireActivity() as MainActivity).setImgChatBoot().visibility =View.GONE

        type=arguments?.getString("type","").toString()


        binding.tvTitle.text=type


        if (type.equals("Send Alert",true)){
            binding.layOut.visibility=View.GONE
        }else{

            binding.layOut.visibility=View.VISIBLE
            binding.btnSendAlert.text = "Set"
        }

        data.clear()
        data.add(TimeModel("15 Minutes",false))
        data.add(TimeModel("30 Minutes",false))
        data.add(TimeModel("45 Minutes",false))
        data.add(TimeModel("1 Hour",false))
        data.add(TimeModel("2 Hour",false))
        data.add(TimeModel("8 Hour",false))
        data.add(TimeModel("12 Hour",false))
        data.add(TimeModel("24 Hour",false))



        dataAlert.clear()





        // data alert

        dataAlert.add(TimeModel("A",false))
        dataAlert.add(TimeModel("B",false))
        dataAlert.add(TimeModel("C",false))
        dataAlert.add(TimeModel("D",false))


        binding.tvMinit.setOnClickListener {
            binding.tvMinit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.left_arrow_top, 0)
            val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = inflater?.inflate(R.layout.item_select_layout, null)
            popupWindow = PopupWindow(popupView, binding.tvMinit.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAsDropDown(binding.tvMinit,  0, 0, Gravity.CENTER)

            // Access views inside the inflated layout using findViewById
            val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)

            rcyData?.adapter=TimeArrayCustomListAdapter(requireContext(),data,this,"time")

            // Set the dismiss listener
            popupWindow.setOnDismissListener {
                binding.tvMinit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_bottom, 0)
            }
        }
        binding.tvCal.setOnClickListener {
            binding.calCard.visibility = View.VISIBLE
        }

        binding.tvAlert.setOnClickListener {
            binding.tvAlert.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.left_arrow_top, 0)
            val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = inflater?.inflate(R.layout.item_select_layout, null)
            popupWindow = PopupWindow(popupView, binding.tvAlert.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAsDropDown(binding.tvAlert,  0, 0, Gravity.CENTER)

            // Access views inside the inflated layout using findViewById
            val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)

            rcyData?.adapter=TimeArrayCustomListAdapter(requireContext(),dataAlert,this,"Alert")


            // Set the dismiss listener
            popupWindow.setOnDismissListener {
                binding.tvAlert.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_bottom, 0)
            }

        }


        val today = CalendarDay.today()

        binding.calenderView.state().edit()
            .setMinimumDate(today) // Set the minimum date
            .commit()
        binding.calenderView.showOtherDates = MaterialCalendarView.SHOW_ALL


        val localDate = DateTime()
        binding.calenderView.state().edit().setMinimumDate(CalendarDay.from(localDate.year, localDate.monthOfYear, localDate.dayOfMonth)).commit()
        binding.calenderView.showOtherDates = MaterialCalendarView.SHOW_ALL
        binding.calenderView.setDateSelected(CalendarDay.today(), true)

        // Custom format: "09 November, 2024"
        val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())

        binding.calenderView.setTitleFormatter { calendarDay ->
            // Convert CalendarDay to a Calendar object
            val calendar = Calendar.getInstance().apply {
                set(calendarDay.year, calendarDay.month - 1, calendarDay.day) // Month is 0-indexed
            }
            // Format the Calendar object
            dateFormat.format(calendar.time)
        }


        binding.calenderView.setOnRangeSelectedListener(object : OnRangeSelectedListener {
            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onRangeSelected(widget: MaterialCalendarView, dates: MutableList<CalendarDay>) {

                if (dates.size < 2) return  // Ensure valid range

                val startDate = dates[0]
                val endDate = dates[dates.size - 1]


                // Separate sets for start, end, and intermediate dates
                val startDates: MutableSet<CalendarDay> = HashSet()
                startDates.add(startDate)

                val endDates: MutableSet<CalendarDay> = HashSet()
                endDates.add(endDate)

                val intermediateDates: MutableSet<CalendarDay> = HashSet(dates)
                intermediateDates.remove(startDate)
                intermediateDates.remove(endDate)

                // Clear existing decorators
                binding.calenderView.removeDecorators()
                val startDrawable = requireContext().getDrawable(R.drawable.startend_icon)
                val intermediateDrawable = requireContext().getDrawable(R.drawable.center_date_icon)

                // Add new decorators
                binding.calenderView.addDecorator(CustomDateDecorator(startDrawable,Color.WHITE, startDates)) // Start date in red
                binding.calenderView.addDecorator(CustomDateDecorator(startDrawable,Color.WHITE, endDates)) // End date in green
                binding.calenderView.addDecorator(CustomDateDecorator(intermediateDrawable,Color.BLACK, intermediateDates)) // Intermediate dates in blue


            }
        })


        binding.tvStartTime.setOnClickListener {
           /* val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = inflater?.inflate(R.layout.time_health_layout, null)
            val tvOK = popupView?.findViewById<TextView>(R.id.tvOK)
            val popupWindow = PopupWindow(popupView, binding.tvTime.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAsDropDown(binding.tvTime,  0, 0, Gravity.END)


            tvOK?.setOnClickListener {
                popupWindow.dismiss()
            }*/

            // on below line we are getting
            // the instance of our calendar.
            val c = Calendar.getInstance()

            // on below line we are getting our hour, minute.
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            // our Time Picker Dialog
            val timePickerDialog = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                // on below line we are setting selected
                // time in our text view.

                // time in our text view.
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                // Using SimpleDateFormat to format the time.
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // "hh" for 12-hour format, "a" for AM/PM
                val formattedTime = timeFormat.format(calendar.time)
                binding.tvStartTime.text = formattedTime.uppercase()
            },
                hour,
                minute,
                false
            )
            // at last we are calling show to
            // display our time picker dialog.
            timePickerDialog.show()

        }

        binding.tvEndTime.setOnClickListener {

            // on below line we are getting
            // the instance of our calendar.
            val c = Calendar.getInstance()

            // on below line we are getting our hour, minute.
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            // on below line we are initializing
            // our Time Picker Dialog
            val timePickerDialog = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                // on below line we are setting selected
                // time in our text view.

                // time in our text view.
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                // Using SimpleDateFormat to format the time.
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // "hh" for 12-hour format, "a" for AM/PM
                val formattedTime = timeFormat.format(calendar.time)

                binding.tvEndTime.text = formattedTime.uppercase()
            },
                hour,
                minute,
                false
            )
            // at last we are calling show to
            // display our time picker dialog.
            timePickerDialog.show()

        }



        binding.btnSendAlert.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    override fun onClickDropDown(pos: String?,type:String?) {
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

        if (type.equals("Alert")){
            for (i in dataAlert.indices) {
                val item = dataAlert[i].copy() // Create a copy to avoid modifying the reference at `position`
                item.name = dataAlert[i].name ?: ""
                // Set the status based on position
                item.status = i == pos?.toInt()
                // Update the item in the list
                dataAlert[i] = item
            }
            // Set the text of the category at 'position'
            binding.tvAlert.text = dataAlert[pos?.toInt()!!].name
        }

        popupWindow.dismiss()

    }

}