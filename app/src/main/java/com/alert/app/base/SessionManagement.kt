package com.alert.app.base

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.mykameal.planner.fragment.authfragment.login.model.RememberMe

class SessionManagement(var context: Context) {
    var dialog: Dialog? = null
    var editor: SharedPreferences.Editor? = null
    var editor2: SharedPreferences.Editor? = null
    var pref: SharedPreferences? = null
    var pref2: SharedPreferences? = null

    init {
        pref = context.getSharedPreferences(AppConstant.LOGIN_SESSION, Context.MODE_PRIVATE)
        pref2 = context.getSharedPreferences(AppConstant.RememberMe_SESSION, Context.MODE_PRIVATE)
        editor2 = pref2?.edit()
        editor = pref?.edit()
    }


    // set details

    fun setUserToken(data: String) {
        editor!!.putString(AppConstant.tokenUser, data)
        editor!!.commit()
    }

    fun setProfileScreen(data: String) {
        editor!!.putString(AppConstant.profile, data)
        editor!!.commit()
    }

    fun setUserEditable(status: Boolean) {
        editor!!.putBoolean(AppConstant.UserEditable, status)
        editor!!.commit()
    }
    fun setUserProfile(data: String) {
        editor!!.putString(AppConstant.profilePic, data)
        editor!!.commit()
    }

    fun setUserEmail(email: String) {
        editor!!.putString(AppConstant.EMAIL, email)
        editor!!.commit()
    }
    fun setLoginSession(session: Boolean?) {
        editor!!.putBoolean(AppConstant.loginSession, session!!)
        editor!!.commit()
    }

    fun setUserPhoneNumber(phone: String) {
        editor!!.putString(AppConstant.PHONE, phone)
        editor!!.commit()
    }

    fun setUserId(id: Int) {
        editor!!.putInt(AppConstant.Id, id)
        editor!!.commit()
    }


    fun setUserName(name: String) {
        editor!!.putString(AppConstant.NAME, name)
        editor!!.commit()
    }

    fun setRememberMe(value : List<RememberMe>){
        editor2!!.putString(AppConstant.rememberMe, Gson().toJson(value))
        editor2!!.apply()
    }


    // get details

    fun getRememberMe() : String?{
        return pref2!!.getString(AppConstant.rememberMe,"")!!
    }
    fun getUserToken() : String?{
        return pref!!.getString(AppConstant.tokenUser,"")!!
    }


    fun getProfileScreen(): String?{
        return pref?.getString(AppConstant.profile, "signup")
    }

    fun getUserProfile(): String? {
        return pref?.getString(AppConstant.profilePic, "")
    }

    fun getUserId(): Int? {
        return pref?.getInt(AppConstant.Id, -1)
    }

    fun getUserName(): String? {
        return pref?.getString(AppConstant.NAME, "")
    }

    fun getUserEditable(): Boolean? {
        return pref?.getBoolean(AppConstant.UserEditable, false)
    }


    fun getUserEmail(): String? {
        return pref?.getString(AppConstant.EMAIL, "")
    }
    fun getLoginSession(): Boolean {
        return pref!!.getBoolean(AppConstant.loginSession, false)
    }

    fun getUserPhoneNumber(): String? {
        return pref?.getString(AppConstant.PHONE, "")
    }


    // session clear
    fun logOut() {
        editor?.clear()
        editor?.apply()
    }



}