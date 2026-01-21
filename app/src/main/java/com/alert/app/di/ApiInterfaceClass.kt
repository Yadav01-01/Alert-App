package com.alert.app.di

import com.alert.app.errormessage.ApiEndPoint
import com.alert.app.model.contact.UserContactRequest
import com.alert.app.model.contact.UserEditContactRequest
import com.alert.app.model.helpingneighbormodel.CreateHelpingNeighbor
import com.alert.app.model.selfAlert.CreateSelfAlertRequest
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


/*
The project utilizes a total of no APIs, all of which are functioning correctly.
This file contains only the API endpoints and keys used in the required functions.
*/


interface ApiInterfaceClass {

    @FormUrlEncoded
    @POST(ApiEndPoint.loginUrl)
    suspend fun loginApiRequest(
        @Field("email") email: String?,
        @Field("password") password: String?,
        @Field("fcm_token") token: String?,
        @Field("device_type") deviceType: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.loginPhoneUrl)
    suspend fun loginPhoneApiRequest(
        @Field("phone_number") phone: String?,
        @Field("country_code") countryCode: String?,
        @Field("password") password: String?,
        @Field("fcm_token") token: String?,
        @Field("device_type") deviceType: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.signUpOtpVerifyUrl)
    suspend fun signUpVerifyApiRequest(
        @Field("otp") otp: String?,
        @Field("email") email: String?,
        @Field("phone_number") phoneNumber: String?,
        @Field("fcm_token") token: String?,
        @Field("device_type") deviceType: String?
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.forgotPassVerify)
    suspend fun forGotOtpVerifyRequestApi(
        @Field("email") email: String?,
        @Field("otp") otp: String?,
        @Field("phone_number") phoneNumber: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.resendOtp)
    suspend fun resendOtpApi(
        @Field("type") type: String?,
        @Field("email") email: String?,
        @Field("phone_number") phoneNumber: String?
    ): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.socialLoginUrl)
    suspend fun socialLoginRequestApi(
        @Field("emailOrPhone") emailOrPhone: String?,
        @Field("fcm_token") token: String?,
        @Field("device_type") deviceType: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.sendOtpForVerifyUrl)
    suspend fun sendOtpEmailPhoneRequestApi(
        @Field("emailOrPhone") emailOrPhone: String?
    ): Response<JsonObject>


    @Multipart
    @POST(ApiEndPoint.upDateProfileUrl)
    suspend fun profileUpdateRequestApi(
        @Part("name") name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.signUpEmail)
    suspend fun signupEmailRequestApi(
        @Field("full_name") name: String?,
        @Field("email") email: String?,
        @Field("password") password: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.signUpPhone)
    suspend fun signupPhoneRequestApi(
        @Field("full_name") name: String?,
        @Field("country_code") countryCode: String?,
        @Field("phone_number") phoneNumber: String?,
        @Field("password") password: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.forgotPassword)
    suspend fun forgotPasswordRequestApi(
        @Field("email") email: String?,
        @Field("phone_number") phone: String?
    ): Response<JsonObject>


    @POST(ApiEndPoint.tutorialsVideoUrl)
    suspend fun tutorialsDataRequestApi(): Response<JsonObject>

    @POST(ApiEndPoint.termsConditionUrl)
    suspend fun termsAndConditionRequestApi(): Response<JsonObject>


    @FormUrlEncoded
    @POST(ApiEndPoint.resetPasswordUrl)
    suspend fun reseatPasswordRequestApi(
        @Field("email") email: String?,
        @Field("phone_number") phone: String?,
        @Field("password") password: String?,
        @Field("password_confirmation") password_confirmation: String?
    ): Response<JsonObject>

    @POST(ApiEndPoint.getUserProfileUrl)
    suspend fun getProfileRequestApi(): Response<JsonObject>

    @POST(ApiEndPoint.getContactList)
    suspend fun getContactList(): Response<JsonObject>

    @POST(ApiEndPoint.getRelation)
    suspend fun getRelation(): Response<JsonObject>

    @POST(ApiEndPoint.getAllAlerts)
    suspend fun getAllAlerts(): Response<JsonObject>

    @POST(ApiEndPoint.manualContact)
    suspend fun manualContact(@Body userContactRequest: UserContactRequest): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.deleteContact)
    suspend fun deleteContact(
        @Field("contact_id") contact_id: String?
    ): Response<JsonObject>

    @POST(ApiEndPoint.editManualContact)
    suspend fun editManualContact(@Body userEditContactRequest: UserEditContactRequest): Response<JsonObject>

    @POST(ApiEndPoint.addContact)
    suspend fun addContact(@Body userContactRequest: UserContactRequest): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.getAlert)
    suspend fun getAlert(@Field("contact_id") contactId: String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.getSelfAlerts)
    suspend fun getSelfAlerts(@Field("type") type: String?): Response<JsonObject>

    @POST(ApiEndPoint.addSelfAlert)
    suspend fun addSelfAlert(@Body createSelfAlertRequest: CreateSelfAlertRequest): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.deleteUserAlert)
    suspend fun deleteUserAlert(
        @Field("alert_id") alert_id: String?,
        @Field("type") type: String?
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.getNearbyUser)
    suspend fun getNearbyUser(
        @Field("latitude") latitude: String?,
        @Field("longitude") longitude: String?
    ): Response<JsonObject>

    @POST(ApiEndPoint.privacyPolicy)
    suspend fun privacyPolicy(): Response<JsonObject>

    @POST(ApiEndPoint.aboutUs)
    suspend fun aboutUs(): Response<JsonObject>

    @POST(ApiEndPoint.getFaq)
    suspend fun getFaq(): Response<JsonObject>

    @POST(ApiEndPoint.userLogout)
    suspend fun userLogout(): Response<JsonObject>

    @POST(ApiEndPoint.deleteUser)
    suspend fun deleteUser(): Response<JsonObject>


    /// Dhananjay Singh has integrated the API from here.
    @FormUrlEncoded
    @POST(ApiEndPoint.checkInUserAlert)
    suspend fun checkInUserAlert(@Field("type") type: String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.responseAlert)
    suspend fun responseAlert(
        @Field("alert_id") alertId: String?,
        @Field("description") description: String?
    ): Response<JsonObject>


    @POST(ApiEndPoint.getNeighbor)
    suspend fun getNeighbor(): Response<JsonObject>

    @POST(ApiEndPoint.addNeighbor)
    suspend fun addNeighbor(@Body createHelpingNeighbor: CreateHelpingNeighbor): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.neighborProfileDetails)
    suspend fun neighborProfileDetails(@Field("contact_id") contactId: String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.blockNeighbor)
    suspend fun neighborProfileBlock(@Field("contact_id") addressId:String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.deleteNeighbor)
    suspend fun neighborProfileDelete(@Field("contact_id") addressId:String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.addEmergencyMessage)
    suspend fun addEmergencyMessage(@Field("message") message: String?): Response<JsonObject>


    @GET(ApiEndPoint.addEmergencyMessage)
    suspend fun getEmergencyMessage(): Response<JsonObject>

    @GET(ApiEndPoint.addUserAddress)
    suspend fun getUserAddress(): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.addUserAddress)
    suspend fun addUserAddress(@Field("type") type:String?, @Field("address") address:String?,
                               @Field("latitude") latitude:String?,@Field("longitude") longitude:String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.deleteAddress)
    suspend fun deleteAddress(@Field("address_id") addressId:String?): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.addEmergencyContact)
    suspend fun addEmergencyContact(@Body createHelpingNeighbor: CreateHelpingNeighbor): Response<JsonObject>

    @POST(ApiEndPoint.getEmergencyContact)
    suspend fun getEmergencyContact(): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.getEmergencyContactProfile)
    suspend fun getEmergencyContactProfile(@Field("contact_id") contactId:String?): Response<JsonObject>


    @POST(ApiEndPoint.getNeighborInviteRequest)
    suspend fun getNeighborInviteRequest(): Response<JsonObject>

    @POST(ApiEndPoint.sendEmergencyMessageRequest)
    suspend fun sendEmergencyMessageRequest(): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.GET_NOTIFICATION)
    suspend fun getNotification(@Field("type") type:String): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.ASK_GPT)
    suspend fun getChatBot(@Field("query") query:String): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.SHARE_LOCATION)
    suspend fun shareYourLocation(@Field("contact_id") contact_id:String,
                                  @Field("duration") duration : String,
                                  @Field("lat") lat:String,
                                  @Field("long") long:String): Response<JsonObject>

    @FormUrlEncoded
    @POST(ApiEndPoint.ADD_HEALTH_ALERT)
    suspend fun addHealthAlert(
        @Field("alertFor") alertFor: String,
        @Field("alertDuration") alertDuration: String,
        @Field("healthAlert") healthAlert: String,
        @Field("start_date") startDate: String,
        @Field("end_date") endDate: String,
        @Field("time") time: String,
        @Field("note") note: String,
        @Field("contact[]") contact: List<String>?
    ): Response<JsonObject>



}