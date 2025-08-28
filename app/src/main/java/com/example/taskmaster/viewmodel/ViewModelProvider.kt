package com.example.taskmaster.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.taskmaster.data.database.AppDatabase
import com.example.taskmaster.data.repository.TaskRepository

fun provideTaskViewModelFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val database = AppDatabase.getDatabase(context)
        val taskDao = database.taskDao()
        val repository = TaskRepository(taskDao)
        TaskViewModel(repository, context)
    }
}
