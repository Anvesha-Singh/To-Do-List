package com.example.taskmaster.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.taskmaster.R
import com.example.taskmaster.data.database.Task
import java.util.concurrent.TimeUnit

class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "task_reminders"
        const val CHANNEL_NAME = "Task Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for task deadlines"
        
        const val NOTIFICATION_6_HOURS = 6L
        const val NOTIFICATION_2_HOURS = 2L
        const val NOTIFICATION_15_MIN = 15L
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationHelper", "Notification channel created: $CHANNEL_ID")
        }
    }
    
    fun scheduleTaskReminder(task: Task, hoursBeforeDeadline: Long) {
        if (hoursBeforeDeadline <= 0) {
            Log.d("NotificationHelper", "Not scheduling notification: hoursBeforeDeadline <= 0")
            return
        }
        
        val currentTime = System.currentTimeMillis()
        val notificationTime = if (hoursBeforeDeadline == NOTIFICATION_15_MIN) {
            task.deadline - (15 * 60 * 1000)
        } else {
            task.deadline - (hoursBeforeDeadline * 60 * 60 * 1000)
        }
        
        if (notificationTime <= currentTime) {
            Log.d("NotificationHelper", "Not scheduling notification: notification time is in the past")
            return
        }
        
        val delay = notificationTime - currentTime
        Log.d("NotificationHelper", "Scheduling notification for task ${task.id} in ${delay}ms (${delay/1000/60} minutes)")
        
        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInputData(workDataOf(
                "taskId" to task.id,
                "taskTitle" to task.title,
                "taskDescription" to task.description,
                "hoursBefore" to hoursBeforeDeadline
            ))
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("task_reminder_${task.id}")
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            "task_reminder_${task.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Log.d("NotificationHelper", "Work scheduled successfully for task ${task.id}")
    }

    fun cancelTaskReminder(taskId: Int) {
        Log.d("NotificationHelper", "Cancelling notification for task: $taskId")
        WorkManager.getInstance(context).cancelAllWorkByTag("task_reminder_$taskId")
        Log.d("NotificationHelper", "Work cancelled for task: $taskId")
    }
    
    fun showNotification(task: Task, hoursBeforeDeadline: Long) {
        Log.d("NotificationHelper", "Showing notification for task: ${task.title}")
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        
        val timeText = when (hoursBeforeDeadline) {
            NOTIFICATION_6_HOURS -> "6 hours"
            NOTIFICATION_2_HOURS -> "2 hours"
            NOTIFICATION_15_MIN -> "15 minutes"
            else -> "$hoursBeforeDeadline hours"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Task Reminder: ${task.title}")
            .setContentText("Due in $timeText: ${task.description}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        notificationManager.notify(task.id, notification)
        Log.d("NotificationHelper", "Notification sent with ID: ${task.id}")
    }

    fun showTestNotification() {
        val testTask = Task(
            id = 999,
            title = "Test Task",
            description = "This is a test notification",
            priority = "Test",
            deadline = System.currentTimeMillis() + 60000, // 1 minute from now
            notificationTime = 0
        )
        
        Log.d("NotificationHelper", "Showing test notification")
        showNotification(testTask, NOTIFICATION_15_MIN)
    }
}