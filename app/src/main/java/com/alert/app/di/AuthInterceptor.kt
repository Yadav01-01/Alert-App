package com.alert.app.di

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alert.app.activity.AuthActivity
import com.alert.app.base.SessionManagement
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(var context: Context) : Interceptor {
    @SuppressLint("SuspiciousIndentation")
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder: Request.Builder = chain.request().newBuilder()
        val token = getBearerToken()
        Log.d("@@@@@@", "token $token")
       /* if (token != null && token.isNotEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
            requestBuilder.addHeader("Accept", "application/json")
        }*/
        token.takeIf { it.isNotEmpty() }?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
            requestBuilder.addHeader("Accept", "application/json")
        }
        //return chain.proceed(requestBuilder.build())
        val response = chain.proceed(requestBuilder.build())
        Log.d("response@@@@@@@", response.toString())
        // Check for 401 Unauthorized response
        if (response.code == 401) {
//            val intent = Intent("com.yourapp.LOGOUT")
//
//            context.sendBroadcast(intent)
            val sessionManagement = SessionManagement(context)
            handleTokenExpiration(sessionManagement)
        }
        return response
    }

    private fun getBearerToken(): String {
        val sessionManagement = SessionManagement(context)
        val token: String = sessionManagement.getUserToken()!!

        return token
    }

    private fun handleTokenExpiration(sessionManager: SessionManagement) {
        // Clear session
        sessionManager.logOut()
        // Redirect to login screen
        val intent  = Intent(context, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

}