package com.bool.gymtracker

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bool.gymtracker.data.local.AppDatabase
import com.bool.gymtracker.data.local.SettingsEntity
import com.bool.gymtracker.data.seed.ExerciseSeed
import com.bool.gymtracker.di.AppContainer
import com.bool.gymtracker.di.LocalAppContainer
import com.bool.gymtracker.ui.GymTrackerRoot
import com.bool.gymtracker.ui.theme.GymTrackerTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w420dp-h900dp-xhdpi")
class V1UiFlowTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun createWorkoutLogSetSeeRestHistoryAndProgress() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        runBlocking {
            db.exerciseDao().insertAll(ExerciseSeed.builtIn())
            db.settingsDao().upsert(SettingsEntity(defaultRestSeconds = 90))
        }
        val container = AppContainer(context, db)

        compose.setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                GymTrackerTheme { GymTrackerRoot() }
            }
        }

        compose.onNodeWithText("Create workout").performClick()
        compose.onNodeWithText("Add exercise").performClick()
        compose.onNodeWithText("Search").performTextInput("Barbell bench")
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Barbell bench press").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Barbell bench press").performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Start").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithContentDescription("Complete set").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onAllNodesWithContentDescription("Complete set")[0].performClick()
        compose.onNodeWithText("1:30").assertIsDisplayed()
        compose.onNodeWithText("Finish").performClick()
        compose.onNodeWithText("Finish workout?").assertIsDisplayed()
        compose.onAllNodesWithText("Finish")[1].performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Start").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("History").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("New workout").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("New workout").assertIsDisplayed()
        compose.onNodeWithText("Progress").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Search exercise").fetchSemanticsNodes().isNotEmpty()
        }
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Barbell bench press").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Barbell bench press").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithTag("lastTopSetValue").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("lastTopSetValue").assertIsDisplayed()
        compose.onNodeWithTag("lastTopSetValue").assertTextEquals("0 kg × 0")
        db.close()
    }
}