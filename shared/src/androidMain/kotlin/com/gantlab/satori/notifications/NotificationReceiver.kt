package com.gantlab.satori.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.app.NotificationManager as SystemNotificationManager

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("taskId", -1)
        val title = intent.getStringExtra("title") ?: "Zadanie z rutyny"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as SystemNotificationManager
        
        val notification = NotificationCompat.Builder(context, "ROUTINE_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Używamy systemowej ikony dla uproszczenia
            .setContentTitle("Czas na zadanie!")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }
}
