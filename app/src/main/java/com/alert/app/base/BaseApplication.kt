package com.alert.app.base

import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.alert.app.R
import com.alert.app.activity.AuthActivity
import com.alert.app.errormessage.MessageClass
import java.text.SimpleDateFormat
import java.util.Locale

object BaseApplication {

    private var dialog: Dialog? = null

    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date!!)
    }


    fun logFormattedTime(apiResponseTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(apiResponseTime)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            "" // return empty string if parsing fails
        }
    }



    fun getTimeAgoText(time: String): String {
        val parts = time.split(":")
        if (parts.size != 2) return ""

        val hour = parts[0].toIntOrNull() ?: 0
        val min = parts[1].toIntOrNull() ?: 0

        return when {
            hour > 0 && min > 0 -> "${hour} hr${if (hour > 1) "s" else ""} ${min} mins ago"
            hour > 0 -> "${hour} hr${if (hour > 1) "s" else ""} ago"
            min > 0 -> "${min} mins ago"
            else -> "just now"
        }
    }



    fun splitDateTime(apiDateTime: String): Pair<String, String> {
        // Input format from API
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Desired output formats
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        return try {
            val date = inputFormat.parse(apiDateTime)
            val formattedDate = dateFormat.format(date)
            val formattedTime = timeFormat.format(date)
            Pair(formattedDate, formattedTime)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("", "")
        }
    }


    fun alertError(context: Context?, msg: String?, status: Boolean) {

        if (context == null) return

        val activity = context as? Activity ?: return

        // 🚨 MOST IMPORTANT CHECKS
        if (activity.isFinishing || activity.isDestroyed) return

        activity.runOnUiThread {

            if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread

            val dialog = Dialog(activity, R.style.BottomSheetDialog)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.alert_box_error)

            dialog.window?.let { window ->
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.copyFrom(window.attributes)
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
                window.attributes = layoutParams
            }

            val tvTitle: TextView = dialog.findViewById(R.id.tv_text)
            val btnOk: RelativeLayout = dialog.findViewById(R.id.btn_okay)
            val ltIcon: LottieAnimationView = dialog.findViewById(R.id.lt_javrvis)

            if (msg == MessageClass.networkError) {
                ltIcon.setAnimation(R.raw.dotloader)
            } else {
                ltIcon.setAnimation(R.raw.loader)
            }

            tvTitle.text = msg ?: ""

            btnOk.setOnClickListener {
                dialog.dismiss()

                if (status) {
                    val sessionManagement = SessionManagement(activity)
                    sessionManagement.logOut()

                    val intent = Intent(activity, AuthActivity::class.java)
                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                    intent.putExtra("openScreen", "Login")
                    activity.startActivity(intent)
                }
            }

            // ✅ FINAL SAFETY CHECK
            if (!activity.isFinishing && !activity.isDestroyed) {
                dialog.show()
            }
        }
    }




    /*    fun  alertError(context: Context?, msg:String?,status:Boolean){
        val dialog= context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.alert_box_error)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog?.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = layoutParams
        val tvTitle: TextView =dialog.findViewById(R.id.tv_text)
        val btnOk: RelativeLayout =dialog.findViewById(R.id.btn_okay)
        val ltIcon: LottieAnimationView =dialog.findViewById(R.id.lt_javrvis)
        if (msg.equals(MessageClass.networkError)){
            ltIcon.setAnimation(R.raw.dotloader)
        }else{
            ltIcon.setAnimation(R.raw.loader)
        }
        tvTitle.text=msg

        btnOk.setOnClickListener {
            if (status){
                dialog.dismiss()
                val sessionManagement= SessionManagement(context)
                sessionManagement.logOut()
                val intent =Intent(context, AuthActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent. putExtra("openScreen", "Login")
                context.startActivity(intent)
            }else{
                dialog.dismiss()
            }

        }
        dialog.show()
    }*/


    fun getPath(context: Context?, uri: Uri?): String? {
        var uri = uri
        val needToCheckUri = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(
                context!!.applicationContext,
                uri
            )
        ) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong()
                )
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]
                if ("image" == type) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
        if ("content".equals(uri!!.scheme, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor?
            try {
                cursor =
                    context!!.contentResolver.query(
                        uri,
                        projection,
                        selection,
                        selectionArgs,
                        null
                    )
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri?): Boolean {
        return "com.android.externalstorage.documents" == uri!!.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri?): Boolean {
        return "com.android.providers.downloads.documents" == uri!!.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri?): Boolean {
        return "com.android.providers.media.documents" == uri!!.authority
    }

    fun cantactValidationError(requireContext: Context, edFullName: EditText, edLastName: EditText, edEmail: EditText,
                               edPhone: EditText, selectedAlertId:Int, selectedRelationId:Int): Boolean {

        if (edFullName.text.toString().trim().isEmpty()){
            alertError(requireContext, MessageClass.fullnameError,false)
            return false
        }else if (edLastName.text.toString().trim().isEmpty()){
            alertError(requireContext, MessageClass.lastError,false)
            return false
        }else if (edEmail.text.toString().trim().isEmpty()){
            alertError(requireContext, MessageClass.emailError,false)
            return false
        }else if (edPhone.text.toString().trim().isEmpty()){
            alertError(requireContext, MessageClass.phoneError,false)
            return false
        }
        else if (selectedRelationId==-1){
            alertError(requireContext, MessageClass.relation,false)
            return false
        }
        else if (selectedAlertId==-1){
            alertError(requireContext, MessageClass.alert,false)
            return false
        }

        return true
    }

    fun alertBox(context: Context?){
        dialog = context?.let { Dialog(it, R.style.CustomProgressBarTheme) }
        dialog?.setContentView(R.layout.progress_dialog)
        dialog?.setCancelable(true)
        dialog?.window?.setDimAmount(0f)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun openDialog(){
        if (!dialog?.isShowing!!) {
            dialog?.show()
        }
    }

    fun dismissDialog(){
        dialog?.dismiss()
    }

    fun isOnline(context: Context?): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true // Fast
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                when {
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                            networkCapabilities.linkDownstreamBandwidthKbps > 2000 -> true // Assume good speed
                    else -> false
                }
            }
            else -> false
        }
    }



}