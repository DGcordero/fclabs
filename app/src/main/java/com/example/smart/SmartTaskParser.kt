package com.example.smart

import com.example.data.TaskCategory
import com.example.data.TaskPriority
import java.util.Calendar

data class SmartParsedResult(
    val title: String,
    val category: TaskCategory,
    val priority: TaskPriority,
    val dueDateEpochMs: Long?,
    val dueTimeFormatted: String?,
    val suggestedSubtasks: List<String>
)

object SmartTaskParser {

    fun parse(input: String): SmartParsedResult {
        var text = input.trim()
        if (text.isEmpty()) {
            return SmartParsedResult(
                title = "",
                category = TaskCategory.PERSONAL,
                priority = TaskPriority.MEDIA,
                dueDateEpochMs = null,
                dueTimeFormatted = null,
                suggestedSubtasks = emptyList()
            )
        }

        var detectedPriority = TaskPriority.MEDIA
        var detectedCategory = TaskCategory.PERSONAL
        var dueDateMs: Long? = null
        var timeStr: String? = null

        val lowerText = text.lowercase()

        // 1. Priority Detection
        when {
            lowerText.contains("urgente") || lowerText.contains("alta") || lowerText.contains("importante") || lowerText.contains("!!!") -> {
                detectedPriority = TaskPriority.ALTA
            }
            lowerText.contains("baja") || lowerText.contains("tranquilo") -> {
                detectedPriority = TaskPriority.BAJA
            }
        }

        // 2. Category Detection
        when {
            lowerText.contains("trabajo") || lowerText.contains("reunión") || lowerText.contains("reunion") || lowerText.contains("informe") || lowerText.contains("oficina") || lowerText.contains("cliente") -> {
                detectedCategory = TaskCategory.TRABAJO
            }
            lowerText.contains("salud") || lowerText.contains("médico") || lowerText.contains("medico") || lowerText.contains("ejercicio") || lowerText.contains("gimnasio") || lowerText.contains("pastilla") || lowerText.contains("doctor") -> {
                detectedCategory = TaskCategory.SALUD
            }
            lowerText.contains("pagar") || lowerText.contains("factura") || lowerText.contains("banco") || lowerText.contains("comprar") || lowerText.contains("dinero") || lowerText.contains("finanzas") || lowerText.contains("tarjeta") -> {
                detectedCategory = TaskCategory.FINANZAS
            }
            lowerText.contains("estudiar") || lowerText.contains("estudio") || lowerText.contains("examen") || lowerText.contains("tarea escolar") || lowerText.contains("curso") || lowerText.contains("leer") -> {
                detectedCategory = TaskCategory.ESTUDIO
            }
            lowerText.contains("proyecto") || lowerText.contains("código") || lowerText.contains("desarrollo") || lowerText.contains("diseño") -> {
                detectedCategory = TaskCategory.PROYECTO
            }
            lowerText.contains("casa") || lowerText.contains("hogar") || lowerText.contains("limpiar") || lowerText.contains("cocinar") || lowerText.contains("reparar") -> {
                detectedCategory = TaskCategory.HOGAR
            }
        }

        // 3. Date & Time Parsing (Hoy, Mañana, En X días)
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        when {
            lowerText.contains("hoy") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 20)
                calendar.set(Calendar.MINUTE, 0)
                dueDateMs = calendar.timeInMillis
            }
            lowerText.contains("mañana") || lowerText.contains("manana") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 10)
                calendar.set(Calendar.MINUTE, 0)
                dueDateMs = calendar.timeInMillis
            }
            lowerText.contains("pasado mañana") || lowerText.contains("pasado manana") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 2)
                calendar.set(Calendar.HOUR_OF_DAY, 10)
                calendar.set(Calendar.MINUTE, 0)
                dueDateMs = calendar.timeInMillis
            }
            lowerText.contains("esta semana") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 3)
                dueDateMs = calendar.timeInMillis
            }
            lowerText.contains("próxima semana") || lowerText.contains("proxima semana") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 7)
                dueDateMs = calendar.timeInMillis
            }
        }

        // Time extraction (e.g. "14:30" or "8pm")
        val timeRegex = Regex("""\b([0-1]?[0-9]|2[0-3]):([0-5][0-9])\b""")
        val timeMatch = timeRegex.find(lowerText)
        if (timeMatch != null) {
            timeStr = timeMatch.value
            try {
                val parts = timeStr.split(":")
                val hour = parts[0].toInt()
                val min = parts[1].toInt()
                if (dueDateMs == null) {
                    calendar.timeInMillis = now
                } else {
                    calendar.timeInMillis = dueDateMs
                }
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, min)
                dueDateMs = calendar.timeInMillis
            } catch (_: Exception) {}
        }

        // Clean title by stripping trigger words
        var cleanTitle = text
        val triggerWords = listOf(
            "mañana", "manana", "hoy", "pasado mañana", "pasado manana", "urgente", "alta", "baja", "media",
            "trabajo", "salud", "finanzas", "estudio", "proyecto", "hogar", "personal"
        )
        for (word in triggerWords) {
            cleanTitle = cleanTitle.replace(Regex("(?i)\\b$word\\b"), "").trim()
        }
        cleanTitle = cleanTitle.replace(Regex("\\s+"), " ")
        if (cleanTitle.isEmpty()) {
            cleanTitle = text
        }

        // 4. Subtask Auto-suggestions
        val suggestedSubtasks = mutableListOf<String>()
        val cleanLower = cleanTitle.lowercase()

        when {
            cleanLower.contains("compras") || cleanLower.contains("supermercado") || cleanLower.contains("super") -> {
                suggestedSubtasks.addAll(listOf("Hacer lista de compras", "Verificar presupuesto", "Guardar recibo"))
            }
            cleanLower.contains("viaje") || cleanLower.contains("vacaciones") -> {
                suggestedSubtasks.addAll(listOf("Empacar maleta", "Revisar documentos", "Confirmar reserva"))
            }
            cleanLower.contains("reunión") || cleanLower.contains("reunion") || cleanLower.contains("presentación") -> {
                suggestedSubtasks.addAll(listOf("Preparar puntos clave", "Enviar invitación", "Tomar notas de la reunión"))
            }
            cleanLower.contains("estudiar") || cleanLower.contains("examen") -> {
                suggestedSubtasks.addAll(listOf("Repasar apuntes", "Hacer resumen", "Resolver ejercicios prácticos"))
            }
            cleanLower.contains("proyecto") || cleanLower.contains("app") -> {
                suggestedSubtasks.addAll(listOf("Definir requerimientos", "Diseñar interfaz", "Probar funcionalidades"))
            }
            cleanLower.contains("limpieza") || cleanLower.contains("limpiar") -> {
                suggestedSubtasks.addAll(listOf("Organizar objetos", "Aspirar/Barrer", "Sacar la basura"))
            }
        }

        return SmartParsedResult(
            title = cleanTitle.replaceFirstChar { it.uppercase() },
            category = detectedCategory,
            priority = detectedPriority,
            dueDateEpochMs = dueDateMs,
            dueTimeFormatted = timeStr,
            suggestedSubtasks = suggestedSubtasks
        )
    }
}
