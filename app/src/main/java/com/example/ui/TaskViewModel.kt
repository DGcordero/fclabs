package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HabitEntity
import com.example.data.Subtask
import com.example.data.TaskCategory
import com.example.data.TaskEntity
import com.example.data.TaskPriority
import com.example.data.TaskRepository
import com.example.security.SecurityManager
import com.example.smart.SmartParsedResult
import com.example.smart.SmartTaskParser
import com.example.smart.SmartUrgencyEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val displayName: String) {
    FECHA("Fecha de Entrega"),
    PRIORIDAD("Prioridad Alta"),
    NOMBRE("Nombre A-Z"),
    RECIENTES("Más Recientes")
}

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    val securityManager: SecurityManager = SecurityManager(application)

    init {
        val db = AppDatabase.getInstance(application)
        repository = TaskRepository(db.taskDao(), db.habitDao(), application)
    }

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow(TaskCategory.TODAS)
    val selectedPriorityFilter = MutableStateFlow<TaskPriority?>(null)
    val showCompletedOnly = MutableStateFlow(false)
    val sortOption = MutableStateFlow(SortOption.FECHA)

    // Security & UI Dialog states
    val isPinLocked = MutableStateFlow(securityManager.isPinLockEnabled())
    val isSecurityDialogOpen = MutableStateFlow(false)
    val isAddEditSheetOpen = MutableStateFlow(false)
    val editingTask = MutableStateFlow<TaskEntity?>(null)
    val isSmartAssistantOpen = MutableStateFlow(false)

    // Quick AI input
    val quickAiInput = MutableStateFlow("")

    // Toast / Snackbar feedback messages
    private val _userFeedback = MutableSharedFlow<String>()
    val userFeedback: SharedFlow<String> = _userFeedback.asSharedFlow()

    // Filter Params Data Class
    private data class FilterParams(
        val query: String,
        val category: TaskCategory,
        val priority: TaskPriority?,
        val completedOnly: Boolean,
        val sort: SortOption
    )

    private val filterParams = combine(
        searchQuery,
        selectedCategory,
        selectedPriorityFilter,
        showCompletedOnly,
        sortOption
    ) { query, category, priority, completedOnly, sort ->
        FilterParams(query, category, priority, completedOnly, sort)
    }

    // Habits State Flow
    val habits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addHabit(title: String, category: String = "Salud", target: String = "1 vez al día") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newHabit = HabitEntity(
                title = title.trim(),
                category = category,
                target = target,
                isCompletedToday = false,
                streakDays = 0,
                lastCompletedEpochMs = null
            )
            repository.insertHabit(newHabit)
            _userFeedback.emit("Hábito añadido a tu rutina")
        }
    }

    fun toggleHabitCompleted(habit: HabitEntity) {
        viewModelScope.launch {
            val isCurrentlyCompleted = habit.isCompletedToday
            val newCompleted = !isCurrentlyCompleted
            val newStreak = if (newCompleted) {
                habit.streakDays + 1
            } else {
                (habit.streakDays - 1).coerceAtLeast(0)
            }
            val updated = habit.copy(
                isCompletedToday = newCompleted,
                streakDays = newStreak,
                lastCompletedEpochMs = if (newCompleted) System.currentTimeMillis() else habit.lastCompletedEpochMs
            )
            repository.updateHabit(updated)
            val msg = if (newCompleted) "¡Hábito '${habit.title}' completado hoy! 🔥" else "Hábito marcado como pendiente"
            _userFeedback.emit(msg)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            _userFeedback.emit("Hábito eliminado")
        }
    }

    // Combined filtered task flow
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        repository.allTasks,
        filterParams
    ) { tasks, params ->
        var list = tasks

        // Search query filter
        if (params.query.isNotBlank()) {
            val q = params.query.lowercase().trim()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.tags.lowercase().contains(q)
            }
        }

        // Category filter
        if (params.category != TaskCategory.TODAS) {
            list = list.filter { it.category == params.category.name }
        }

        // Priority filter
        if (params.priority != null) {
            list = list.filter { it.priority == params.priority.name }
        }

        // Status filter
        if (params.completedOnly) {
            list = list.filter { it.isCompleted }
        }

        // Sorting
        when (params.sort) {
            SortOption.FECHA -> list.sortedWith(
                compareByDescending<TaskEntity> { it.isPinned }
                    .thenBy { it.isCompleted }
                    .thenBy { it.dueDateEpochMs ?: Long.MAX_VALUE }
            )
            SortOption.PRIORIDAD -> list.sortedWith(
                compareByDescending<TaskEntity> { it.isPinned }
                    .thenBy { it.isCompleted }
                    .thenByDescending { try { TaskPriority.valueOf(it.priority).level } catch (_: Exception) { 1 } }
            )
            SortOption.NOMBRE -> list.sortedWith(
                compareByDescending<TaskEntity> { it.isPinned }
                    .thenBy { it.isCompleted }
                    .thenBy { it.title.lowercase() }
            )
            SortOption.RECIENTES -> list.sortedWith(
                compareByDescending<TaskEntity> { it.isPinned }
                    .thenBy { it.isCompleted }
                    .thenByDescending { it.createdAtEpochMs }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Summary statistics for QuickStatsBar
    val taskStats: StateFlow<TaskStats> = repository.allTasks.combine(filteredTasks) { all, _ ->
        val total = all.size
        val completed = all.count { it.isCompleted }
        val pending = all.count { !it.isCompleted }
        val highPriority = all.count { !it.isCompleted && it.priority == TaskPriority.ALTA.name }
        val now = System.currentTimeMillis()
        val overdue = all.count { !it.isCompleted && it.dueDateEpochMs != null && it.dueDateEpochMs < now }
        TaskStats(total = total, completed = completed, pending = pending, highPriority = highPriority, overdue = overdue)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskStats()
    )

    // Actions
    fun unlockWithPin(pin: String): Boolean {
        return if (securityManager.verifyPin(pin)) {
            isPinLocked.value = false
            true
        } else {
            false
        }
    }

    fun lockApp() {
        if (securityManager.isPinLockEnabled()) {
            isPinLocked.value = true
        }
    }

    fun saveTask(
        id: Int = 0,
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        dueDateEpochMs: Long?,
        dueTimeFormatted: String?,
        reminderEpochMs: Long?,
        isPinned: Boolean,
        subtasks: List<Subtask>
    ) {
        if (title.isBlank()) return

        viewModelScope.launch {
            val task = TaskEntity(
                id = id,
                title = title.trim(),
                description = description.trim(),
                category = category.name,
                priority = priority.name,
                dueDateEpochMs = dueDateEpochMs,
                dueTimeFormatted = dueTimeFormatted,
                reminderEpochMs = reminderEpochMs,
                isCompleted = editingTask.value?.isCompleted ?: false,
                isPinned = isPinned,
                subtasksJson = TaskEntity.toJson(subtasks),
                createdAtEpochMs = editingTask.value?.createdAtEpochMs ?: System.currentTimeMillis()
            )

            if (id == 0) {
                repository.insertTask(task)
                _userFeedback.emit("Tarea creada con éxito ✨")
            } else {
                repository.updateTask(task)
                _userFeedback.emit("Tarea actualizada 📝")
            }
            closeAddEditSheet()
        }
    }

    fun parseAndApplyQuickInput(): SmartParsedResult {
        val result = SmartTaskParser.parse(quickAiInput.value)
        return result
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
            val msg = if (!task.isCompleted) "¡Tarea completada! 🎉" else "Tarea marcada como pendiente"
            _userFeedback.emit(msg)
        }
    }

    fun toggleTaskPinned(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskPinned(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _userFeedback.emit("Tarea eliminada")
        }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            repository.deleteCompletedTasks()
            _userFeedback.emit("Tareas completadas limpiadas")
        }
    }

    fun openAddSheet() {
        editingTask.value = null
        quickAiInput.value = ""
        isAddEditSheetOpen.value = true
    }

    fun openAddSheetForDate(dateMs: Long) {
        editingTask.value = TaskEntity(
            title = "",
            description = "",
            category = TaskCategory.CITAS.name,
            priority = TaskPriority.MEDIA.name,
            dueDateEpochMs = dateMs,
            dueTimeFormatted = "10:00"
        )
        quickAiInput.value = ""
        isAddEditSheetOpen.value = true
    }

    fun openEditSheet(task: TaskEntity) {
        editingTask.value = task
        quickAiInput.value = ""
        isAddEditSheetOpen.value = true
    }

    fun closeAddEditSheet() {
        isAddEditSheetOpen.value = false
        editingTask.value = null
        quickAiInput.value = ""
    }

    // Export & Import backup logic
    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportTasksToJson(filteredTasks.value)
            onResult(json)
            _userFeedback.emit("Copia de seguridad exportada 💾")
        }
    }

    fun importBackup(json: String) {
        viewModelScope.launch {
            val count = repository.importTasksFromJson(json)
            if (count > 0) {
                _userFeedback.emit("Se importaron $count tareas correctamente 📥")
            } else {
                _userFeedback.emit("Error al importar la copia de seguridad")
            }
        }
    }
}

data class TaskStats(
    val total: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val highPriority: Int = 0,
    val overdue: Int = 0
)
