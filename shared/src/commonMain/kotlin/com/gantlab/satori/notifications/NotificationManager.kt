package com.gantlab.satori.notifications

interface NotificationManager {
    fun scheduleTaskNotification(taskId: Long, title: String, time: String)
    fun cancelTaskNotification(taskId: Long)
}
