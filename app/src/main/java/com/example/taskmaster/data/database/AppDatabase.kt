package com.example.taskmaster.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

@Database(
    entities = [Task::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migration from version 1 to 2: change priority from Int to String
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create temporary table with new schema
                db.execSQL("""
                    CREATE TABLE tasks_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        deadline INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
                
                // Copy data from old table to new table, converting priority to string
                db.execSQL("""
                    INSERT INTO tasks_new (id, title, description, priority, deadline, isCompleted, createdAt)
                    SELECT id, title, description, 
                           CASE 
                               WHEN priority = 1 THEN 'High'
                               WHEN priority = 2 THEN 'Medium'
                               WHEN priority = 3 THEN 'Normal'
                               WHEN priority = 4 THEN 'Low'
                               WHEN priority = 5 THEN 'Very Low'
                               ELSE 'Other'
                           END,
                           deadline, isCompleted, createdAt
                    FROM tasks
                """)
                
                // Drop old table and rename new table
                db.execSQL("DROP TABLE tasks")
                db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
            }
        }
        
        // Migration from version 2 to 3: add notificationTime column
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN notificationTime INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
