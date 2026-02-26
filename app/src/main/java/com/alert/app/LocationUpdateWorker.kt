package com.alert.app

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LocationUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params)
{

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get data from input
            val journeyId = inputData.getString("journey_id") ?: run {
                Log.e("RetrofitLog", "❌ LocationUpdateWorker: journey_id is null")
                return@withContext Result.failure()
            }
            val token = inputData.getString("token") ?: run {
                Log.e("RetrofitLog", "❌ LocationUpdateWorker: token is null")
                return@withContext Result.failure()
            }
            val latitude = inputData.getDouble("latitude", 0.0)
            val longitude = inputData.getDouble("longitude", 0.0)

            // Log worker execution
            Log.d("RetrofitLog", "📍 LocationUpdateWorker: Executing at ${System.currentTimeMillis()}")
            Log.d("RetrofitLog", "📍 Journey ID: $journeyId")
            Log.d("RetrofitLog", "📍 Latitude: $latitude, Longitude: $longitude")

            // Skip if coordinates are invalid
            if (latitude == 0.0 || longitude == 0.0) {
                Log.w("RetrofitLog", "⚠️ LocationUpdateWorker: Invalid coordinates (0.0), retrying...")
                return@withContext Result.retry()
            }

            // Call API
            Log.d("RetrofitLog", "📡 Calling update_live_location API...")
            val success = updateLiveLocation(journeyId, token, latitude, longitude)

            if (success) {
                Log.d("RetrofitLog", "✅ LocationUpdateWorker: API call successful")
                Result.success()
            } else {
                Log.e("RetrofitLog", "❌ LocationUpdateWorker: API call failed")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("RetrofitLog", "❌ LocationUpdateWorker: Exception in doWork - ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun updateLiveLocation(
        journeyId: String,
        token: String,
        latitude: Double,
        longitude: Double
    ): Boolean {
        return try {
            Log.d("RetrofitLog", "📤 Preparing API request...")
            Log.d("RetrofitLog", "URL: https://alertapp.tgastaging.com/api/update_live_location")
            Log.d("RetrofitLog", "Request Body: {\"journey_id\":\"$journeyId\",\"latitude\":\"$latitude\",\"longitude\":\"$longitude\"}")

            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val json = JSONObject().apply {
                put("journey_id", journeyId)
                put("latitude", latitude.toString())
                put("longitude", longitude.toString())
            }

            val request = Request.Builder()
                .url("https://alertapp.tgastaging.com/api/update_live_location")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()

            Log.d("RetrofitLog", "📤 Sending request...")
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d("RetrofitLog", "📥 Response successful - Code: ${response.code}")
                Log.d("RetrofitLog", "📥 Response Body: $responseBody")

                val jsonResponse = JSONObject(responseBody ?: "")
                val code = jsonResponse.optJSONObject("data")?.optInt("code") ?: 0
                val message = jsonResponse.optJSONObject("data")?.optString("message") ?: ""

                Log.d("RetrofitLog", "📥 Parsed Response - Code: $code, Message: $message")

                code == 200
            } else {
                Log.e("RetrofitLog", "❌ Response failed - Code: ${response.code}")
                Log.e("RetrofitLog", "❌ Error Body: ${response.body?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e("RetrofitLog", "❌ Exception in updateLiveLocation: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    companion object {
        const val WORK_NAME = "location_update_work"
        private const val TAG = "RetrofitLog"
    }
}
/*
package com.alert.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LocationUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params)
{

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get data from input
            val journeyId = inputData.getString("journey_id") ?: return@withContext Result.failure()
            val token = inputData.getString("token") ?: return@withContext Result.failure()
            val latitude = inputData.getDouble("latitude", 0.0)
            val longitude = inputData.getDouble("longitude", 0.0)

            // Skip if coordinates are invalid
            if (latitude == 0.0 || longitude == 0.0) {
                return@withContext Result.retry()
            }

            // Call API
            val success = updateLiveLocation(journeyId, token, latitude, longitude)

            if (success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun updateLiveLocation(
        journeyId: String,
        token: String,
        latitude: Double,
        longitude: Double
    ): Boolean {
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val json = JSONObject().apply {
                put("journey_id", journeyId)
                put("latitude", latitude.toString())
                put("longitude", longitude.toString())
            }

            val request = Request.Builder()
                .url("https://alertapp.tgastaging.com/api/update_live_location")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody ?: "")
                val code = jsonResponse.optJSONObject("data")?.optInt("code") ?: 0
                code == 200
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        const val WORK_NAME = "location_update_work"
    }
}*/
