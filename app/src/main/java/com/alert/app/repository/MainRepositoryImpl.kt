package com.alert.app.repository

import com.alert.app.base.AppConstant
import com.alert.app.di.ApiInterfaceClass
import com.alert.app.di.NetworkResult
import com.alert.app.errormessage.MessageClass
import com.alert.app.model.contact.UserContactRequest
import com.alert.app.model.contact.UserEditContactRequest
import com.alert.app.model.helpingneighbormodel.CreateHelpingNeighbor
import com.alert.app.model.notification.AlertModel
import com.alert.app.model.selfAlert.CreateSelfAlertRequest
import com.google.android.exoplayer2.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject


class MainRepositoryImpl @Inject constructor(private val apiInterface: ApiInterfaceClass) :
    MainRepository {

    // this function call for login request
    override suspend fun loginRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        email: String,
        password: String,
        fcmToken: String,
        deviceType: String,
    ) {
        try {
            apiInterface.loginApiRequest(email, password, fcmToken, deviceType).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {

                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")

                    // Optional: parse JSON to extract "message" field
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }

                    successCallback(NetworkResult.Error(errorMessage))
                    // successCallback(NetworkResult.Error(errorBody().toString()))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun loginPhoneRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        phone: String,
        countryCode: String,
        password: String,
        fcmToken: String,
        deviceType: String
    ) {
        try {
            apiInterface.loginPhoneApiRequest(phone,countryCode, password, fcmToken, deviceType).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {

                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")

                    // Optional: parse JSON to extract "message" field
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }

                    successCallback(NetworkResult.Error(errorMessage))
                    // successCallback(NetworkResult.Error(errorBody().toString()))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun resendOtp(
        successCallback: (response: NetworkResult<String>) -> Unit,
        type: String,
        email: String?,
        phoneNumber: String?
    ) {
        try {
            apiInterface.resendOtpApi(type, email, phoneNumber).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {

                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")

                    // Optional: parse JSON to extract "message" field
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }

                    successCallback(NetworkResult.Error(errorMessage))
                    // successCallback(NetworkResult.Error(errorBody().toString()))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun getProfileRequestApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            apiInterface.getProfileRequestApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {
                    Log.d("checkData", errorBody().toString())
                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")

                    // Optional: parse JSON to extract "message" field
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    successCallback(NetworkResult.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun signupOtpVerifyRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        otp: String,
        email: String?,
        phoneNumber: String?,
        token: String,
        deviceType: String,
    ) {
        try {
            apiInterface.signUpVerifyApiRequest( otp,email,phoneNumber, token, deviceType).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {
                    Log.d("checkData", errorBody().toString())
                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")

                    // Optional: parse JSON to extract "message" field
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    successCallback(NetworkResult.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun forGotOtpVerifyRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        email: String?,
        otp: String,
        phoneNumber :String?
    ) {
        try {
            apiInterface.forGotOtpVerifyRequestApi(email, otp, phoneNumber).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {
                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")

                    // Optional: parse JSON to extract "message" field
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    successCallback(NetworkResult.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun socialLoginRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String,
        deviceType: String,
        token: String
    ) {
        try {
            apiInterface.socialLoginRequestApi(emailOrPhone, token, deviceType).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {
                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")

                    // Optional: parse JSON to extract "message" field
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    successCallback(NetworkResult.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun sendOtpEmailPhoneRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String
    ) {
        try {
            apiInterface.sendOtpEmailPhoneRequestApi(emailOrPhone).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {
                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")

                    // Optional: parse JSON to extract "message" field
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    successCallback(NetworkResult.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun profileUpdateRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        cusName: RequestBody,
        cusEmail: RequestBody,
        cusPhone: RequestBody,
        cusAddress: RequestBody,
        cusLatitude: RequestBody,
        cusLongitude: RequestBody,
        requestImage: MultipartBody.Part?
    ) {
        try {
            apiInterface.profileUpdateRequestApi(
                cusName,
                cusEmail,
                cusPhone,
                cusAddress,
                cusLatitude,
                cusLongitude,
                requestImage
            ).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {
                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")

                    // Optional: parse JSON to extract "message" field
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    successCallback(NetworkResult.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun signupEmailRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        name: String,
        email: String,
        password: String
    ) {
        try {
            apiInterface.signupEmailRequestApi( name,email, password).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {
                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    successCallback(NetworkResult.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun signupPhoneRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        name: String,
        countryCode: String,
        phoneNumber: String,
        password: String
    ) {
        try {
            apiInterface.signupPhoneRequestApi(name, countryCode, phoneNumber,password).apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {
                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    successCallback(NetworkResult.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun forgotPasswordRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        email: String?,
        phone: String?
    ) {
        try {
            apiInterface.forgotPasswordRequestApi(email, phone).apply {
                if (isSuccessful) {
                    body()?.let {
                        val data = it.get("data").asJsonObject
                        val otp = data.get("otp").asInt

                        successCallback(NetworkResult.Success(otp.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {
                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    successCallback(NetworkResult.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun tutorialsDataRequestApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            apiInterface.tutorialsDataRequestApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {
                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    successCallback(NetworkResult.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun termsAndConditionRequestApi(successCallback: (response: NetworkResult<String>) -> Unit) {
        try {
            apiInterface.termsAndConditionRequestApi().apply {
                if (isSuccessful) {
                    body()?.let {
                        successCallback(NetworkResult.Success(it.toString()))
                    } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                } else {
                    val errorJson = errorBody()?.string()
                    Log.d("checkData", errorJson ?: "Unknown error")
                    val errorMessage = try {
                        JSONObject(errorJson ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    successCallback(NetworkResult.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun reseatPasswordRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        email: String?,
        phone: String?,
        password: String,
        password_confirmation: String
    ) {
        try {
            apiInterface.reseatPasswordRequestApi(email, phone, password, password_confirmation)
                .apply {
                    if (isSuccessful) {
                        body()?.let {
                            successCallback(NetworkResult.Success(it.toString()))
                        } ?: successCallback(NetworkResult.Error(MessageClass.apiError))
                    } else {
                        val errorJson = errorBody()?.string()
                        Log.d("checkData", errorJson ?: "Unknown error")
                        // Optional: parse JSON to extract "message" field
                        val errorMessage = try {
                            JSONObject(errorJson ?: "").getString("message")
                        } catch (e: Exception) {
                            "Something went wrong"
                        }
                        successCallback(NetworkResult.Error(errorMessage))
                    }
                }
        } catch (e: Exception) {
            successCallback(NetworkResult.Error(e.message.toString()))
        }
    }

    override suspend fun getContactList(): Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.getContactList().apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }

    override suspend fun getRelation(): Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.getRelation().apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }

    override suspend fun getAllAlerts(): Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.getAllAlerts().apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }

    override suspend fun manualContact(userContactRequest: UserContactRequest)
            : Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.manualContact(userContactRequest).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }


    override suspend fun deleteContact(contactId: String)
            : Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.deleteContact(contactId).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }

    override suspend fun editManualContact(userEditContactRequest: UserEditContactRequest)
            : Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.editManualContact(userEditContactRequest).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }

    override suspend fun addContact(userContactRequest: UserContactRequest)
            : Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.addContact(userContactRequest).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }

    override suspend fun getAlert(contactId: String)
            : Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.getAlert(contactId).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }

    override suspend fun getSelfAlerts(type: String?): Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.getSelfAlerts(type).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }


    override suspend fun addSelfAlert(createSelfAlertRequest: CreateSelfAlertRequest)
            : Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.addSelfAlert(createSelfAlertRequest).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }


    override suspend fun deleteUserAlert(alertId: String, type: String)
            : Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.deleteUserAlert(alertId, type).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }


    override suspend fun getNearbyUser(latitude: String, longitude: String)
            : Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.getNearbyUser(latitude, longitude).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }
        }


    override suspend fun privacyPolicy()
            : Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.privacyPolicy().apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }

    override suspend fun aboutUs()
            : Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.aboutUs().apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }


    override suspend fun getFaq()
            : Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.getFaq().apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }


    override suspend fun userLogout()
            : Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.userLogout().apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }


    override suspend fun deleteUser(): Flow<NetworkResult<JsonObject>> = flow {
        try {
            apiInterface.deleteUser().apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }

    }

    override suspend fun checkInUserAlert(type: String?): Flow<NetworkResult<JsonObject>> = flow {
        try {
            apiInterface.checkInUserAlert(type).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }

    }


    override suspend fun responseAlert(
        alertId: String?,
        description: String?
    ): Flow<NetworkResult<JsonObject>> = flow {
        try {
            apiInterface.responseAlert(alertId, description).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }

    }

    override suspend fun getNeighbor(): Flow<NetworkResult<JsonObject>> = flow {
        try {
            apiInterface.getNeighbor().apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }

    }

    override suspend fun addNeighbor(createHelpingNeighbor: CreateHelpingNeighbor): Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.addNeighbor(createHelpingNeighbor).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }



    override suspend fun neighborProfileDetails(contactId: String?): Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.neighborProfileDetails(contactId).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }
        }


    override suspend fun neighborProfileBlock(contactId: String?): Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.neighborProfileBlock(contactId).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }
        }



    override suspend fun neighborProfileDelete(contactId: String?): Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.neighborProfileDelete(contactId).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }
        }


    override suspend fun addEmergencyMessage(message: String?): Flow<NetworkResult<JsonObject>> =
        flow {
            try {
                apiInterface.addEmergencyMessage(message).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }

        }


    override suspend fun getEmergencyMessage(): Flow<NetworkResult<JsonObject>> = flow {
        try {
            apiInterface.getEmergencyMessage().apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }

    }

    override suspend fun getUserAddress(): Flow<NetworkResult<JsonObject>> = flow {
        try {
            apiInterface.getUserAddress().apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }
    }


    override suspend fun getEmergencyContact(): Flow<NetworkResult<JsonObject>> = flow {
        try {
            apiInterface.getEmergencyContact().apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }
    }


    override suspend fun getNeighborInviteRequest(): Flow<NetworkResult<JsonObject>> = flow {
        try {
            apiInterface.getNeighborInviteRequest().apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }
    }


    override suspend fun sendEmergencyMessageRequest(): Flow<NetworkResult<JsonObject>> = flow {
        try {
            apiInterface.sendEmergencyMessageRequest().apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }
    }


    override suspend fun addUserAddress(
        type: String?,
        address: String?,
        latitude: String?,
        longitude: String?
    ): Flow<NetworkResult<JsonObject>> = flow {

        try {
            apiInterface.addUserAddress(type, address, latitude, longitude).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }
    }


    override suspend fun deleteAddress(addressId: String?): Flow<NetworkResult<JsonObject>> = flow {
        try {
            apiInterface.deleteAddress(addressId).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }
    }


    override suspend fun getEmergencyContactProfile(contactId: String?): Flow<NetworkResult<JsonObject>> = flow {
        try {
            apiInterface.getEmergencyContactProfile(contactId).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }

    }

    override suspend fun getNotifications(type: String): Flow<NetworkResult<List<AlertModel>>> = flow {
        try {
            val response = apiInterface.getNotification(type)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body["status"].asBoolean) {

                    val data = body["data"].asJsonObject
                    val listKey = if (type == "message") "messageList" else "alertList"
                    val jsonArray = data[listKey]?.asJsonArray ?: JsonArray()

                    val alertList = Gson().fromJson(jsonArray, Array<AlertModel>::class.java).toList()

                    emit(NetworkResult.Success(alertList))

                } else {
                    val errorMsg = body?.get("message")?.asString ?: AppConstant.unKnownError
                    emit(NetworkResult.Error(errorMsg))
                }
            } else {
                val errorJson = response.errorBody()?.string()?.let { JSONObject(it) }
                val errorMessage = errorJson?.optString("message", AppConstant.unKnownError)
                emit(NetworkResult.Error(errorMessage ?: AppConstant.unKnownError))
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error("HttpException: ${e.message()}"))
        } catch (e: IOException) {
            emit(NetworkResult.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(NetworkResult.Error("Unexpected error: ${e.message}"))
        }
    }


    override suspend fun getChatBot(query: String): Flow<NetworkResult<JsonObject>> = flow {
        try {

            val response = apiInterface.getChatBot(query)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.get("status")?.asString == "success") {
                    emit(NetworkResult.Success(body))
                } else {
                    val message = body?.get("response")?.asString ?: "Unknown error"
                    emit(NetworkResult.Error(message))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Something went wrong"
                emit(NetworkResult.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Unexpected error"))
        }
    }

    override suspend fun shareLocation(
        contact_id: String,
        duration: String,
        lat: String,
        long: String
    ): Flow<NetworkResult<JsonObject>> = flow{
        try {
            apiInterface.shareYourLocation(contact_id, duration, lat, long).apply {
                if (isSuccessful) {
                    body()?.let { resp ->
                        if (resp.has("status") && resp.get("status").asBoolean) {
                            emit(NetworkResult.Success(resp))
                        } else {
                            emit(NetworkResult.Error(resp.get("message").asString))
                        }
                    } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                } else {
                    try {
                        val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                        emit(
                            NetworkResult.Error(
                                jsonObj?.getString("message")
                                    ?: AppConstant.unKnownError
                            )
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        emit(NetworkResult.Error(AppConstant.unKnownError))
                    }
                }
            }
        } catch (e: HttpException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: IOException) {
            emit(NetworkResult.Error(e.message ?: ""))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: ""))
        }
    }

    override suspend fun addHealthAlerts(
        alertFor: String,
        alertDuration: String,
        healthAlert: String,
        date: String,
        time: String,
        note: String,
        contact: List<String>?
    ): Flow<NetworkResult<JsonObject>> = flow {

        try {
            val response = apiInterface.addHealthAlert(alertFor, alertDuration, healthAlert, date, time, note, contact)

            if (response.isSuccessful) {
                response.body()?.let { resp ->
                    if (resp.has("status") && resp.get("status").asBoolean) {
                        emit(NetworkResult.Success(resp))
                    } else {
                        emit(
                            NetworkResult.Error(
                                resp.get("message")?.asString ?: AppConstant.unKnownError
                            )
                        )
                    }
                } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
            } else {
                val errorMsg = try {
                    val jsonObj = response.errorBody()?.string()?.let { JSONObject(it) }
                    jsonObj?.getString("message")
                } catch (e: Exception) {
                    null
                }
                emit(NetworkResult.Error(errorMsg ?: AppConstant.unKnownError))
            }

        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: AppConstant.unKnownError))
        }
    }



    override suspend fun addEmergencyContact(createHelpingNeighbor: CreateHelpingNeighbor): Flow<NetworkResult<JsonObject>> = flow {
            try {
                apiInterface.addEmergencyContact(createHelpingNeighbor).apply {
                    if (isSuccessful) {
                        body()?.let { resp ->
                            if (resp.has("status") && resp.get("status").asBoolean) {
                                emit(NetworkResult.Success(resp))
                            } else {
                                emit(NetworkResult.Error(resp.get("message").asString))
                            }
                        } ?: emit(NetworkResult.Error(AppConstant.unKnownError))
                    } else {
                        try {
                            val jsonObj = this.errorBody()?.string()?.let { JSONObject(it) }
                            emit(
                                NetworkResult.Error(
                                    jsonObj?.getString("message")
                                        ?: AppConstant.unKnownError
                                )
                            )
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            emit(NetworkResult.Error(AppConstant.unKnownError))
                        }
                    }
                }
            } catch (e: HttpException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: IOException) {
                emit(NetworkResult.Error(e.message ?: ""))
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: ""))
            }
        }

}