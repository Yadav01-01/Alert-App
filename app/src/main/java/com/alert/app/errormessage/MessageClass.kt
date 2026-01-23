package com.alert.app.errormessage

import com.alert.app.base.BaseApplication

object MessageClass {

    const val networkError="You're offline. Please Check your internet connection and try again."
    const val sessionError="Your session has expired. Please log in again to continue."
    const val deactivatedUser=403
    const val PATH_ERROR="Path should not be null"
    const val deletedUser=404
    const val tokenEnd=402
    const val emailRegulerExpression =
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    const val passwordRegulerExpression="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!~])(?=\\S+\$).{6,20}\$"
    const val nameError="Name can't be empty."
    const val alertTitle="Alert Title can't be empty."
    const val selectdate="Please select date."
    const val selecttime="Please start time."
    const val selectend="Please end time."
    const val notes="Notes can't be empty."
    const val fullnameError="Full Name can't be empty."
    const val lastError="Last Name can't be empty."
    const val massageError="Massage can't be empty."
    const val emailPhoneError="Email/phone can't be empty."
    const val emergencyMessage="Message can't be empty."
    const val emergencyMessageHint="Enter Your Message here...."
    const val selectCountryError="Please Select Country."
    const val selectLanguageError="Please Select Language."
    const val phoneError="Phone can't be empty."
    const val relation="Please select relation"
    const val alert="Please select alert"
    const val addressError="Address can't be empty."
    const val typeError="Please Select Type"
    const val AgreetoacceptError="Please Agree to Accept Terms and condition."
    const val profilePic="Please choose a profile picture."
    const val emailErrorValidation="Please enter valid email."
    const val emailError="Email can't be empty."
    const val passwordValidationError="Your password must be at least 6 characters including a lowercase letter, an uppercase letter, a special character and a number."
    const val passwordError="Password can't be empty."
    const val cnfPasswordError="Confirm Password can't be empty."
    const val passwordSameError="Password and confirm password should be same."
    const val confirmPasswordError="Confirm password can't be empty."
    const val emailPhoneValidationError="Please enter valid email or phone number."
    const val emailValidationError="Please enter valid email."
    const val emptyOtp="OTP can't be empty."
    const val correctOtp="Please enter correct OTP."
    const val verifyStatus="Verified"
    const val verifyNowStatus="<u>Verify now</u>"
    const val emailVaildStatus="invalid email"
    const val emailVerifyStatus="Email Verify First"
    const val phoneVerifyStatus="Phone Verify First"
    const val phoneVaildStatus="invalid phone"
    const val apiError:String="Something went wrong"
    const val phoneValidationError = "Please enter a valid 10-digit phone number"
    const val countryCodeSelectionError = "Please Select Country Code"

}