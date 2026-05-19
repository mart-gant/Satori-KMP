package com.gantlab.satori.notifications

import platform.UserNotifications.*
import platform.Foundation.*

class IosNotificationManager : NotificationManager {

    init {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound
        ) { granted, error ->
            if (granted) {
                println("iOS Notifications: Permission granted")
            } else {
                println("iOS Notifications: Permission denied: ${error?.localizedDescription}")
            }
        }
    }

    override fun scheduleTaskNotification(taskId: Long, title: String, time: String) {
        val parts = time.split(":")
        if (parts.size != 2) return

        val hour = parts[0].toLongOrNull() ?: return
        val minute = parts[1].toLongOrNull() ?: return

        val content = UNMutableNotificationContent().apply {
            setTitle("Czas na zadanie!")
            setBody(title)
            setSound(UNNotificationSound.defaultSound())
        }

        val dateComponents = NSDateComponents().apply {
            setHour(hour)
            setMinute(minute)
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents, repeats = true)
        val request = UNNotificationRequest.requestWithIdentifier(
            taskId.toString(),
            content,
            trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("iOS Notifications: Error scheduling: ${error.localizedDescription}")
            }
        }
    }

    override fun cancelTaskNotification(taskId: Long) {
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(listOf(taskId.toString()))
    }

    override fun scheduleDailyReminder(id: Int, title: String, message: String, hour: Int, minute: Int) {
        // Implementation stub
    }
}
