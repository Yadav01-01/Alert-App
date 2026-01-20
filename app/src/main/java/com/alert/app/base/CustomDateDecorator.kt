package com.alert.app.base

import android.graphics.drawable.Drawable
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class CustomDateDecorator(
    private val backgroundDrawable: Drawable?,
    private val color: Int,
    private val dates: Set<CalendarDay>
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
//        // Set the background drawable
//        view.setBackgroundDrawable(backgroundDrawable!!)

        // Set the background drawable (icon or background)
//        backgroundDrawable?.let { view.setBackgroundDrawable(it) }
        backgroundDrawable?.let { view.setSelectionDrawable(it) }

        view.addSpan(ForegroundColorSpan(color)) // Optional: make bold
    }
}