package com.alert.app.base

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class StartEndDateDecorator(
    private val startDate: CalendarDay,
    private val endDate: CalendarDay,
    private val backgroundDrawable: Drawable
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == startDate || day == endDate
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(backgroundDrawable)
        view.addSpan(ForegroundColorSpan(Color.WHITE))// Set text color to white
    }
}