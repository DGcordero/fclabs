package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskCategory
import com.example.data.TaskEntity
import com.example.data.TaskPriority
import com.example.smart.SmartTaskParser
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PriorityHighRed
import com.example.ui.theme.TealSecondary

@Composable
fun EisenhowerMatrixView(
    tasks: List<TaskEntity>,
    onQuickAddParsedTask: (title: String, desc: String, cat: TaskCategory, prio: TaskPriority, dateMs: Long?, timeStr: String?, subtasks: List<String>) -> Unit,
    onTaskClick: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fastPromptInput by remember { mutableStateOf("") }

    // Divide tasks into 4 Eisenhower Quadrants deterministically
    val pendingTasks = remember(tasks) { tasks.filter { !it.isCompleted } }

    val q1DoFirst = remember(pendingTasks) {
        pendingTasks.filter { it.priority == "ALTA" || (it.dueDateEpochMs != null && it.dueDateEpochMs - System.currentTimeMillis() < 24 * 3600 * 1000) }
    }

    val q2Schedule = remember(pendingTasks) {
        pendingTasks.filter { (it.category == "CITAS" || it.category == "ESTUDIO" || it.category == "PROYECTO" || it.category == "TRABAJO") && it.priority != "ALTA" }
    }

    val q3Quick = remember(pendingTasks) {
        pendingTasks.filter { (it.category == "FINANZAS" || it.category == "HOGAR" || it.category == "COMPRAS") && !q1DoFirst.contains(it) }
    }

    val q4Routine = remember(pendingTasks) {
        pendingTasks.filter { !q1DoFirst.contains(it) && !q2Schedule.contains(it) && !q3Quick.contains(it) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("eisenhower_matrix_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Fast Entry Rule Engine Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Procesador Rápido de Tareas & Citas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Detecta fechas, horas, categorías y prioridad en texto en español de manera 100% local:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = fastPromptInput,
                        onValueChange = { fastPromptInput = it },
                        placeholder = { Text("Ej: Cita médica el viernes 17:00 urgente", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fast_command_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (fastPromptInput.isNotBlank()) {
                                val parsed = SmartTaskParser.parse(fastPromptInput)
                                onQuickAddParsedTask(
                                    parsed.title,
                                    "Agendado con Entrada Rápida Táctica",
                                    parsed.category,
                                    parsed.priority,
                                    parsed.dueDateEpochMs,
                                    parsed.dueTimeFormatted,
                                    parsed.suggestedSubtasks
                                )
                                Toast.makeText(context, "Cita/Tarea creada", Toast.LENGTH_SHORT).show()
                                fastPromptInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Procesar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fast Presets
                val samplePrompts = listOf(
                    "Cita dentista el viernes 17:00 urgente",
                    "Reunión trabajo mañana 10:00",
                    "Cita de estudio el 20 de agosto 15:00",
                    "Pagar factura de luz finanzas"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(samplePrompts) { prompt ->
                        Surface(
                            onClick = {
                                fastPromptInput = prompt
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "📌 $prompt",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Eisenhower Quadrants Grid Header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = EmeraldPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Matriz de Priorización Eisenhower (Local)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Quadrant 1: URGENTE E IMPORTANTE
        QuadrantCard(
            title = "🚨 1. Hazlo Ya (Urgente e Importante)",
            subtitle = "Citas y tareas de alta prioridad o límite < 24h",
            accentColor = PriorityHighRed,
            items = q1DoFirst,
            onTaskClick = onTaskClick
        )

        // Quadrant 2: PLANIFICAR
        QuadrantCard(
            title = "📅 2. Planificar (Citas & Proyectos Clave)",
            subtitle = "Estudio, proyectos, reuniones y salud a futuro",
            accentColor = EmeraldPrimary,
            items = q2Schedule,
            onTaskClick = onTaskClick
        )

        // Quadrant 3: RÁPIDO Y DELEGAR
        QuadrantCard(
            title = "⚡ 3. Rápido / Trámites (Baja Carga Cognitiva)",
            subtitle = "Pagos, compras, finanzas y tareas de hogar",
            accentColor = AccentAmber,
            items = q3Quick,
            onTaskClick = onTaskClick
        )

        // Quadrant 4: RUTINAS
        QuadrantCard(
            title = "📝 4. Pendientes Secundarios y Notas",
            subtitle = "Recordatorios personales sin límite estricto",
            accentColor = TealSecondary,
            items = q4Routine,
            onTaskClick = onTaskClick
        )
    }
}

@Composable
private fun QuadrantCard(
    title: String,
    subtitle: String,
    accentColor: androidx.compose.ui.graphics.Color,
    items: List<TaskEntity>,
    onTaskClick: (TaskEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = accentColor
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${items.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items.take(3).forEach { task ->
                        Surface(
                            onClick = { onTaskClick(task) },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = task.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = task.getFormattedDueDate(),
                                    fontSize = 10.sp,
                                    color = TealSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
