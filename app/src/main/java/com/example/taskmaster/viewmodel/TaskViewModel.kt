package com.example.taskmaster.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.data.database.Task
import com.example.taskmaster.data.repository.TaskRepository
import com.example.taskmaster.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository,
    private val context: Context
) : ViewModel() {
    
    private val notificationHelper = NotificationHelper(context)
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    
    private val _sortedByDeadline = MutableStateFlow<List<Task>>(emptyList())
    val sortedByDeadline: StateFlow<List<Task>> = _sortedByDeadline.asStateFlow()
    
    private val _sortedByPriority = MutableStateFlow<List<Task>>(emptyList())
    val sortedByPriority: StateFlow<List<Task>> = _sortedByPriority.asStateFlow()
    
    init {
        loadTasks()
        loadSortedByDeadline()
        loadSortedByPriority()
    }
    
    private fun loadTasks() {
        viewModelScope.launch {
            repository.getAll().collect { taskList ->
                _tasks.value = taskList
            }
        }
    }
    
    private fun loadSortedByDeadline() {
        viewModelScope.launch {
            repository.getSortedByDeadline().collect { taskList ->
                _sortedByDeadline.value = taskList
            }
        }
    }
    
    private fun loadSortedByPriority() {
        viewModelScope.launch {
            repository.getSortedByPriority().collect { taskList ->
                _sortedByPriority.value = taskList
            }
        }
    }
    
    fun insertTask(task: Task) {
        viewModelScope.launch {
            repository.insert(task)
            
            // Schedule notification if selected
            if (task.notificationTime > 0) {
                notificationHelper.scheduleTaskReminder(task, task.notificationTime)
            }
        }
    }
    
    fun updateTask(task: Task) {
        viewModelScope.launch {
            // Cancel existing notification if any
            val existingTask = _tasks.value.find { it.id == task.id }
            if (existingTask?.notificationTime ?: 0 > 0) {
                notificationHelper.cancelTaskReminder(task.id)
            }
            
            repository.update(task)
            
            // Schedule new notification if selected
            if (task.notificationTime > 0) {
                notificationHelper.scheduleTaskReminder(task, task.notificationTime)
            }
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            // Cancel notification if any
            if (task.notificationTime > 0) {
                notificationHelper.cancelTaskReminder(task.id)
            }
            
            repository.delete(task)
        }
    }
    
    fun refreshTasks() {
        loadTasks()
        loadSortedByDeadline()
        loadSortedByPriority()
    }
}
