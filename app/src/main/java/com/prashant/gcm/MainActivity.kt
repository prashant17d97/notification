package com.prashant.gcm

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prashant.gcm.ui.theme.GCMTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private var imageUrl: String? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageUrl = intent.getNotificationData()
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            GCMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        imageUrl = imageUrl,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        imageUrl = intent.getNotificationData()
    }
}


private fun Intent.getNotificationData(): String? {
    val imageUrl = getStringExtra("imageUrl")
    val messageText = getStringExtra("messageText")
    val titleText = getStringExtra("titleText")
    Log.d("MainActivity", "onNewIntent: $imageUrl\n$titleText\n$messageText")
    return imageUrl
}

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

@Composable
fun Greeting(name: String, imageUrl: String?, modifier: Modifier = Modifier) {

    var imageBitmap: Bitmap? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(imageUrl) {
        imageBitmap = imageUrl?.let { getBitmapFromUrl(it) }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        imageBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(), contentDescription = "NotificationImage",
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GCMTheme {
        Greeting("Android", "")
    }
}

private fun MainActivity.isNotificationPermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        checkSelfPermission(
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}