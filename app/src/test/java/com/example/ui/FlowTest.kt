package com.example.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import android.app.Application
import android.content.Context
import com.example.ui.theme.CorderoFTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class FlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        // Clear all security preferences to ensure no PIN lock leaks from other tests
        context.getSharedPreferences("corderof_security_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun testAppLoadsAndCanSearch() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = TaskViewModel(application)

        // Explicitly force PIN lock off
        viewModel.securityManager.setPinLockEnabled(false)
        viewModel.isPinLocked.value = false

        composeTestRule.setContent {
            CorderoFTheme {
                CorderoFApp(viewModel = viewModel)
            }
        }

        // Verify the operations dashboard view exists
        composeTestRule.onNodeWithTag("operations_dashboard_view").assertExists()

        // Scroll to search_input to ensure it gets composed in the lazy layout
        composeTestRule.onNodeWithTag("operations_dashboard_view")
            .performScrollToNode(hasTestTag("search_input"))

        // Verify search input exists and can accept text
        composeTestRule.onNodeWithTag("search_input").assertExists()
        composeTestRule.onNodeWithTag("search_input").performTextInput("recon")
    }
}
