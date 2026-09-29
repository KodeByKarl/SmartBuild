package com.example.smart_build.screens.composemodule

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.smart_build.data.ModuleProgressStore
import com.example.smart_build.data.ReturnToModule
import com.example.smart_build.navigation.Routes
import com.example.smart_build.screens.crimplab.CableMode
import com.example.smart_build.screens.crimplab.CrimpLabScreen
import com.example.smart_build.screens.maintenancelab.MaintenanceLabScreen
import com.example.smart_build.screens.networklab.CableIntroScreen
import com.example.smart_build.screens.networklab.NetworkTopologyScreen
import com.example.smart_build.screens.serverlab.ServerLabScreen
import com.example.smart_build.screens.serverlab.ServerTopologyScreen
import com.example.smart_build.ui.theme.Black

/**
 * Compose Guided/Assessment for Modules 2–4.
 * M2: cable intro → crimp → topology
 * M3: client–server topology (straight-through) → Server Manager
 * M4: maintenance service visit
 *
 * simulationType 0 = Guided Simulation (hints on)
 * simulationType 1 = Scenario Assessment (scenario brief + no yellow coach)
 */
@Composable
fun ComposeModuleScreen(
  navController: NavHostController,
  moduleId: Int,
  simulationType: Int,
) {
  val context = LocalContext.current
  val isAssessment = simulationType == 1
  val hints = !isAssessment
  var showScenario by remember { mutableStateOf(isAssessment) }
  var station by remember { mutableIntStateOf(0) }

  fun goHome() {
    ReturnToModule.mark(moduleId)
    val returned = navController.popBackStack()
    if (!returned) {
      navController.navigate(Routes.HomePage.route) { launchSingleTop = true }
    }
  }

  fun stepBack() {
    when {
      showScenario -> goHome()
      station > 0 -> station -= 1
      isAssessment -> showScenario = true
      else -> goHome()
    }
  }

  fun finishPath() {
    when {
      isAssessment -> {
        ModuleProgressStore.markAssessmentCompleted(context, moduleId)
        Toast.makeText(context, "Scenario Assessment complete.", Toast.LENGTH_SHORT).show()
      }
      else -> {
        ModuleProgressStore.markGuidedCompleted(context, moduleId)
        Toast.makeText(
          context,
          "Guided Simulation saved. Scenario Assessment unlocked.",
          Toast.LENGTH_SHORT,
        ).show()
      }
    }
    goHome()
  }

  fun bumpProgress(fractionOfModule: Float) {
    val pct = (fractionOfModule * 99f).coerceIn(0f, 99f)
    ModuleProgressStore.setProgressPercent(context, moduleId, pct)
  }

  BackHandler { stepBack() }

  Box(modifier = Modifier.fillMaxSize().background(Black)) {
    if (showScenario) {
      ScenarioBriefScreen(
        scenario = AssessmentScenarios.forModule(moduleId),
        onBegin = { showScenario = false },
        onBack = { stepBack() },
      )
    } else {
      when (moduleId) {
        2 -> when (station) {
          0 -> CableIntroScreen(
            navController = navController,
            embedded = true,
            onLeave = { stepBack() },
            onContinue = {
              bumpProgress(0.2f)
              station = 1
            },
          )
          1 -> CrimpLabScreen(
            navController = navController,
            hints = hints,
            forcedMode = CableMode.STRAIGHT,
            allowModeSwitch = hints,
            embedded = true,
            onLeave = { stepBack() },
            onStationComplete = {
              bumpProgress(0.55f)
              station = 2
            },
          )
          else -> NetworkTopologyScreen(
            navController = navController,
            hints = hints,
            embedded = true,
            startFaulted = isAssessment,
            onLeave = { stepBack() },
            onStationComplete = {
              bumpProgress(0.99f)
              finishPath()
            },
          )
        }

        3 -> when (station) {
          0 -> ServerTopologyScreen(
            navController = navController,
            hints = hints,
            embedded = true,
            startFaulted = isAssessment,
            onLeave = { stepBack() },
            onStationComplete = {
              bumpProgress(0.45f)
              station = 1
            },
          )
          else -> ServerLabScreen(
            navController = navController,
            hints = hints,
            embedded = true,
            startFaulted = isAssessment,
            onLeave = { stepBack() },
            onStationComplete = {
              bumpProgress(0.99f)
              finishPath()
            },
          )
        }

        4 -> MaintenanceLabScreen(
          navController = navController,
          hints = hints,
          embedded = true,
          startFaulted = isAssessment,
          onLeave = { stepBack() },
          onStationComplete = {
            bumpProgress(0.99f)
            finishPath()
          },
        )

        else -> goHome()
      }
    }
  }
}
