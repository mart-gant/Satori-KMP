package com.gantlab.satori

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.gantlab.satori.settings.SettingsManager
import com.gantlab.satori.ui.Routes
import com.gantlab.satori.worker.SyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val settings = SettingsManager()
        getPlatform().setLanguage(settings.language)

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "SatoriSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        val initialRoute = when (intent?.getStringExtra("navigate_to")) {
            "tips" -> Routes.TIPS
            else -> null
        }

        setContent {
            App(initialRoute = initialRoute)
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
