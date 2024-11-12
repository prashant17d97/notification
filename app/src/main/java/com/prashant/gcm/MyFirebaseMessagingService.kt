package com.prashant.gcm


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder
    private val channelId = "com.prashant.gcm"
    private val description = "GCMDemo"

    override fun onNewToken(token: String) {
        Log.d("MyFirebaseMessagingService", "New_Token:--> $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(
            "MyFirebaseMessagingService",
            "onMessageReceived: ${remoteMessage.notification!!.body}"
        )
        
        // Initialize NotificationManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val imageUrl = remoteMessage.data["imageUrl"]
        val messageText = remoteMessage.notification?.body
        val titleText = remoteMessage.notification?.title

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("imageUrl", imageUrl)
            putExtra("messageText", messageText)
            putExtra("titleText", titleText)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Fetch the image in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = imageUrl?.let { getBitmapFromUrl(it) }

            // Build notification with or without image
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel =
                    NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.GREEN
                notificationChannel.enableVibration(false)
                notificationManager.createNotificationChannel(notificationChannel)

                builder = Notification.Builder(this@MyFirebaseMessagingService, channelId)
                    .setContentTitle(remoteMessage.notification!!.title)
                    .setContentText(remoteMessage.notification!!.body)
                    .setSmallIcon(R.drawable.android)
                    .setContentIntent(pendingIntent)

                // Set the image as largeIcon if it exists
                bitmap?.let {
                    builder.setLargeIcon(it)
                }

            } else {
                builder = Notification.Builder(this@MyFirebaseMessagingService)
                    .setContentTitle(remoteMessage.notification!!.title)
                    .setContentText(remoteMessage.notification!!.body)
                    .setSmallIcon(R.drawable.android)
                    .setContentIntent(pendingIntent)

                // Set the image as largeIcon if it exists
                bitmap?.let {
                    builder.setLargeIcon(it)
                }
            }

            // Show the notification
            notificationManager.notify(1234, builder.build())
        }
    }

    // Function to download image and convert it to Bitmap
    private suspend fun getBitmapFromUrl(imageUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl);
                BitmapFactory.decodeStream(url.openConnection().getInputStream())
            } catch (e: IOException) {
                Log.e("MyFirebaseMessagingService", "getBitmapFromUrl: ${e.localizedMessage}")
                null
            }
        }
    }
}