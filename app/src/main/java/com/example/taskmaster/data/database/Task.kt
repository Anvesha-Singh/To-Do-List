package com.example.taskmaster.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String, // Changed from Int to String to store category names
    val deadline: Long,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val notificationTime: Long = 0 // 0 = no notification, 6 = 6 hours, 2 = 2 hours, 0.25 = 15 minutes
)
