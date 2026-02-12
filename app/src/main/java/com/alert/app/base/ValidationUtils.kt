package com.alert.app.base

object ValidationUtils {


    // Email validate karne ke liye
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Phone number validate karne ke liye (Minimum 10 digits check)
    fun isValidPhone(phone: String): Boolean {
        return phone.isNotEmpty() && android.util.Patterns.PHONE.matcher(phone).matches() && phone.length >= 10
    }

}