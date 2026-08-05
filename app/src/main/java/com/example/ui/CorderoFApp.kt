package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TaskCategory
import com.example.data.TaskEntity
import com.example.data.TaskPriority
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import com.example.ui.components.AddEditTaskSheet
import com.example.ui.components.CategoryChipGroup
import com.example.ui.components.EisenhowerMatrixView
import com.example.ui.components.HabitsTrackerView
import com.example.ui.components.PinLockScreen
import com.example.ui.components.QuickStatsBar
import com.example.ui.components.SecuritySettingsDialog
import com.example.ui.components.SmartAssistantSheet
import com.example.ui.components.TacticalCalendarView
import com.example.ui.components.TaskItemCard
import com.example.ui.components.UpcomingAppointmentsPanel
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorderoFApp(
    viewModel: TaskViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe ViewModel state
    val isPinLocked by viewModel.isPinLocked.collectAsState()
    val tasks by viewModel.filteredTasks.collectAsState()
    val stats by viewModel.taskStats.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val showCompletedOnly by viewModel.showCompletedOnly.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    val isAddEditSheetOpen by viewModel.isAddEditSheetOpen.collectAsState()
    val editingTask by viewModel.editingTask.collectAsState()
    val isSecurityDialogOpen by viewModel.isSecurityDialogOpen.collectAsState()
    val isSmartAssistantOpen by viewModel.isSmartAssistantOpen.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Tareas & Citas, 1: Calendario, 2: Matriz IA

    val addEditSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val smartAssistantSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle feedback toasts/snackbars
    LaunchedEffect(Unit) {
        viewModel.userFeedback.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (isPinLocked) {
        PinLockScreen(
            onUnlock = { pin -> viewModel.unlockWithPin(pin) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(EmeraldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cordero F",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Gestión Personal & Privacidad",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Smart Assistant
                    IconButton(
                        onClick = { viewModel.isSmartAssistantOpen.value = true },
                        modifier = Modifier.testTag("assistant_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Asistente Inteligente",
                            tint = EmeraldPrimary
                        )
                    }

                    // Security Settings
                    IconButton(
                        onClick = { viewModel.isSecurityDialogOpen.value = true },
                        modifier = Modifier.testTag("security_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Ajustes de Privacidad",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Quick Lock
                    if (viewModel.securityManager.isPinLockEnabled()) {
                        IconButton(onClick = { viewModel.lockApp() }) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Bloquear App",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddSheet() },
                icon = { Icon(Icons.Default.Add, contentDescription = "Añadir Tarea") },
                text = { Text("Nueva Tarea / Cita", fontWeight = FontWeight.Bold) },
                containerColor = EmeraldPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_task_fab")
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = "Tareas y Citas") },
                    label = { Text("Agenda", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.2f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario") },
                    label = { Text("Calendario", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.2f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Whatshot, contentDescription = "Hábitos y Rutinas") },
                    label = { Text("Hábitos", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.2f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Matriz Eisenhower") },
                    label = { Text("Matriz", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.2f)
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                1 -> {
                    // TAB 1: Calendario Táctico
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            TacticalCalendarView(
                                tasks = tasks,
                                onAddNewAppointmentForDate = { dateMs ->
                                    viewModel.openAddSheetForDate(dateMs)
                                },
                                onTaskClick = { task ->
                                    viewModel.openEditSheet(task)
                                }
                            )
                        }
                    }
                }
                2 -> {
                    // TAB 2: Hábitos & Rutinas Diarias
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            HabitsTrackerView()
                        }
                    }
                }
                3 -> {
                    // TAB 3: Matriz de Priorización Local
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            EisenhowerMatrixView(
                                tasks = tasks,
                                onQuickAddParsedTask = { title, desc, cat, prio, dateMs, timeStr, subtasksList ->
                                    viewModel.saveTask(
                                        id = 0,
                                        title = title,
                                        description = desc,
                                        category = cat,
                                        priority = prio,
                                        dueDateEpochMs = dateMs,
                                        dueTimeFormatted = timeStr,
                                        reminderEpochMs = if (dateMs != null) dateMs else null,
                                        isPinned = false,
                                        subtasks = subtasksList.map { com.example.data.Subtask(id = java.util.UUID.randomUUID().toString(), title = it, isCompleted = false) }
                                    )
                                },
                                onTaskClick = { task ->
                                    viewModel.openEditSheet(task)
                                }
                            )
                        }
                    }
                }
                else -> {
                    // TAB 0: Vista Principal
                    Column(modifier = Modifier.fillMaxSize()) {
            // Dashboard Summary Header
            QuickStatsBar(
                stats = stats,
                isOfflineMode = viewModel.securityManager.isOfflineOnlyMode(),
                onSecurityClick = { viewModel.isSecurityDialogOpen.value = true }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Upcoming Appointments & Pending Tasks Panel
            UpcomingAppointmentsPanel(
                tasks = tasks,
                securityManager = viewModel.securityManager,
                onLockApp = { viewModel.lockApp() },
                onOpenSecuritySettings = { viewModel.isSecurityDialogOpen.value = true },
                onAddNewAppointment = { viewModel.openAddSheet() },
                onTaskClick = { task -> viewModel.openEditSheet(task) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar & Sort Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Buscar tareas o notas...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Sort Dropdown Button
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Ordenar",
                            tint = EmeraldPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
                                onClick = {
                                    viewModel.sortOption.value = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (Categories & Completed toggle)
            CategoryChipGroup(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectedCategory.value = it }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Secondary filters row (Completadas toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = showCompletedOnly,
                    onClick = { viewModel.showCompletedOnly.value = !showCompletedOnly },
                    label = { Text("Mostrar completadas") },
                    leadingIcon = {
                        if (showCompletedOnly) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = EmeraldPrimary
                    )
                )

                Text(
                    text = "${tasks.size} tarea(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tasks List
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier
                                    .height(48.dp)
                                    .width(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No se encontraron tareas para '$searchQuery'" else "¡No hay tareas en esta categoría!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Presiona '+ Nueva Tarea' o usa el asistente para añadir recordatorios inteligentes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = tasks,
                        key = { it.id }
                    ) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                            onTogglePinned = { viewModel.toggleTaskPinned(task) },
                            onEdit = { viewModel.openEditSheet(task) },
                            onDelete = { viewModel.deleteTask(task) },
                            onSubtaskToggle = { subtaskId, isCompleted ->
                                val list = task.getSubtasksList().toMutableList()
                                val idx = list.indexOfFirst { it.id == subtaskId }
                                if (idx != -1) {
                                    list[idx] = list[idx].copy(isCompleted = isCompleted)
                                    val updated = task.copy(subtasksJson = TaskEntity.toJson(list))
                                    viewModel.saveTask(
                                        id = updated.id,
                                        title = updated.title,
                                        description = updated.description,
                                        category = try { TaskCategory.valueOf(updated.category) } catch (e: Exception) { TaskCategory.PERSONAL },
                                        priority = try { TaskPriority.valueOf(updated.priority) } catch (e: Exception) { TaskPriority.MEDIA },
                                        dueDateEpochMs = updated.dueDateEpochMs,
                                        dueTimeFormatted = updated.dueTimeFormatted,
                                        reminderEpochMs = updated.reminderEpochMs,
                                        isPinned = updated.isPinned,
                                        subtasks = list
                                    )
                                }
                            }
                        )
                    }
                }
            }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets & Dialogs
    if (isAddEditSheetOpen) {
        AddEditTaskSheet(
            task = editingTask,
            sheetState = addEditSheetState,
            onDismiss = { viewModel.closeAddEditSheet() },
            onSave = { id, title, desc, cat, prio, dateMs, timeStr, remMs, pinned, subtasks ->
                viewModel.saveTask(
                    id = id,
                    title = title,
                    description = desc,
                    category = cat,
                    priority = prio,
                    dueDateEpochMs = dateMs,
                    dueTimeFormatted = timeStr,
                    reminderEpochMs = remMs,
                    isPinned = pinned,
                    subtasks = subtasks
                )
            }
        )
    }

    if (isSecurityDialogOpen) {
        SecuritySettingsDialog(
            securityManager = viewModel.securityManager,
            onDismiss = { viewModel.isSecurityDialogOpen.value = false },
            onExportBackup = { callback -> viewModel.exportBackup(callback) },
            onImportBackup = { json -> viewModel.importBackup(json) }
        )
    }

    if (isSmartAssistantOpen) {
        SmartAssistantSheet(
            tasks = tasks,
            sheetState = smartAssistantSheetState,
            onDismiss = { viewModel.isSmartAssistantOpen.value = false }
        )
    }
}
