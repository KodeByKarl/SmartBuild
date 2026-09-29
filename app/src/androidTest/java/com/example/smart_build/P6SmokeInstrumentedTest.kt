package com.example.smart_build

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.smart_build.data.ModuleProgressStore
import com.example.smart_build.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * P6 smoke: progress still saves; Login→Home→M0–M1 Godot / M2–M4 Compose routes stay navigable.
 * Run on device/emulator: `./gradlew :app:connectedDebugAndroidTest`
 */
@RunWith(AndroidJUnit4::class)
class P6SmokeInstrumentedTest {

  @Test
  fun guidedAndAssessmentProgressPersistsMonotonically() {
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    val uid = "p6-smoke-${System.currentTimeMillis()}"
    ModuleProgressStore.bindAccount(ctx, uid)

    assertEquals(0f, ModuleProgressStore.progress(ctx, 2), 0.01f)
    assertFalse(ModuleProgressStore.guidedDone(ctx, 2))
    assertFalse(ModuleProgressStore.assessmentDone(ctx, 2))

    ModuleProgressStore.setProgressPercent(ctx, 2, 40f)
    assertEquals(40f, ModuleProgressStore.progress(ctx, 2), 0.01f)

    // Never decrease.
    ModuleProgressStore.setProgressPercent(ctx, 2, 15f)
    assertEquals(40f, ModuleProgressStore.progress(ctx, 2), 0.01f)

    ModuleProgressStore.markGuidedCompleted(ctx, 2)
    assertTrue(ModuleProgressStore.guidedDone(ctx, 2))
    assertTrue(ModuleProgressStore.progress(ctx, 2) >= 50f)
    assertTrue(ModuleProgressStore.progress(ctx, 2) <= 99f)
    assertFalse(ModuleProgressStore.assessmentDone(ctx, 2))

    ModuleProgressStore.markAssessmentCompleted(ctx, 2)
    assertTrue(ModuleProgressStore.assessmentDone(ctx, 2))
    assertEquals(100f, ModuleProgressStore.progress(ctx, 2), 0.01f)

    // Assessment done freezes percent.
    ModuleProgressStore.setProgressPercent(ctx, 2, 50f)
    assertEquals(100f, ModuleProgressStore.progress(ctx, 2), 0.01f)

    // Retake must wipe progress so reopen starts at page 1.
    ModuleProgressStore.resetForRetake(ctx, 2)
    assertEquals(0f, ModuleProgressStore.progress(ctx, 2), 0.01f)
    assertFalse(ModuleProgressStore.guidedDone(ctx, 2))
    assertFalse(ModuleProgressStore.assessmentDone(ctx, 2))
  }

  @Test
  fun loginHomeModuleRoutesCoverM0ToM4() {
    assertEquals("ap", Routes.LoginPage.route)
    assertEquals("hp", Routes.HomePage.route)

    // M0–M1 → Godot ModulePage
    val godotTitles = listOf(
      0 to "Introduction To Computer Systems Servicing",
      1 to "Installing and Configuring Computer Systems",
    )
    for ((id, name) in godotTitles) {
      val guided = Routes.ModulePage.createRoute(id, name, 0, 0f)
      assertTrue("guided Godot route for M$id", guided.startsWith("mp/$id/"))
      assertTrue("encoded name for M$id", !guided.contains(" "))
      assertTrue(guided.contains("/0/"))

      if (id >= 1) {
        val assess = Routes.ModulePage.createRoute(id, name, 1, 50f)
        assertTrue(assess.contains("/1/"))
        assertTrue(assess.contains("50"))
      }
    }

    // M2–M4 → ComposeModule
    for (id in 2..4) {
      val guided = Routes.ComposeModule.createRoute(id, 0)
      assertEquals("cm/$id/0", guided)
      val assess = Routes.ComposeModule.createRoute(id, 1)
      assertEquals("cm/$id/1", assess)
    }
  }
}
