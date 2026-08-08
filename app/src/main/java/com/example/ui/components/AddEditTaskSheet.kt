package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Subtask
import com.example.data.TaskCategory
import com.example.data.TaskEntity
import com.example.data.TaskPriority
import com.example.smart.SmartTaskParser
import com.example.smart.SmartUrgencyEngine
import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.NotificationsActive
import com.example.ui.theme.EmeraldPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskSheet(
    task: TaskEntity?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (
        id: Int,
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        dueDateEpochMs: Long?,
        dueTimeFormatted: String?,
        reminderEpochMs: Long?,
        isPinned: Boolean,
        subtasks: List<Subtask>
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var title by remember(task) { mutableStateOf(task?.title ?: "") }
    var description by remember(task) { mutableStateOf(task?.description ?: "") }
    var category by remember(task) {
        mutableStateOf(try { TaskCategory.valueOf(task?.category ?: "") } catch (e: Exception) { TaskCategory.PERSONAL })
    }
    var priority by remember(task) {
        mutableStateOf(try { TaskPriority.valueOf(task?.priority ?: "") } catch (e: Exception) { TaskPriority.MEDIA })
    }
    var dueDateEpochMs by remember(task) { mutableStateOf(task?.dueDateEpochMs) }
    var dueTimeFormatted by remember(task) { mutableStateOf(task?.dueTimeFormatted) }
    var hasReminder by remember(task) { mutableStateOf(task?.reminderEpochMs != null) }
    var reminderEpochMs by remember(task) { mutableStateOf(task?.reminderEpochMs) }
    var isPinned by remember(task) { mutableStateOf(task?.isPinned ?: false) }

    val subtasks = remember(task) {
        mutableStateListOf<Subtask>().apply {
            if (task != null) addAll(task.getSubtasksList())
        }
    }

    var newSubtaskText by remember { mutableStateOf("") }
    var quickAiInput by remember { mutableStateOf("") }
    var showAiInput by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("add_edit_task_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (task == null) "Nueva Tarea Personal" else "Editar Tarea",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = { showAiInput = !showAiInput }) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Analizador IA",
                        tint = EmeraldPrimary
                    )
                }
            }

            // Quick AI Parsing Section
            AnimatedVisibility(visible = showAiInput || task == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier
                                    .height(18.dp)
                                    .width(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Entrada Rápida con IA en Español",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = quickAiInput,
                                onValueChange = { quickAiInput = it },
                                placeholder = { Text("Ej: Reunión mañana 16:00 urgente trabajo") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_ai_input_field"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (quickAiInput.isNotBlank()) {
                                        val parsed = SmartTaskParser.parse(quickAiInput)
                                        title = parsed.title
                                        category = parsed.category
                                        priority = parsed.priority
                                        if (parsed.dueDateEpochMs != null) {
                                            dueDateEpochMs = parsed.dueDateEpochMs
                                            reminderEpochMs = parsed.dueDateEpochMs
                                            hasReminder = true
                                        }
                                        if (parsed.dueTimeFormatted != null) {
                                            dueTimeFormatted = parsed.dueTimeFormatted
                                        }
                                        parsed.suggestedSubtasks.forEach { st ->
                                            subtasks.add(Subtask(title = st))
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                modifier = Modifier.testTag("apply_ai_button")
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Aplicar")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título de la tarea *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notas o descripción personal") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_description_input"),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category selector
            Text(
                text = "Categoría",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskCategory.entries.filter { it != TaskCategory.TODAS }.forEach { cat ->
                    val selected = cat == category
                    OutlinedButton(
                        onClick = { category = cat },
                        colors = if (selected) ButtonDefaults.outlinedButtonColors(containerColor = EmeraldPrimary.copy(alpha = 0.2f)) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(cat),
                            contentDescription = null,
                            tint = if (selected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .height(16.dp)
                                .width(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = cat.displayName, color = if (selected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Priority Selector
            Text(
                text = "Prioridad",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TaskPriority.entries.forEach { prio ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (priority == prio),
                            onClick = { priority = prio }
                        )
                        Text(text = prio.displayName, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date & Time Pickers
            Text(
                text = "Fecha de Entrega & Recordatorio",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Date picker button
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        if (dueDateEpochMs != null) cal.timeInMillis = dueDateEpochMs!!

                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                cal.set(Calendar.YEAR, year)
                                cal.set(Calendar.MONTH, month)
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                dueDateEpochMs = cal.timeInMillis
                                if (hasReminder && reminderEpochMs == null) {
                                    reminderEpochMs = cal.timeInMillis
                                }
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (dueDateEpochMs != null) {
                            SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-ES")).format(Date(dueDateEpochMs!!))
                        } else "Elegir Fecha"
                    )
                }

                // Time picker button
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        if (dueDateEpochMs != null) cal.timeInMillis = dueDateEpochMs!!

                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                cal.set(Calendar.MINUTE, minute)
                                dueDateEpochMs = cal.timeInMillis
                                dueTimeFormatted = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                                if (hasReminder) {
                                    reminderEpochMs = cal.timeInMillis
                                }
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Alarm, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = dueTimeFormatted ?: "Hora")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reminder toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notificación Push en Dispositivo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Switch(
                    checked = hasReminder,
                    onCheckedChange = {
                        hasReminder = it
                        if (it && reminderEpochMs == null) {
                            reminderEpochMs = dueDateEpochMs ?: (System.currentTimeMillis() + 3600000)
                        } else if (!it) {
                            reminderEpochMs = null
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                )
            }

            if (hasReminder) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = {
                        val testIntent = android.content.Intent(context, com.example.reminder.TaskReminderReceiver::class.java).apply {
                            putExtra(com.example.reminder.TaskReminderReceiver.EXTRA_TASK_ID, 9999)
                            putExtra(com.example.reminder.TaskReminderReceiver.EXTRA_TASK_TITLE, if (title.isNotBlank()) "Recordatorio: $title" else "Recordatorio de Prueba")
                            putExtra(com.example.reminder.TaskReminderReceiver.EXTRA_TASK_DESC, "Notificación Push local activada correctamente en tu dispositivo 📱")
                        }
                        context.sendBroadcast(testIntent)
                        Toast.makeText(context, "Notificación Push enviada al dispositivo 🔔", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Probar Notificación Push Inmediata", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtasks Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sub-tareas (${subtasks.size})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                // Auto-suggest subtasks button
                OutlinedButton(
                    onClick = {
                        if (title.isNotBlank()) {
                            val suggestions = SmartUrgencyEngine.generateSubtaskSuggestions(title, category.name)
                            suggestions.forEach { st ->
                                if (subtasks.none { it.title == st }) {
                                    subtasks.add(Subtask(title = st))
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier
                            .height(14.dp)
                            .width(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sugerir Sub-tareas", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            subtasks.forEachIndexed { index, subtask ->
                SubtaskItemRow(
                    subtask = subtask,
                    onToggle = { isChecked ->
                        subtasks[index] = subtask.copy(isCompleted = isChecked)
                    },
                    onDelete = { subtasks.removeAt(index) }
                )
            }

            // Input to add a new subtask
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newSubtaskText,
                    onValueChange = { newSubtaskText = it },
                    placeholder = { Text("Añadir paso o sub-tarea...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newSubtaskText.isNotBlank()) {
                            subtasks.add(Subtask(title = newSubtaskText.trim()))
                            newSubtaskText = ""
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Sub-tarea", tint = EmeraldPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                task?.id ?: 0,
                                title,
                                description,
                                category,
                                priority,
                                dueDateEpochMs,
                                dueTimeFormatted,
                                if (hasReminder) reminderEpochMs else null,
                                isPinned,
                                subtasks.toList()
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_task_button")
                ) {
                    Text("Guardar Tarea")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
