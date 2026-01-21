package com.alert.app.errormessage

import android.content.Context
import com.alert.app.base.BaseApplication
import java.text.SimpleDateFormat
import java.util.Locale

object AlertUtils {
    fun showAlert(context: Context, message: String, status: Boolean) {
        BaseApplication.alertError(context, message, status)
    }

    fun formatDateTimeRange(start: String, end: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd, HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())

        return try {
            val startDate = inputFormat.parse(start)
            val endDate = inputFormat.parse(end)
            "${outputFormat.format(startDate).uppercase()} to ${outputFormat.format(endDate).uppercase()}"
        } catch (e: Exception) {
            "Invalid date"
        }
    }

    fun formatDateTimeRangeSingle(start: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd, HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE, dd MMMM, yyyy  hh:mm a", Locale.getDefault())

        return try {
            val startDate = inputFormat.parse(start)
            "${outputFormat.format(startDate)}"
        } catch (e: Exception) {
            "Invalid date"
        }
    }

    fun getDayOfWeek(dateString: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateString)
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        return dayFormat.format(date!!)
    }


    fun convertToStandardDateyyyyMMdd(input: String?): String {
        val inputFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        return try {
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            ""
        }
    }
    fun convertTo24HourTime(input: String): String {
        val inputFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)

        return try {
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            ""
        }
    }
}