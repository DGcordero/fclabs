package com.example.smart

import com.example.data.TaskCategory
import com.example.data.TaskEntity
import com.example.data.TaskPriority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmartUrgencyEngine {

    fun calculateUrgencyScore(task: TaskEntity): Float {
        if (task.isCompleted) return -100f

        var score = 0f

        // Priority score
        when (task.priority) {
            TaskPriority.ALTA.name -> score += 50f
            TaskPriority.MEDIA.name -> score += 30f
            TaskPriority.BAJA.name -> score += 10f
        }

        // Pinned score
        if (task.isPinned) score += 40f

        // Due date score
        val now = System.currentTimeMillis()
        val dueDate = task.dueDateEpochMs
        if (dueDate != null) {
            val diffHours = (dueDate - now) / (1000f * 60 * 60)
            when {
                diffHours < 0 -> score += 100f // Overdue!
                diffHours <= 24 -> score += 80f // Due within 24h
                diffHours <= 72 -> score += 40f // Due within 3 days
                else -> score += 15f
            }
        }

        return score
    }

    fun generateDailySummary(tasks: List<TaskEntity>): String {
        val total = tasks.size
        val pending = tasks.count { !it.isCompleted }
        val completed = tasks.count { it.isCompleted }
        val highPriorityPending = tasks.count { !it.isCompleted && it.priority == TaskPriority.ALTA.name }

        val now = System.currentTimeMillis()
        val overdue = tasks.count { !it.isCompleted && it.dueDateEpochMs != null && it.dueDateEpochMs < now }

        val sb = StringBuilder()
        sb.append("📋 **Resumen Inteligente del Día**\n\n")

        if (total == 0) {
            sb.append("¡Tu lista está vacía! Es un excelente momento para planificar tus nuevos objetivos.")
            return sb.toString()
        }

        val percentage = if (total > 0) (completed * 100 / total) else 0
        sb.append("• **Progreso actual:** $completed de $total tareas completadas ($percentage%).\n")

        if (overdue > 0) {
            sb.append("⚠️ **Atención:** Tienes **$overdue tarea(s) vencida(s)** que requieren tu atención inmediata.\n")
        }

        if (highPriorityPending > 0) {
            sb.append("🔥 **Prioridad Alta:** Tienes **$highPriorityPending tarea(s) de alta prioridad** pendientes.\n")
        } else if (pending > 0) {
            sb.append("✨ Tienes $pending tarea(s) pendientes. ¡Mantén el ritmo constante!\n")
        } else {
            sb.append("🎉 ¡Felicidades! Has completado todas tus tareas pendientes. Excelente trabajo de organización.\n")
        }

        // Advice
        sb.append("\n💡 **Recomendación Cordero F:** ")
        if (highPriorityPending > 0) {
            sb.append("Comienza tu jornada enfocándote en la tarea prioritaria de más alto valor antes de las actividades secundarias.")
        } else if (pending > 3) {
            sb.append("Divide tus tareas pendientes en bloques de 25 minutos (Técnica Pomodoro) para avanzar sin fatiga.")
        } else {
            sb.append("Tómate un momento para revisar tu agenda de mañana o disfrutar tu tiempo libre con tranquilidad.")
        }

        return sb.toString()
    }

    fun generateSubtaskSuggestions(taskTitle: String, category: String): List<String> {
        val lower = taskTitle.lowercase()
        return when {
            lower.contains("comprar") || lower.contains("super") -> listOf(
                "Elaborar lista detallada",
                "Revisar ofertas locales",
                "Realizar la compra y verificar productos"
            )
            lower.contains("estudiar") || lower.contains("examen") -> listOf(
                "Revisar notas y resúmenes",
                "Realizar tarjetas de memoria (flashcards)",
                "Simulación de examen de práctica"
            )
            lower.contains("viaje") || lower.contains("vuelo") -> listOf(
                "Verificar documentos e identificaciones",
                "Hacer lista de equipaje y empacar",
                "Confirmar horarios de salida y transporte"
            )
            category == TaskCategory.TRABAJO.name -> listOf(
                "Definir el objetivo principal",
                "Redactar borrador inicial",
                "Revisar detalles y enviar a interesados"
            )
            category == TaskCategory.SALUD.name -> listOf(
                "Agendar cita o definir horario",
                "Preparar ropa/equipo necesario",
                "Registrar progreso en la app"
            )
            else -> listOf(
                "Paso 1: Planificación inicial",
                "Paso 2: Ejecución principal",
                "Paso 3: Verificación final"
            )
        }
    }
}
