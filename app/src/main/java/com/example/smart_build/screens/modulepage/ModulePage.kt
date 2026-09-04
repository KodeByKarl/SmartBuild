package com.example.smart_build.screens.modulepage

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smart_build.SmartBuildBridge
import com.example.smart_build.components.ModuleName
import com.example.smart_build.data.ModuleProgressStore
import com.example.smart_build.navigation.Routes
import com.example.smart_build.ui.theme.Black
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White
import com.example.smart_build.viewmodel.module.GodotEnvState
import com.example.smart_build.viewmodel.module.ModuleViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import org.json.JSONObject

private const val PLUGIN_WAIT_MS = 30_000L
private const val ENGINE_WAIT_MS = 90_000L
private const val PREPARE_ACK_MS = 120_000L

@Composable
fun ModulePage(
  navController: NavHostController,
  moduleId: Int,
  moduleName: String,
  simulationType: Int,
  progress: Float,
  viewModel: ModuleViewModel = viewModel()
) {
  val godotEnvState = viewModel.godotEnvState.collectAsStateWithLifecycle()
  var godotMessage by remember { mutableStateOf("") }
  val context = LocalContext.current
  var loadingMessage by remember { mutableStateOf("SETTING UP YOUR ENVIRONMENT...") }
  var prepareAttempt by remember { mutableStateOf(0) }
  var acceptModuleEvents by remember { mutableStateOf(false) }

  DisposableEffect(Unit) {
    onDispose { SmartBuildBridge.setGodotSurfaceVisible(false) }
  }

  LaunchedEffect(godotEnvState.value) {
    SmartBuildBridge.setGodotSurfaceVisible(godotEnvState.value is GodotEnvState.Ready)
  }

  // Always send prepare when module params change — do not rely on engine_initialized alone.
  LaunchedEffect(moduleId, simulationType, progress, prepareAttempt) {
    acceptModuleEvents = false
    viewModel.changeGodotEnvState(GodotEnvState.Preparing)
    loadingMessage = "SETTING UP YOUR ENVIRONMENT..."

    val pluginReady = withTimeoutOrNull(PLUGIN_WAIT_MS) {
      while (!SmartBuildBridge.isPluginReady()) {
        delay(250)
      }
      true
    } ?: false

    if (!pluginReady) {
      Log.e("GODOT_COMM", "Timed out waiting for SmartBuildGodotPlugin")
      viewModel.changeGodotEnvState(GodotEnvState.Error)
      return@LaunchedEffect
    }

    if (!SmartBuildBridge.engineInitialized) {
      loadingMessage = "STARTING SIMULATION ENGINE..."
      val engineReady = withTimeoutOrNull(ENGINE_WAIT_MS) {
        while (!SmartBuildBridge.engineInitialized) {
          delay(250)
        }
        true
      } ?: false

      if (!engineReady) {
        Log.e("GODOT_COMM", "Timed out waiting for engine_initialized")
        viewModel.changeGodotEnvState(GodotEnvState.Error)
        return@LaunchedEffect
      }
    }

    delay(120)

    // Subscribe BEFORE prepare. loading+ready are emitted back-to-back; a waiter
    // that starts after prepare misses ready (replay=0) and then paints Error.
    coroutineScope {
      val ackDeferred = async {
        SmartBuildBridge.godotMessages
          .map { raw -> JSONObject(raw) }
          .first { json ->
            val event = json.optString("event")
            val mid = json.optInt("moduleId", -1)
            mid == moduleId && (event == "ready" || event == "error")
          }
      }
      yield()
      viewModel.prepareGodot(moduleId, simulationType, progress)
      loadingMessage = "LOADING MODULE $moduleId..."

      val ack = withTimeoutOrNull(PREPARE_ACK_MS) { ackDeferred.await() }
      val event = ack?.optString("event")
      when (event) {
        "ready" -> {
          acceptModuleEvents = true
          viewModel.changeGodotEnvState(GodotEnvState.Ready)
        }
        "error" -> {
          Log.e("GODOT_COMM", "Godot reported load error")
          viewModel.changeGodotEnvState(GodotEnvState.Error)
        }
        else -> {
          if (acceptModuleEvents || viewModel.godotEnvState.value is GodotEnvState.Ready) {
            acceptModuleEvents = true
            viewModel.changeGodotEnvState(GodotEnvState.Ready)
          } else {
            Log.e("GODOT_COMM", "Prepare got no ack")
            viewModel.changeGodotEnvState(GodotEnvState.Error)
          }
        }
      }
    }
  }

  LaunchedEffect(godotMessage) {
    if (godotMessage.isEmpty()) return@LaunchedEffect

    try {
      val json = JSONObject(godotMessage)
      val event = json.optString("event")
      val eventModuleId = json.optInt("moduleId", -1)

      when (event) {
        "destroy" -> {
          if (eventModuleId != -1 && eventModuleId != moduleId) return@LaunchedEffect
          if (!acceptModuleEvents) return@LaunchedEffect
          val returned = navController.popBackStack(Routes.HomePage.route, inclusive = false)
          if (!returned) {
            navController.navigate(Routes.HomePage.route) { launchSingleTop = true }
          }
        }

        "guided_completed" -> {
          if (eventModuleId != moduleId || !acceptModuleEvents) return@LaunchedEffect
          ModuleProgressStore.markGuidedCompleted(context, moduleId)
          Toast.makeText(context, "Guided Simulation saved. Assessment unlocked (same pages, no guides).", Toast.LENGTH_SHORT).show()
          val returned = navController.popBackStack(Routes.HomePage.route, inclusive = false)
          if (!returned) {
            navController.navigate(Routes.HomePage.route) { launchSingleTop = true }
          }
        }

        "assessment_completed" -> {
          if (eventModuleId != moduleId || !acceptModuleEvents) return@LaunchedEffect
          if (moduleId == 0) {
            ModuleProgressStore.markIntroCompleted(context)
          } else {
            ModuleProgressStore.markAssessmentCompleted(context, moduleId)
          }
          Toast.makeText(context, "Module marked complete.", Toast.LENGTH_SHORT).show()
          val returned = navController.popBackStack(Routes.HomePage.route, inclusive = false)
          if (!returned) {
            navController.navigate(Routes.HomePage.route) { launchSingleTop = true }
          }
        }

        "progress_update" -> {
          if (eventModuleId != moduleId) return@LaunchedEffect
          val percent = json.optDouble("percent", -1.0).toFloat()
          if (percent >= 0f) {
            ModuleProgressStore.setProgressPercent(context, moduleId, percent)
          }
        }

        "engine_initialized" -> {
          loadingMessage = "SETTING UP YOUR ENVIRONMENT..."
        }

        "loading" -> {
          if (eventModuleId == moduleId || eventModuleId == -1) {
            loadingMessage = "LOADING MODULE $moduleId..."
          }
        }

        "ready" -> {
          if (eventModuleId == moduleId) {
            acceptModuleEvents = true
            viewModel.changeGodotEnvState(GodotEnvState.Ready)
          }
        }

        "error" -> {
          if (eventModuleId == moduleId || eventModuleId == -1) {
            viewModel.changeGodotEnvState(GodotEnvState.Error)
          }
        }
      }
    } catch (e: Exception) {
      Log.e("GODOT_COMM", "Failed to process godotMessage.", e)
    }
  }

  LaunchedEffect(Unit) {
    SmartBuildBridge.godotMessages.collect { message ->
      Log.d("GODOT_COMM", "Compose received: $message")
      godotMessage = message
    }
  }

  BoxWithConstraints(
    contentAlignment = Alignment.Center,
    modifier = Modifier.fillMaxSize()
  ) {
    val MAX_WIDTH = maxWidth
    val MAX_HEIGHT = maxHeight

    AnimatedVisibility(
      visible = godotEnvState.value is GodotEnvState.Preparing,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .fillMaxSize()
          .background(Black)
      ) {
        ModuleName(
          moduleName = moduleName,
          maxWidth = MAX_WIDTH,
          variant = 3
        )

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.offset(y = (MAX_HEIGHT.value * 0.4f).dp)
        ) {
          Text(
            loadingMessage,
            style = Typography.headlineSmall.copy(fontFamily = GSFlex, fontWeight = FontWeight.Normal),
            color = White,
            fontSize = (MAX_WIDTH.value * 0.019f).sp,
            letterSpacing = ((MAX_WIDTH.value * 0.019f) * 0.12f).sp,
            lineHeight = ((MAX_WIDTH.value * 0.019f) * 1.2f).sp
          )

          Spacer(Modifier.size((MAX_HEIGHT.value * 0.005f).dp))

          LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f))
        }
      }
    }

    AnimatedVisibility(
      visible = godotEnvState.value is GodotEnvState.Error,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .fillMaxSize()
          .background(Black)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            "Could not load the simulation.",
            style = Typography.titleMedium,
            color = White,
            fontSize = (MAX_WIDTH.value * 0.02f).sp
          )
          Spacer(Modifier.size((MAX_HEIGHT.value * 0.02f).dp))
          Button(onClick = { prepareAttempt += 1 }) {
            Text("Try again")
          }
          Spacer(Modifier.size((MAX_HEIGHT.value * 0.015f).dp))
          Button(onClick = { navController.popBackStack() }) {
            Text("Back to Home")
          }
        }
      }
    }
  }
}
