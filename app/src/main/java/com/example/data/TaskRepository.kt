package com.example.data

import android.content.Context
import com.example.reminder.ReminderScheduler
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val context: Context
) {
    private val reminderScheduler = ReminderScheduler(context)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val taskListType = Types.newParameterizedType(List::class.java, TaskEntity::class.java)
    private val taskListAdapter = moshi.adapter<List<TaskEntity>>(taskListType)

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = taskDao.getPendingTasks()

    fun getTaskById(id: Int): Flow<TaskEntity?> = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long {
        val id = taskDao.insertTask(task)
        val savedTask = task.copy(id = id.toInt())
        if (savedTask.reminderEpochMs != null) {
            reminderScheduler.scheduleReminder(savedTask)
        }
        return id
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
        if (task.isCompleted || task.reminderEpochMs == null) {
            reminderScheduler.cancelReminder(task.id)
        } else {
            reminderScheduler.scheduleReminder(task)
        }
    }

    suspend fun deleteTask(task: TaskEntity) {
        reminderScheduler.cancelReminder(task.id)
        taskDao.deleteTask(task)
    }

    suspend fun toggleTaskCompleted(task: TaskEntity) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        updateTask(updated)
    }

    suspend fun toggleTaskPinned(task: TaskEntity) {
        val updated = task.copy(isPinned = !task.isPinned)
        updateTask(updated)
    }

    suspend fun deleteCompletedTasks() {
        taskDao.deleteCompletedTasks()
    }

    // Export & Import for offline privacy guarantee
    suspend fun exportTasksToJson(tasks: List<TaskEntity>): String {
        return try {
            taskListAdapter.toJson(tasks)
        } catch (e: Exception) {
            "[]"
        }
    }

    suspend fun importTasksFromJson(jsonString: String): Int {
        return try {
            val importedList = taskListAdapter.fromJson(jsonString) ?: emptyList()
            var count = 0
            for (task in importedList) {
                val cleanTask = task.copy(id = 0) // New row
                taskDao.insertTask(cleanTask)
                count++
            }
            count
        } catch (e: Exception) {
            0
        }
    }
}
