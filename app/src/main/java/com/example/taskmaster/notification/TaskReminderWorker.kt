package com.example.taskmaster.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskmaster.data.database.AppDatabase
import com.example.taskmaster.data.database.Task

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val taskId = inputData.getInt("taskId", -1)
        val hoursBefore = inputData.getLong("hoursBefore", 0)
        
        Log.d("TaskReminderWorker", "Worker started for taskId: $taskId, hoursBefore: $hoursBefore")
        
        if (taskId == -1) {
            Log.e("TaskReminderWorker", "Invalid taskId")
            return Result.failure()
        }
        
        val database = AppDatabase.getDatabase(applicationContext)
        val task = database.taskDao().getTaskById(taskId)
        
        if (task == null) {
            Log.d("TaskReminderWorker", "Task not found in database")
            return Result.success()
        }
        
        if (task.isCompleted) {
            Log.d("TaskReminderWorker", "Task is already completed, not showing notification")
            return Result.success()
        }
        
        Log.d("TaskReminderWorker", "Showing notification for task: ${task.title}")
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showNotification(task, hoursBefore)
        
        return Result.success()
    }
}