package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

enum class TaskPriority(val displayName: String, val level: Int) {
    ALTA("Alta", 3),
    MEDIA("Media", 2),
    BAJA("Baja", 1)
}

enum class TaskCategory(val displayName: String, val iconName: String) {
    TODAS("Todas", "FormatListBulleted"),
    CITAS("Citas & Eventos", "Event"),
    PERSONAL("Personal", "Person"),
    TRABAJO("Trabajo", "Work"),
    SALUD("Salud", "Favorite"),
    FINANZAS("Finanzas", "AttachMoney"),
    ESTUDIO("Estudio", "School"),
    PROYECTO("Proyecto", "RocketLaunch"),
    HOGAR("Hogar", "Home")
}

@JsonClass(generateAdapter = true)
data class Subtask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: String = TaskCategory.PERSONAL.name,
    val priority: String = TaskPriority.MEDIA.name,
    val dueDateEpochMs: Long? = null,
    val dueTimeFormatted: String? = null, // e.g., "14:30"
    val reminderEpochMs: Long? = null,
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val subtasksJson: String = "[]",
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val tags: String = ""
) {
    companion object {
        private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        private val listType = Types.newParameterizedType(List::class.java, Subtask::class.java)
        private val adapter = moshi.adapter<List<Subtask>>(listType)

        fun parseSubtasks(json: String): List<Subtask> {
            return try {
                adapter.fromJson(json) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun toJson(subtasks: List<Subtask>): String {
            return try {
                adapter.toJson(subtasks)
            } catch (e: Exception) {
                "[]"
            }
        }
    }

    fun getSubtasksList(): List<Subtask> = parseSubtasks(subtasksJson)

    fun getFormattedDueDate(): String {
        return if (dueDateEpochMs != null) {
            val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.forLanguageTag("es-ES"))
            val dateStr = sdf.format(java.util.Date(dueDateEpochMs))
            if (!dueTimeFormatted.isNullOrBlank()) {
                "$dateStr, $dueTimeFormatted"
            } else {
                dateStr
            }
        } else if (!dueTimeFormatted.isNullOrBlank()) {
            dueTimeFormatted
        } else {
            "Sin fecha"
        }
    }
}
