package com.gantlab.satori.notifications

interface NotificationManager {
    fun scheduleTaskNotification(taskId: Long, title: String, time: String)
    fun cancelTaskNotification(taskId: Long)
    fun scheduleDailyReminder(id: Int, title: String, message: String, hour: Int, minute: Int)
}
