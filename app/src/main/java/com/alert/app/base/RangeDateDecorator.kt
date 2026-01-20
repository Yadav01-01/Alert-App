package com.alert.app.base

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class RangeDateDecorator(
    private val startDate: CalendarDay?,
    private val endDate: CalendarDay?,
    private val intermediateDates: List<CalendarDay>,
    private val startEndDrawable: Drawable,
    private val intermediateDrawable: Drawable
) : DayViewDecorator {


    private var currentDay: CalendarDay? = null


    override fun shouldDecorate(day: CalendarDay): Boolean {
        // Store the current day being processed
        currentDay = day


        // Check if the day is the start, end, or one of the intermediate dates
        return day == startDate || day == endDate || intermediateDates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        // Apply appropriate drawable based on the type of date
        when {
            currentDay == startDate || currentDay == endDate -> {
                view.setBackgroundDrawable(startEndDrawable)
                view.addSpan(ForegroundColorSpan(Color.WHITE))
            }
            intermediateDates.contains(currentDay) -> {
                view.setBackgroundDrawable(intermediateDrawable)
            }
        }
    }
}