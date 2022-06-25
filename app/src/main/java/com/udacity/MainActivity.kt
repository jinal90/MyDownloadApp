package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var downloadManager: DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {

            when (rg_downloadOptions.checkedRadioButtonId) {
                R.id.rb_Glide -> download(URL_GLIDE)
                R.id.rb_LoadApp -> download(URL_UDACITY_LOADAPP)
                R.id.rb_Retrofit -> download(URL_RETROFIT)
                else -> Toast.makeText(this, "Please select a file to download", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        createNotificationChannel()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            var downloadFileName = ""
            if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                intent.extras?.let {

                    val q = DownloadManager.Query()
                    q.setFilterById(it.getLong(DownloadManager.EXTRA_DOWNLOAD_ID))
                    val c: Cursor = downloadManager.query(q)

                    if (c.moveToFirst()) {
                        val status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            // process download
                            val uri = c.getString(c.getColumnIndex("uri"))
                            // get other required data by changing the constant passed to getColumnIndex

                            downloadFileName = getFileName(uri)
                            Log.d("LoadApp", "Download success: $downloadFileName")
                            context?.let { context ->
                                sendNotification(context, downloadFileName, "success")
                            }

                            Toast.makeText(context, "Download success!", Toast.LENGTH_SHORT).show()
                        } else {
                            context?.let { context ->
                                sendNotification(context, downloadFileName, "failure")
                            }

                            Log.d("LoadApp", "Download failed.")
                            Toast.makeText(context, "Download failed!", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }
        }
    }

    fun sendNotification(context: Context, downloadFileName: String, status: String) {
        val date = Date()
        val notificationID = SimpleDateFormat("ddHHmmss", Locale.US).format(date).toInt()

        val detailActivityIntent = Intent(context, DetailActivity::class.java).apply {
            putExtra("downloadFileName", downloadFileName)
            putExtra("status", status)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                notificationID,
                detailActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_cloud_download_24)
                .setContentTitle(getString(R.string.app_name))

                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(
                    R.drawable.ic_baseline_cloud_download_24, "Check Status",
                    pendingIntent
                )

        if (status == "success") {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Repository downloaded successfully!")
            )
        } else {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Repository download Failed!")
            )
        }

        with(NotificationManagerCompat.from(context)) {
            notify(notificationID, builder.build())
        }
    }

    fun getFileName(uri: String): String {
        when (uri) {
            URL_GLIDE -> return "Glide - Image Loading Library by BumpTech."
            URL_UDACITY_LOADAPP -> return "LoadApp - Current Repository by Udacity."
            URL_RETROFIT -> return "Retrofit - Type-safe HTTP client for Android and Java by Square, Inc."
        }
        return ""
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "$CHANNEL_ID"
            val descriptionText = "Channel for show download complete status notification."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        downloadID = downloadManager.enqueue(request)
    }

    companion object {
        private const val URL_GLIDE =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val URL_UDACITY_LOADAPP =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/refs/heads/master.zip"
        private const val URL_RETROFIT =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"

        private const val CHANNEL_ID = "channelO1"
    }

}
