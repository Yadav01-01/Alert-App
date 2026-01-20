package com.alert.app.base


import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade


class DisablePastDatesDecorator : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.isBefore(CalendarDay.today())
    }

    override fun decorate(view: DayViewFacade) {
        view.setDaysDisabled(true) // Disable the dates
    }
}

