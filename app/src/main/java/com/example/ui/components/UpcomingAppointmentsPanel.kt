package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskCategory
import com.example.data.TaskEntity
import com.example.security.SecurityManager
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PriorityHighRed
import com.example.ui.theme.TealSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UpcomingAppointmentsPanel(
    tasks: List<TaskEntity>,
    securityManager: SecurityManager,
    onLockApp: () -> Unit,
    onOpenSecuritySettings: () -> Unit,
    onAddNewAppointment: () -> Unit,
    onTaskClick: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter pending tasks/appointments sorted by due date or priority
    val upcomingItems = remember(tasks) {
        tasks.filter { !it.isCompleted }
            .sortedWith(
                compareByDescending<TaskEntity> { it.dueDateEpochMs != null }
                    .thenBy { it.dueDateEpochMs ?: Long.MAX_VALUE }
            )
            .take(6)
    }

    val isPinEnabled = securityManager.isPinLockEnabled()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("upcoming_appointments_panel"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Panel Title & PIN Quick Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Citas",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Citas & Tareas Próximas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${upcomingItems.size} pendiente(s) destacadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // PIN de Inicio Badge / Quick Lock
                Surface(
                    onClick = {
                        if (isPinEnabled) {
                            onLockApp()
                        } else {
                            onOpenSecuritySettings()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPinEnabled) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("pin_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPinEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "PIN de Inicio",
                            tint = if (isPinEnabled) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPinEnabled) "PIN Activo" else "+ Configurar PIN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPinEnabled) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Carousel or Empty State for Upcoming Appointments
            if (upcomingItems.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = TealSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Sin citas ni tareas pendientes",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Añade recordatorios con fecha y hora",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = onAddNewAppointment) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Añadir Cita",
                                tint = EmeraldPrimary
                            )
                        }
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = upcomingItems,
                        key = { it.id }
                    ) { item ->
                        AppointmentCard(
                            item = item,
                            onClick = { onTaskClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    item: TaskEntity,
    onClick: () -> Unit
) {
    val categoryEnum = remember(item.category) {
        try { TaskCategory.valueOf(item.category) } catch (e: Exception) { TaskCategory.PERSONAL }
    }

    val formattedDate = remember(item.dueDateEpochMs, item.dueTimeFormatted) {
        if (item.dueDateEpochMs != null) {
            val sdf = SimpleDateFormat("dd MMM", Locale("es", "ES"))
            val dateStr = sdf.format(Date(item.dueDateEpochMs))
            if (!item.dueTimeFormatted.isNull_or_blank_safe()) {
                "$dateStr, ${item.dueTimeFormatted}"
            } else {
                dateStr
            }
        } else if (!item.dueTimeFormatted.isNull_or_blank_safe()) {
            item.dueTimeFormatted!!
        } else {
            "Pendiente"
        }
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .width(200.dp)
            .testTag("appointment_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                Text(
                    text = categoryEnum.displayName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )

                if (item.priority == "ALTA") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PriorityHighRed.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ALTA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = PriorityHighRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = TealSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TealSecondary
                )
            }
        }
    }
}

private fun String?.isNull_or_blank_safe(): Boolean {
    return this == null || this.trim().isEmpty()
}
