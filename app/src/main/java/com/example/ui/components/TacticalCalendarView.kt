package com.example.ui.components

import android.app.NotificationManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskCategory
import com.example.data.TaskEntity
import com.example.reminder.TaskReminderReceiver
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PriorityHighRed
import com.example.ui.theme.TealSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TacticalCalendarView(
    tasks: List<TaskEntity>,
    onAddNewAppointmentForDate: (Long) -> Unit,
    onTaskClick: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var calendarMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDateMs by remember { mutableStateOf(System.currentTimeMillis()) }

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("es", "ES")) }
    val dayMonthFormat = remember { SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")) }

    // Days calculation for calendar grid
    val daysInMonth = remember(calendarMonth.timeInMillis) {
        val cal = calendarMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val daysList = mutableListOf<CalendarDayInfo?>()
        for (i in 0 until firstDayOfWeek) {
            daysList.add(null) // blank padding
        }

        val todayCal = Calendar.getInstance()

        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dayMs = cal.timeInMillis

            val isToday = todayCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                    todayCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)

            // Find items on this day
            val itemsOnDay = tasks.filter { task ->
                task.dueDateEpochMs != null && isSameDay(task.dueDateEpochMs, dayMs)
            }

            daysList.add(
                CalendarDayInfo(
                    dayNumber = day,
                    epochMs = dayMs,
                    isToday = isToday,
                    hasAppointments = itemsOnDay.isNotEmpty(),
                    itemCount = itemsOnDay.size,
                    hasHighPriority = itemsOnDay.any { it.priority == "ALTA" }
                )
            )
        }
        daysList
    }

    // Selected day agenda items
    val agendaItems = remember(selectedDateMs, tasks) {
        tasks.filter { task ->
            task.dueDateEpochMs != null && isSameDay(task.dueDateEpochMs, selectedDateMs)
        }.sortedBy { it.dueTimeFormatted ?: "23:59" }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tactical_calendar_view")
    ) {
        // Month Selector Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = monthYearFormat.format(calendarMonth.time).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val cal = calendarMonth.clone() as Calendar
                                cal.add(Calendar.MONTH, -1)
                                calendarMonth = cal
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Mes anterior",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = {
                                val cal = calendarMonth.clone() as Calendar
                                cal.add(Calendar.MONTH, 1)
                                calendarMonth = cal
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Mes siguiente",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Days of Week Header
                val daysOfWeek = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grid of Days
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    userScrollEnabled = false,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(daysInMonth) { dayInfo ->
                        if (dayInfo == null) {
                            Box(modifier = Modifier.aspectRatio(1f))
                        } else {
                            val isSelected = isSameDay(dayInfo.epochMs, selectedDateMs)

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> EmeraldPrimary
                                            dayInfo.isToday -> EmeraldPrimary.copy(alpha = 0.25f)
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .border(
                                        width = if (dayInfo.isToday && !isSelected) 2.dp else 0.dp,
                                        color = EmeraldPrimary,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedDateMs = dayInfo.epochMs
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = dayInfo.dayNumber.toString(),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || dayInfo.isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            dayInfo.isToday -> EmeraldPrimary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )

                                    if (dayInfo.hasAppointments) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else if (dayInfo.hasHighPriority) PriorityHighRed else EmeraldPrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Day Agenda Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dayMonthFormat.format(Date(selectedDateMs)).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${agendaItems.size} cita(s) programada(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { onAddNewAppointmentForDate(selectedDateMs) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Añadir Cita", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Agenda Items list
        if (agendaItems.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📅 No hay citas ni eventos para esta fecha.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onAddNewAppointmentForDate(selectedDateMs) }
                    ) {
                        Text("Agendar una cita para hoy")
                    }
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                agendaItems.forEach { item ->
                    val categoryEnum = try { TaskCategory.valueOf(item.category) } catch (e: Exception) { TaskCategory.PERSONAL }

                    Surface(
                        onClick = { onTaskClick(item) },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(categoryEnum),
                                        contentDescription = categoryEnum.displayName,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = TealSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = item.dueTimeFormatted ?: "Todo el día",
                                            fontSize = 11.sp,
                                            color = TealSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (item.reminderEpochMs != null) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = "Alarma",
                                                tint = EmeraldPrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (item.priority == "ALTA") {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PriorityHighRed.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "URGENTE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PriorityHighRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instant Notification Testing Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Notificaciones Inteligentes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Recordatorios programados directamente en el sistema Android",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        val receiver = TaskReminderReceiver()
                        val intent = android.content.Intent(context, TaskReminderReceiver::class.java).apply {
                            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, 9999)
                            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, "⚡ Notificación Táctica Cita Cordero F")
                            putExtra(TaskReminderReceiver.EXTRA_TASK_DESC, "Prueba realizada con éxito: Tus citas y alarmas avisarán puntualmente.")
                        }
                        receiver.onReceive(context, intent)
                        Toast.makeText(context, "Notificación enviada", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Probar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class CalendarDayInfo(
    val dayNumber: Int,
    val epochMs: Long,
    val isToday: Boolean,
    val hasAppointments: Boolean,
    val itemCount: Int,
    val hasHighPriority: Boolean
)

private fun isSameDay(ms1: Long, ms2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = ms1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = ms2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
