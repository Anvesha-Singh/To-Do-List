package com.example.taskmaster.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: Task)
    
    @Update
    suspend fun updateTask(task: Task)
    
    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    fun getTasksSortedByDeadline(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks ORDER BY priority ASC") // Changed to ASC for alphabetical sorting of categories
    fun getTasksSortedByPriority(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?
}
