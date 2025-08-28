package com.example.taskmaster.data.repository

import com.example.taskmaster.data.database.Task
import com.example.taskmaster.data.database.TaskDao
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao
) {
    suspend fun insert(task: Task) {
        taskDao.insertTask(task)
    }
    
    suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }
    
    suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }
    
    fun getAll(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }
    
    fun getSortedByDeadline(): Flow<List<Task>> {
        return taskDao.getTasksSortedByDeadline()
    }
    
    fun getSortedByPriority(): Flow<List<Task>> {
        return taskDao.getTasksSortedByPriority()
    }
}
