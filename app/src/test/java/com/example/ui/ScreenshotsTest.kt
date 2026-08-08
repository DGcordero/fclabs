package com.example.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import android.app.Application
import com.example.data.TaskEntity
import com.example.data.HabitEntity
import com.example.data.TaskCategory
import com.example.data.TaskPriority
import com.example.security.SecurityManager
import com.example.ui.components.AddEditTaskSheet
import com.example.ui.components.CategoryChipGroup
import com.example.ui.components.EisenhowerMatrixView
import com.example.ui.components.HabitsTrackerView
import com.example.ui.components.OperationsDashboardView
import com.example.ui.theme.CorderoFTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ScreenshotsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var securityManager: SecurityManager

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        securityManager = SecurityManager(application)
        securityManager.setPinLockEnabled(false)
    }

    private val sampleTasks = listOf(
        TaskEntity(
            id = 1,
            title = "Misión de Reconocimiento Alpha",
            description = "Explorar sector norte de la base de datos.",
            category = TaskCategory.TRABAJO.name,
            priority = TaskPriority.ALTA.name,
            dueDateEpochMs = System.currentTimeMillis() + 86400000,
            dueTimeFormatted = "14:30",
            isPinned = true
        ),
        TaskEntity(
            id = 2,
            title = "Abastecimiento de Provisiones",
            description = "Comprar insumos médicos y raciones de supervivencia.",
            category = TaskCategory.SALUD.name,
            priority = TaskPriority.MEDIA.name,
            isCompleted = true
        )
    )

    private val sampleHabits = listOf(
        HabitEntity(
            id = 1,
            title = "Entrenamiento de Combate (Gimnasio)",
            category = "Ejercicio",
            target = "1 hora al día",
            isCompletedToday = true,
            streakDays = 5
        ),
        HabitEntity(
            id = 2,
            title = "Estudio Técnico de Tácticas",
            category = "Lectura",
            target = "15 páginas",
            isCompletedToday = false,
            streakDays = 3
        )
    )

    @Test
    fun testOperationsDashboardViewScreenshot() {
        composeTestRule.setContent {
            CorderoFTheme {
                Surface {
                    OperationsDashboardView(
                        tasks = sampleTasks,
                        stats = TaskStats(total = 2, completed = 1, pending = 1, highPriority = 1, overdue = 0),
                        securityManager = securityManager,
                        searchQuery = "",
                        onSearchQueryChange = {},
                        selectedCategory = TaskCategory.TODAS,
                        onCategorySelected = {},
                        showCompletedOnly = false,
                        onToggleCompletedOnly = {},
                        sortOption = SortOption.FECHA,
                        onSortOptionSelected = {},
                        onLockApp = {},
                        onOpenSecuritySettings = {},
                        onAddNewTask = {},
                        onTaskClick = {},
                        onToggleTaskComplete = {},
                        onToggleTaskPin = {},
                        onOpenSmartAssistant = {},
                        onSwitchTab = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "../docs/screenshots/OperationsDashboardView.png")
    }

    @Test
    fun testCategoryChipGroupScreenshot() {
        composeTestRule.setContent {
            CorderoFTheme {
                Surface {
                    CategoryChipGroup(
                        selectedCategory = TaskCategory.TRABAJO,
                        onCategorySelected = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "../docs/screenshots/CategoryChipGroup.png")
    }

    @Test
    fun testEisenhowerMatrixViewScreenshot() {
        composeTestRule.setContent {
            CorderoFTheme {
                Surface {
                    EisenhowerMatrixView(
                        tasks = sampleTasks,
                        onQuickAddParsedTask = { _, _, _, _, _, _, _ -> },
                        onTaskClick = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "../docs/screenshots/EisenhowerMatrixView.png")
    }

    @Test
    fun testHabitsTrackerViewScreenshot() {
        composeTestRule.setContent {
            CorderoFTheme {
                Surface {
                    HabitsTrackerView(
                        habits = sampleHabits,
                        onAddHabit = { _, _, _ -> },
                        onToggleHabit = {},
                        onDeleteHabit = {},
                        onTriggerNotification = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "../docs/screenshots/HabitsTrackerView.png")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun testAddEditTaskSheetScreenshot() {
        composeTestRule.setContent {
            CorderoFTheme {
                Scaffold { padding ->
                    AddEditTaskSheet(
                        task = sampleTasks[0],
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        onDismiss = {},
                        onSave = { _, _, _, _, _, _, _, _, _, _ -> }
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "../docs/screenshots/AddEditTaskSheet.png")
    }
}
