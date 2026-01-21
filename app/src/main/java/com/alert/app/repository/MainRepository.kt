package com.alert.app.repository


import com.alert.app.di.NetworkResult
import com.alert.app.model.contact.UserContactRequest
import com.alert.app.model.contact.UserEditContactRequest
import com.alert.app.model.helpingneighbormodel.CreateHelpingNeighbor
import com.alert.app.model.notification.AlertModel
import com.alert.app.model.selfAlert.CreateSelfAlertRequest
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody


interface MainRepository {


    suspend fun loginRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        email: String,
        password: String,
        token: String,
        deviceType: String,
    )

    suspend fun loginPhoneRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        phone: String,
        countryCode: String,
        password: String,
        token: String,
        deviceType: String,
    )

    suspend fun resendOtp(
        successCallback: (response: NetworkResult<String>) -> Unit,
        type: String,
        email: String?,
        phoneNumber: String?
    )

    suspend fun getProfileRequestApi(successCallback: (response: NetworkResult<String>) -> Unit)


    suspend fun signupOtpVerifyRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        otp: String,
        email: String?,
        phoneNumber: String?,
        token: String,
        deviceType: String,
    )

    suspend fun forGotOtpVerifyRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        email: String?,
        otp: String,
        phoneNumber :String?
    )

    suspend fun socialLoginRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String,
        deviceType: String,
        token: String
    )

    suspend fun sendOtpEmailPhoneRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        emailOrPhone: String
    )

    suspend fun profileUpdateRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        cusName: RequestBody,
        cusEmail: RequestBody,
        cusPhone: RequestBody,
        cusAddress: RequestBody,
        cusLatitude: RequestBody,
        cusLongitude: RequestBody,
        requestImage: MultipartBody.Part?
    )

    suspend fun signupEmailRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        name: String,
        email: String,
        password: String
    )

    suspend fun signupPhoneRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        name: String,
        countryCode: String,
        phoneNumber: String,
        password: String
    )

    suspend fun forgotPasswordRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        email: String?,
        phone: String?
    )

    suspend fun tutorialsDataRequestApi(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun termsAndConditionRequestApi(successCallback: (response: NetworkResult<String>) -> Unit)

    suspend fun reseatPasswordRequestApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        email: String?,
        phone: String?,
        password: String,
        password_confirmation: String
    )

    suspend fun getContactList() : Flow<NetworkResult<JsonObject>>

    suspend fun getRelation(): Flow<NetworkResult<JsonObject>>

    suspend fun getAllAlerts(): Flow<NetworkResult<JsonObject>>

    suspend fun manualContact(userRequest: UserContactRequest): Flow<NetworkResult<JsonObject>>

    suspend fun deleteContact(contact_id: String): Flow<NetworkResult<JsonObject>>

    suspend fun editManualContact(userEditContactRequest: UserEditContactRequest): Flow<NetworkResult<JsonObject>>

    suspend fun addContact(userRequest: UserContactRequest): Flow<NetworkResult<JsonObject>>

    suspend fun getAlert(contactId: String): Flow<NetworkResult<JsonObject>>

    suspend fun getSelfAlerts(type: String?): Flow<NetworkResult<JsonObject>>

    suspend fun addSelfAlert(createSelfAlertRequest: CreateSelfAlertRequest): Flow<NetworkResult<JsonObject>>

    suspend fun deleteUserAlert(alertId: String,type: String): Flow<NetworkResult<JsonObject>>

    suspend fun getNearbyUser(latitude: String,longitude: String): Flow<NetworkResult<JsonObject>>

    suspend fun privacyPolicy(): Flow<NetworkResult<JsonObject>>

    suspend fun aboutUs(): Flow<NetworkResult<JsonObject>>

    suspend fun getFaq(): Flow<NetworkResult<JsonObject>>

    suspend fun userLogout(): Flow<NetworkResult<JsonObject>>

    suspend fun deleteUser(): Flow<NetworkResult<JsonObject>>

    suspend fun checkInUserAlert(type: String?): Flow<NetworkResult<JsonObject>>
    suspend fun responseAlert(alertId: String?,description:String?): Flow<NetworkResult<JsonObject>>

    suspend fun getNeighbor(): Flow<NetworkResult<JsonObject>>

    suspend fun addNeighbor(createHelpingNeighbor: CreateHelpingNeighbor): Flow<NetworkResult<JsonObject>>

    suspend fun neighborProfileDetails(contactId: String?): Flow<NetworkResult<JsonObject>>
    suspend fun neighborProfileBlock(contactId: String?): Flow<NetworkResult<JsonObject>>
    suspend fun neighborProfileDelete(contactId: String?): Flow<NetworkResult<JsonObject>>
    suspend fun addEmergencyMessage(message: String?): Flow<NetworkResult<JsonObject>>
    suspend fun getEmergencyMessage(): Flow<NetworkResult<JsonObject>>
    suspend fun getUserAddress(): Flow<NetworkResult<JsonObject>>
    suspend fun addUserAddress(type: String?, address: String?, latitude: String?, longitude: String?): Flow<NetworkResult<JsonObject>>
    suspend fun deleteAddress(addressID: String?): Flow<NetworkResult<JsonObject>>
    suspend fun addEmergencyContact(createHelpingNeighbor: CreateHelpingNeighbor): Flow<NetworkResult<JsonObject>>
    suspend fun getEmergencyContact(): Flow<NetworkResult<JsonObject>>
    suspend fun getNeighborInviteRequest(): Flow<NetworkResult<JsonObject>>
    suspend fun sendEmergencyMessageRequest(): Flow<NetworkResult<JsonObject>>

    suspend fun getEmergencyContactProfile(contactId: String?): Flow<NetworkResult<JsonObject>>

    suspend fun getNotifications(type: String): Flow<NetworkResult<List<AlertModel>>>

    suspend fun getChatBot(query: String): Flow<NetworkResult<JsonObject>>

    suspend fun shareLocation(contact_id: String, duration: String, lat: String, long: String): Flow<NetworkResult<JsonObject>>

    suspend fun addHealthAlerts(alertFor: String,alertDuration: String,healthAlert: String,
                                startDate: String,endDate : String,time: String, note: String,contact: List<String>?) : Flow<NetworkResult<JsonObject>>

}