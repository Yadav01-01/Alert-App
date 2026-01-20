package com.alert.app.commonworkutils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.alert.app.R
import com.alert.app.base.AppConstant
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CommonWorkUtils(var context: Context) {

    private var dialog: Dialog? = null
    private val dialog1: Dialog? = null
    var pref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    init{
        pref=context.getSharedPreferences(AppConstant.LOGIN_SESSION, Context.MODE_PRIVATE)
        editor=pref?.edit()
    }


    fun hideStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT < 16) {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            val decorView = activity.window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
        }
    }

    fun getDateFormat(dateFor: String?): String? {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        var date: Date? = null //new Date();
        try {
            date = formatter.parse(dateFor)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val dt1 = SimpleDateFormat("yyyy-MM-dd")
        return dt1.format(date)
    }



    @SuppressLint("MissingInflatedId", "LocalSuppress")
    fun alertDialog(context: Context?, error: String?, finish: Boolean) {
        if (context != null) {
            val inflator = LayoutInflater.from(context)
            val dialogView: View = inflator.inflate(R.layout.alert_box_error, null)
            val alert = Dialog(context, R.style.BottomSheetDialog)
            alert.setCancelable(false)
            alert.setContentView(dialogView)
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(alert.window!!.attributes)
            //            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            alert.window!!.attributes = layoutParams
            val tvMessage = dialogView.findViewById<TextView>(R.id.tv_text) as TextView
            tvMessage.text = error
            val tvOk = dialogView.findViewById<RelativeLayout>(R.id.btn_okay)
            tvOk.setOnClickListener { view: View? -> alert.dismiss() }
            alert.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alert.show()
        }
    }
    fun OnlyEditDate(dt: String?): String? {
        var outputDateStr = ""
        val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val inputFormat1: DateFormat = SimpleDateFormat("MM/dd/yyyy")
        //        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
        val outputFormat: DateFormat = SimpleDateFormat("MM/dd/yyyy")
        // String inputDateStr="2013-06-24";
        var date: Date? = null
        try {
            date = inputFormat.parse(dt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        outputDateStr = outputFormat.format(date)
        return outputDateStr
    }



    fun onlyWeekName(dt: String?): String? {
        var outputDateStr = ""
        val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        //        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
        val outputFormat: DateFormat = SimpleDateFormat("EEEE")
        // String inputDateStr="2013-06-24";
        var date: Date? = null
        try {
            date = inputFormat.parse(dt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        outputDateStr = outputFormat.format(date)
        return outputDateStr
    }

    fun onlyYYYYNameAndDate1(dt: String?): String? {
        var outputDateStr = ""
        val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        //        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
        val outputFormat: DateFormat = SimpleDateFormat("yyyy")
        // String inputDateStr="2013-06-24";
        var date: Date? = null
        try {
            date = inputFormat.parse(dt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        outputDateStr = outputFormat.format(date)
        return outputDateStr
    }


    fun onlyMMMNameAndDate1(dt: String?): String? {
        var outputDateStr = ""
        val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        //        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
        val outputFormat: DateFormat = SimpleDateFormat("MM")
        // String inputDateStr="2013-06-24";
        var date: Date? = null
        try {
            date = inputFormat.parse(dt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        outputDateStr = outputFormat.format(date)
        return outputDateStr
    }

    fun onlyDDameAndDate1(dt: String?): String? {
        var outputDateStr = ""
        val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        //        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
        val outputFormat: DateFormat = SimpleDateFormat("dd")
        // String inputDateStr="2013-06-24";
        var date: Date? = null
        try {
            date = inputFormat.parse(dt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        outputDateStr = outputFormat.format(date)
        return outputDateStr
    }

    fun onlyDate(dt: String?): String? {
        var outputDateStr = ""
        val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        //        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
        val outputFormat: DateFormat = SimpleDateFormat("dd")
        // String inputDateStr="2013-06-24";
        var date: Date? = null
        try {
            date = inputFormat.parse(dt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        outputDateStr = outputFormat.format(date)
        return outputDateStr
    }


    fun getAddress(context: Context,lat: Double, longi: Double): String {
        var address = ""
        try {
            val geocoder: Geocoder
            val addresses: List<Address>?
            geocoder = Geocoder(context, Locale.getDefault())
            addresses = geocoder.getFromLocation(
                lat,
                longi,
                1
            ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses!![0].getAddressLine(0)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return address
    }


    fun isOnline(context: Context?): Boolean {
        if (context != null) {
            val connectivity =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivity != null) {
                val info = connectivity.allNetworkInfo
                if (info != null) for (networkInfo in info) if (networkInfo.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
        }
        return false
    }




    fun currentDateFormat(): String? {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        return formatter.format(date.time)
    }



    fun snackBarUsing(context: Context, view: View?, msg: String?,status:Boolean) {
        if (status){
            val snackBar = Snackbar.make(view!!, msg!!, Snackbar.LENGTH_INDEFINITE)
                .setAction("Settings") { view1: View? ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
                }

            val snackBarView = snackBar.view
            // Change the background color
            snackBarView.setBackgroundColor(Color.parseColor("#FFFFFF"))
            val snackbarTextView = snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            // Center the text
            snackbarTextView.gravity = Gravity.CENTER
            snackbarTextView.setTextColor(Color.BLACK)
            snackbarTextView.maxLines = 8
            snackBar.show()
        }else{
            val snackbar = Snackbar.make(view!!, msg!!, Snackbar.LENGTH_SHORT)
            val snackbarView = snackbar.view
            // Change the background color
            snackbarView.setBackgroundColor(Color.parseColor("#FFFFFF"))
            val snackbarTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            // Center the text
            snackbarTextView.gravity = Gravity.CENTER
            snackbarTextView.setTextColor(Color.BLACK)
            snackbarTextView.maxLines = 8
            snackbar.show()
        }
    }



     fun getPath(context: Context?, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return context?.let { getDataColumn(it, contentUri, null, null) }

            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return context?.let { getDataColumn(it, contentUri, selection, selectionArgs) }
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else context?.let {
                getDataColumn(
                    it, uri, null, null
                )
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
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


}