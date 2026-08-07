package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PriorityHighRed
import com.example.ui.theme.TealSecondary
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WorkShiftDay(
    val dayName: String,
    val dayCode: String,
    var isWorkDay: Boolean = true,
    var shiftType: String = "Turno Partido", // "Turno Partido", "Mañana", "Tarde", "Noche", "Teletrabajo", "Libre"
    var isSplitShift: Boolean = true,
    var startTime: String = "08:00",
    var endTime: String = "17:00",
    var splitStart1: String = "09:00",
    var splitEnd1: String = "14:00",
    var splitStart2: String = "16:00",
    var splitEnd2: String = "19:00",
    var targetHours: Double = 8.0,
    var note: String = ""
)

data class ClockLog(
    val id: String,
    val dateFormatted: String,
    val clockInTime: String,
    var clockOutTime: String? = null,
    val totalHours: Double = 0.0,
    val shiftType: String = "Presencial"
)

private const val PREFS_NAME = "work_schedule_prefs"
private const val KEY_SCHEDULE_JSON = "key_schedule_json"
private const val KEY_CLOCK_LOGS_JSON = "key_clock_logs_json"

// Helper functions for persistence
private fun saveScheduleToPrefs(context: Context, schedule: List<WorkShiftDay>) {
    try {
        val jsonArray = JSONArray()
        for (day in schedule) {
            val obj = JSONObject().apply {
                put("dayName", day.dayName)
                put("dayCode", day.dayCode)
                put("isWorkDay", day.isWorkDay)
                put("shiftType", day.shiftType)
                put("isSplitShift", day.isSplitShift)
                put("startTime", day.startTime)
                put("endTime", day.endTime)
                put("splitStart1", day.splitStart1)
                put("splitEnd1", day.splitEnd1)
                put("splitStart2", day.splitStart2)
                put("splitEnd2", day.splitEnd2)
                put("targetHours", day.targetHours)
                put("note", day.note)
            }
            jsonArray.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SCHEDULE_JSON, jsonArray.toString())
            .apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun loadScheduleFromPrefs(context: Context): List<WorkShiftDay>? {
    val jsonStr = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_SCHEDULE_JSON, null) ?: return null
    return try {
        val jsonArray = JSONArray(jsonStr)
        val list = mutableListOf<WorkShiftDay>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                WorkShiftDay(
                    dayName = obj.optString("dayName", ""),
                    dayCode = obj.optString("dayCode", ""),
                    isWorkDay = obj.optBoolean("isWorkDay", true),
                    shiftType = obj.optString("shiftType", "Turno Partido"),
                    isSplitShift = obj.optBoolean("isSplitShift", true),
                    startTime = obj.optString("startTime", "08:00"),
                    endTime = obj.optString("endTime", "17:00"),
                    splitStart1 = obj.optString("splitStart1", "09:00"),
                    splitEnd1 = obj.optString("splitEnd1", "14:00"),
                    splitStart2 = obj.optString("splitStart2", "16:00"),
                    splitEnd2 = obj.optString("splitEnd2", "19:00"),
                    targetHours = obj.optDouble("targetHours", 8.0),
                    note = obj.optString("note", "")
                )
            )
        }
        if (list.size == 7) list else null
    } catch (e: Exception) {
        null
    }
}

private fun calculateHoursBetween(start: String, end: String): Double {
    return try {
        val sParts = start.trim().split(":")
        val eParts = end.trim().split(":")
        if (sParts.size == 2 && eParts.size == 2) {
            val startMins = sParts[0].toInt() * 60 + sParts[1].toInt()
            val endMins = eParts[0].toInt() * 60 + eParts[1].toInt()
            if (endMins > startMins) {
                val diffMins = endMins - startMins
                Math.round((diffMins / 60.0) * 10.0) / 10.0
            } else 0.0
        } else 0.0
    } catch (e: Exception) {
        0.0
    }
}

private fun calculateTotalDayHours(day: WorkShiftDay): Double {
    if (!day.isWorkDay || day.shiftType == "Libre") return 0.0
    return if (day.isSplitShift || day.shiftType == "Turno Partido") {
        val h1 = calculateHoursBetween(day.splitStart1, day.splitEnd1)
        val h2 = calculateHoursBetween(day.splitStart2, day.splitEnd2)
        val total = h1 + h2
        if (total > 0) total else 8.0
    } else {
        val h = calculateHoursBetween(day.startTime, day.endTime)
        if (h > 0) h else 8.0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkScheduleView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Fichaje actual state
    var isClockedIn by remember { mutableStateOf(false) }
    var currentClockInTime by remember { mutableStateOf("") }
    var currentClockInEpoch by remember { mutableStateOf(0L) }

    val clockLogs = remember {
        mutableStateListOf(
            ClockLog("c1", dateFormat.format(Date()), "08:02", "17:05", 8.5, "Presencial"),
            ClockLog("c2", "06/08/2026", "09:00", "19:00", 8.0, "Turno Partido"),
            ClockLog("c3", "05/08/2026", "08:15", "17:15", 8.0, "Teletrabajo")
        )
    }

    // Default 7 days
    val defaultSchedule = remember {
        listOf(
            WorkShiftDay("Lunes", "LUN", true, "Turno Partido", isSplitShift = true, splitStart1 = "09:00", splitEnd1 = "14:00", splitStart2 = "16:00", splitEnd2 = "19:00", targetHours = 8.0, note = "Turno partido habitual"),
            WorkShiftDay("Martes", "MAR", true, "Turno Partido", isSplitShift = true, splitStart1 = "09:00", splitEnd1 = "14:00", splitStart2 = "16:00", splitEnd2 = "19:00", targetHours = 8.0, note = "Turno partido habitual"),
            WorkShiftDay("Miércoles", "MIÉ", true, "Turno Partido", isSplitShift = true, splitStart1 = "09:00", splitEnd1 = "14:00", splitStart2 = "16:00", splitEnd2 = "19:00", targetHours = 8.0, note = "Teletrabajo mañana / Oficina tarde"),
            WorkShiftDay("Jueves", "JUE", true, "Turno Partido", isSplitShift = true, splitStart1 = "09:00", splitEnd1 = "14:00", splitStart2 = "16:00", splitEnd2 = "19:00", targetHours = 8.0, note = "Turno partido habitual"),
            WorkShiftDay("Viernes", "VIE", true, "Turno Partido", isSplitShift = true, splitStart1 = "09:00", splitEnd1 = "14:00", splitStart2 = "16:00", splitEnd2 = "19:00", targetHours = 8.0, note = "Turno partido habitual"),
            WorkShiftDay("Sábado", "SÁB", false, "Libre", isSplitShift = false, startTime = "--:--", endTime = "--:--", targetHours = 0.0, note = "Descanso semanal"),
            WorkShiftDay("Domingo", "DOM", false, "Libre", isSplitShift = false, startTime = "--:--", endTime = "--:--", targetHours = 0.0, note = "Descanso semanal")
        )
    }

    val workSchedule = remember { mutableStateListOf<WorkShiftDay>() }

    // Load from SharedPreferences on start
    LaunchedEffect(Unit) {
        val saved = loadScheduleFromPrefs(context)
        workSchedule.clear()
        if (saved != null) {
            workSchedule.addAll(saved)
        } else {
            workSchedule.addAll(defaultSchedule)
            saveScheduleToPrefs(context, defaultSchedule)
        }
    }

    var editingDayIndex by remember { mutableStateOf<Int?>(null) }
    var isGlobalEditActive by remember { mutableStateOf(false) }

    // Computations
    val totalWeeklyTargetHours = workSchedule.filter { it.isWorkDay }.sumOf { it.targetHours }
    val totalClockedHoursThisWeek = clockLogs.sumOf { it.totalHours }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("work_schedule_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECCIÓN 1: CONTROL DE FICHAJE RÁPIDO ---
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
                        Surface(
                            shape = CircleShape,
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Control de Fichaje Diario",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Registro de entrada y salida laboral",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isClockedIn) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = if (isClockedIn) "● EN JORNADA" else "○ FUERA DE JORNADA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClockedIn) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isClockedIn) {
                                isClockedIn = true
                                currentClockInTime = timeFormat.format(Date())
                                currentClockInEpoch = System.currentTimeMillis()
                                Toast.makeText(context, "Fichaje de Entrada: $currentClockInTime ⏱️", Toast.LENGTH_SHORT).show()
                            } else {
                                val clockOutTime = timeFormat.format(Date())
                                val elapsedHours = ((System.currentTimeMillis() - currentClockInEpoch) / (1000.0 * 3600.0)).let {
                                    Math.round(it * 10.0) / 10.0
                                }
                                clockLogs.add(
                                    0,
                                    ClockLog(
                                        id = System.currentTimeMillis().toString(),
                                        dateFormatted = dateFormat.format(Date()),
                                        clockInTime = currentClockInTime,
                                        clockOutTime = clockOutTime,
                                        totalHours = if (elapsedHours > 0) elapsedHours else 8.0,
                                        shiftType = "Turno Partido"
                                    )
                                )
                                isClockedIn = false
                                Toast.makeText(context, "Fichaje de Salida: $clockOutTime ($elapsedHours h)", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isClockedIn) PriorityHighRed else EmeraldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isClockedIn) Icons.Default.Logout else Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isClockedIn) "Fichar Salida" else "Fichar Entrada",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Pausa/Descanso registrado ☕", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Coffee,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = AccentAmber
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registrar Pausa", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Objetivo Semanal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${Math.round(totalWeeklyTargetHours * 10.0) / 10.0}h", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Registrado", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${Math.round(totalClockedHoursThisWeek * 10.0) / 10.0}h", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TealSecondary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Balance", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val diff = totalClockedHoursThisWeek - totalWeeklyTargetHours
                            Text(
                                text = if (diff >= 0) "+${String.format("%.1f", diff)}h" else "${String.format("%.1f", diff)}h",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (diff >= 0) EmeraldPrimary else PriorityHighRed
                            )
                        }
                    }
                }
            }
        }

        // --- SECCIÓN 2: CUADRANTE DE TURNOS Y TURNOS PARTIDOS ---
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
                        Surface(
                            shape = CircleShape,
                            color = AccentAmber.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.WorkHistory,
                                    contentDescription = null,
                                    tint = AccentAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cuadrante de Turnos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Gestión de turnos partidos e itinerarios",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { isGlobalEditActive = !isGlobalEditActive }) {
                            Icon(
                                imageVector = if (isGlobalEditActive) Icons.Default.Check else Icons.Default.Tune,
                                contentDescription = "Modo Ajustes",
                                tint = EmeraldPrimary
                            )
                        }

                        IconButton(onClick = {
                            Toast.makeText(context, "Recordatorio de turno configurado ⏰", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Alarma Turno",
                                tint = AccentAmber
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Presets Bar
                Text(
                    text = "Acciones Rápidas y Plantillas de Turno:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Button(
                            onClick = {
                                for (i in 0..6) {
                                    val current = workSchedule[i]
                                    val isWeekend = (i == 5 || i == 6)
                                    val targetH = if (isWeekend) 0.0 else 8.0
                                    workSchedule[i] = current.copy(
                                        isWorkDay = !isWeekend,
                                        shiftType = if (isWeekend) "Libre" else "Turno Partido",
                                        isSplitShift = !isWeekend,
                                        splitStart1 = "09:00",
                                        splitEnd1 = "14:00",
                                        splitStart2 = "16:00",
                                        splitEnd2 = "19:00",
                                        targetHours = targetH,
                                        note = if (isWeekend) "Descanso" else "Turno partido L-V"
                                    )
                                }
                                saveScheduleToPrefs(context, workSchedule)
                                Toast.makeText(context, "⚡ Turno Partido guardado de Lunes a Viernes!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⚡ Turno Partido L-V (9-14 / 16-19)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                for (i in 0..6) {
                                    val current = workSchedule[i]
                                    workSchedule[i] = current.copy(
                                        isWorkDay = true,
                                        shiftType = "Turno Partido",
                                        isSplitShift = true,
                                        splitStart1 = "09:00",
                                        splitEnd1 = "14:00",
                                        splitStart2 = "16:00",
                                        splitEnd2 = "19:00",
                                        targetHours = 8.0,
                                        note = "Turno partido diario"
                                    )
                                }
                                saveScheduleToPrefs(context, workSchedule)
                                Toast.makeText(context, "⚡ Turno Partido aplicado a TODOS LOS DÍAS (L-D)", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Turno Partido Todos los Días (L-D)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                for (i in 0..4) {
                                    val current = workSchedule[i]
                                    workSchedule[i] = current.copy(
                                        isWorkDay = true,
                                        shiftType = "Mañana",
                                        isSplitShift = false,
                                        startTime = "08:00",
                                        endTime = "16:00",
                                        targetHours = 8.0,
                                        note = "Jornada continua"
                                    )
                                }
                                saveScheduleToPrefs(context, workSchedule)
                                Toast.makeText(context, "Jornada continua (08:00 - 16:00) guardada", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Jornada Continua L-V", fontSize = 11.sp)
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                workSchedule.clear()
                                workSchedule.addAll(defaultSchedule)
                                saveScheduleToPrefs(context, defaultSchedule)
                                Toast.makeText(context, "Horario restablecido por defecto", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restablecer", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Schedule Days List
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    workSchedule.forEachIndexed { index, day ->
                        val isSplit = day.isSplitShift || day.shiftType == "Turno Partido"

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (day.isWorkDay) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            tonalElevation = if (day.isWorkDay) 2.dp else 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSplit && day.isWorkDay) 1.dp else 0.dp,
                                    color = if (isSplit && day.isWorkDay) AccentAmber.copy(alpha = 0.4f) else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = when {
                                                !day.isWorkDay -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                                isSplit -> AccentAmber
                                                day.shiftType == "Mañana" -> EmeraldPrimary
                                                day.shiftType == "Teletrabajo" -> TealSecondary
                                                else -> MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = day.dayCode.take(2),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (day.isWorkDay) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = day.dayName,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = when {
                                                        !day.isWorkDay -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                                        isSplit -> AccentAmber.copy(alpha = 0.18f)
                                                        day.shiftType == "Mañana" -> EmeraldPrimary.copy(alpha = 0.15f)
                                                        day.shiftType == "Teletrabajo" -> TealSecondary.copy(alpha = 0.15f)
                                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                    }
                                                ) {
                                                    Text(
                                                        text = if (!day.isWorkDay) "Libre / Descanso" else if (isSplit) "⚡ Turno Partido" else day.shiftType,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when {
                                                            !day.isWorkDay -> MaterialTheme.colorScheme.onSurfaceVariant
                                                            isSplit -> AccentAmber
                                                            day.shiftType == "Mañana" -> EmeraldPrimary
                                                            day.shiftType == "Teletrabajo" -> TealSecondary
                                                            else -> MaterialTheme.colorScheme.primary
                                                        },
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }

                                            if (day.note.isNotBlank()) {
                                                Text(
                                                    text = day.note,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isGlobalEditActive) {
                                            Switch(
                                                checked = day.isWorkDay,
                                                onCheckedChange = { checked ->
                                                    workSchedule[index] = day.copy(
                                                        isWorkDay = checked,
                                                        targetHours = if (checked) calculateTotalDayHours(day) else 0.0
                                                    )
                                                    saveScheduleToPrefs(context, workSchedule)
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                                            )
                                        }

                                        IconButton(onClick = { editingDayIndex = index }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar Día",
                                                tint = EmeraldPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Detailed Time Timeline for Split Shift vs Continuous Shift
                                if (day.isWorkDay) {
                                    if (isSplit) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Tramo 1 Pill
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = AccentAmber.copy(alpha = 0.12f),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "T1: ${day.splitStart1} - ${day.splitEnd1}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }

                                            // Pausa indicator
                                            Text("☕", fontSize = 11.sp)

                                            // Tramo 2 Pill
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = AccentAmber.copy(alpha = 0.12f),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(Icons.Default.NightsStay, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "T2: ${day.splitStart2} - ${day.splitEnd2}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }

                                            // Total Hours badge
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = EmeraldPrimary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "${calculateTotalDayHours(day)}h",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldPrimary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = TealSecondary.copy(alpha = 0.12f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = TealSecondary, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${day.startTime}  ➔  ${day.endTime}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = TealSecondary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "${calculateTotalDayHours(day)}h",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TealSecondary,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECCIÓN 3: HISTORIAL DE FICHAJES ---
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
                        Surface(
                            shape = CircleShape,
                            color = TealSecondary.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = TealSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Historial Reciente de Fichajes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text("${clockLogs.size} registros", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(10.dp))

                clockLogs.forEach { log ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.dateFormatted, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "Entrada: ${log.clockInTime} | Salida: ${log.clockOutTime ?: "En curso"} (${log.shiftType})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${log.totalHours}h",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- MODAL DIALOG EDITAR DÍA Y TURNO PARTIDO ---
    if (editingDayIndex != null && editingDayIndex!! in workSchedule.indices) {
        val targetDayIndex = editingDayIndex!!
        val currentDay = workSchedule[targetDayIndex]

        var editIsWorkDay by remember(targetDayIndex) { mutableStateOf(currentDay.isWorkDay) }
        var editShiftType by remember(targetDayIndex) { mutableStateOf(currentDay.shiftType) }
        var editIsSplitShift by remember(targetDayIndex) { mutableStateOf(currentDay.isSplitShift || currentDay.shiftType == "Turno Partido") }

        var editStart1 by remember(targetDayIndex) { mutableStateOf(currentDay.splitStart1) }
        var editEnd1 by remember(targetDayIndex) { mutableStateOf(currentDay.splitEnd1) }
        var editStart2 by remember(targetDayIndex) { mutableStateOf(currentDay.splitStart2) }
        var editEnd2 by remember(targetDayIndex) { mutableStateOf(currentDay.splitEnd2) }

        var editStartTime by remember(targetDayIndex) { mutableStateOf(currentDay.startTime) }
        var editEndTime by remember(targetDayIndex) { mutableStateOf(currentDay.endTime) }
        var editNote by remember(targetDayIndex) { mutableStateOf(currentDay.note) }

        // Live calculated target hours
        val calculatedHours = if (!editIsWorkDay || editShiftType == "Libre") 0.0 else {
            if (editIsSplitShift) {
                val h1 = calculateHoursBetween(editStart1, editEnd1)
                val h2 = calculateHoursBetween(editStart2, editEnd2)
                val sum = h1 + h2
                if (sum > 0) sum else 8.0
            } else {
                val h = calculateHoursBetween(editStartTime, editEndTime)
                if (h > 0) h else 8.0
            }
        }

        AlertDialog(
            onDismissRequest = { editingDayIndex = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AccentAmber,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    currentDay.dayCode.take(2),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Configurar ${currentDay.dayName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${calculatedHours}h laborables",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Switch Día Laborable
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("¿Es Día Laborable?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Switch(
                                checked = editIsWorkDay,
                                onCheckedChange = { editIsWorkDay = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                            )
                        }
                    }

                    if (editIsWorkDay) {
                        // Selección de Tipo de Jornada
                        Text("Tipo de Jornada:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val types = listOf("Turno Partido", "Mañana", "Tarde", "Noche", "Teletrabajo")
                            items(types) { t ->
                                val isSel = (t == "Turno Partido" && editIsSplitShift) || (t == editShiftType && !editIsSplitShift)
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        editShiftType = t
                                        editIsSplitShift = (t == "Turno Partido")
                                    },
                                    label = { Text(if (t == "Turno Partido") "⚡ Turno Partido" else t, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (t == "Turno Partido") AccentAmber else EmeraldPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Presets para Turno Partido
                        if (editIsSplitShift) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AccentAmber.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Plantillas para Turno Partido:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentAmber)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                editStart1 = "09:00"
                                                editEnd1 = "14:00"
                                                editStart2 = "16:00"
                                                editEnd2 = "19:00"
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("09-14 / 16-19 (8h)", fontSize = 9.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                editStart1 = "08:30"
                                                editEnd1 = "13:30"
                                                editStart2 = "15:30"
                                                editEnd2 = "18:30"
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("08:30-13:30 / 15:30-18:30", fontSize = 9.sp)
                                        }
                                    }
                                }
                            }

                            // Inputs Tramo 1
                            Text("Tramo 1 (Mañana):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = editStart1,
                                    onValueChange = { editStart1 = it },
                                    label = { Text("Entrada 1 (Ej: 09:00)", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                                )

                                OutlinedTextField(
                                    value = editEnd1,
                                    onValueChange = { editEnd1 = it },
                                    label = { Text("Salida 1 (Ej: 14:00)", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                                )
                            }

                            // Inputs Tramo 2
                            Text("Tramo 2 (Tarde):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = editStart2,
                                    onValueChange = { editStart2 = it },
                                    label = { Text("Entrada 2 (Ej: 16:00)", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                                )

                                OutlinedTextField(
                                    value = editEnd2,
                                    onValueChange = { editEnd2 = it },
                                    label = { Text("Salida 2 (Ej: 19:00)", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                                )
                            }
                        } else {
                            // Inputs Jornada Continua
                            Text("Horario Continuo:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = editStartTime,
                                    onValueChange = { editStartTime = it },
                                    label = { Text("Hora Entrada", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = editEndTime,
                                    onValueChange = { editEndTime = it },
                                    label = { Text("Hora Salida", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }

                        // Nota opcional
                        OutlinedTextField(
                            value = editNote,
                            onValueChange = { editNote = it },
                            label = { Text("Nota o Ubicación (Ej: Oficina central, Remoto...)", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = currentDay.copy(
                            isWorkDay = editIsWorkDay,
                            shiftType = if (editIsSplitShift) "Turno Partido" else editShiftType,
                            isSplitShift = editIsSplitShift,
                            splitStart1 = editStart1.ifBlank { "09:00" },
                            splitEnd1 = editEnd1.ifBlank { "14:00" },
                            splitStart2 = editStart2.ifBlank { "16:00" },
                            splitEnd2 = editEnd2.ifBlank { "19:00" },
                            startTime = editStartTime.ifBlank { "08:00" },
                            endTime = editEndTime.ifBlank { "17:00" },
                            targetHours = calculatedHours,
                            note = editNote
                        )
                        workSchedule[targetDayIndex] = updated
                        saveScheduleToPrefs(context, workSchedule)
                        editingDayIndex = null
                        Toast.makeText(context, "✅ Horario de ${currentDay.dayName} guardado correctamente", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingDayIndex = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
