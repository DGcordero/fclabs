package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String = "Salud",
    val target: String = "1 vez al día",
    val isCompletedToday: Boolean = false,
    val streakDays: Int = 0,
    val lastCompletedEpochMs: Long? = null
)
