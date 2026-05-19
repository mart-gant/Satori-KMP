package com.gantlab.satori.notifications

@Suppress("unused")
class DummyNotificationManager : NotificationManager {
    override fun scheduleTaskNotification(taskId: Long, title: String, time: String) {
        println("DUMMY NOTIFICATION: Scheduled '$title' at $time for task $taskId")
    }

    override fun cancelTaskNotification(taskId: Long) {
        println("DUMMY NOTIFICATION: Cancelled for task $taskId")
    }

    override fun scheduleDailyReminder(id: Int, title: String, message: String, hour: Int, minute: Int) {
        println("DUMMY NOTIFICATION: Daily reminder '$title' scheduled for $hour:$minute")
    }
}
