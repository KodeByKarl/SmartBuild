package com.example.smart_build.screens.modulepage

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smart_build.SmartBuildBridge
import com.example.smart_build.SmartBuildMessage
import com.example.smart_build.components.ModuleName
import com.example.smart_build.godot.GodotTestScreen
import com.example.smart_build.navigation.Routes
import com.example.smart_build.screens.modulepage.components.ModuleNameWithLoadingProgress
import com.example.smart_build.ui.theme.Black
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White
import com.example.smart_build.viewmodel.module.GodotEnvState
import com.example.smart_build.viewmodel.module.ModuleViewModel
import org.json.JSONObject

@Composable
fun ModulePage(
  navController : NavHostController,
  moduleId: Int,
  moduleName: String,
  simulationType: Int,
  progress: Float,
  viewModel : ModuleViewModel = viewModel()
) {
  val godotEnvState = viewModel.godotEnvState.collectAsStateWithLifecycle()
  var godotMessage by remember { mutableStateOf("") }
  var loadingMessage by remember { mutableStateOf("SETTING UP YOUR ENVIRONMENT...") }

  Log.d("GODOT_COMM", "State: $godotEnvState")
  Log.d("GODOT_COMM", "Godot Message: $godotMessage")

//  LaunchedEffect(moduleId, simulationType, progress) {
//    viewModel.prepareGodot(
//      moduleId = moduleId,
//      simulationType = simulationType,
//      progress = progress
//    )
//  }

  LaunchedEffect(godotMessage) {
    Log.d("GODOT_COMM", "State1: $godotEnvState")
    Log.d("GODOT_COMM", "Godot Message1: $godotMessage")

    if(godotMessage.isEmpty()) return@LaunchedEffect

    try {
      val json = JSONObject(godotMessage)
      val event = json.optString("event")

      when(event) {
        "destroy" -> {
          navController.navigate(Routes.HomePage.route) {
            popUpTo(Routes.ModulePage.createRoute(moduleId, moduleName, simulationType, progress)) {
              inclusive = true
            }
          }
        }

        "engine_initialized" -> {
//          loadingMessage = "ENGINE STARTING..."
          loadingMessage = "SETTING UP YOUR ENVIRONMENT..."

          viewModel.prepareGodot(moduleId, simulationType, progress)
        }

        "loading" -> { loadingMessage = "Loading Module $moduleId".uppercase() }

        "ready" -> { viewModel.changeGodotEnvState(GodotEnvState.Ready) }
      }
    } catch(e: Exception) {
      Log.e("GODOT_COMM", "Failed to process godotMessage.")
    }

    Log.d("GODOT_COMM", "State1: $godotEnvState")
  }

  LaunchedEffect(Unit) {
    SmartBuildBridge.godotMessages.collect { message ->
      Log.d("GODOT_COMM", "Compose received: $message")

      godotMessage = message
    }
  }

  BoxWithConstraints(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .fillMaxSize()
      .background(Black)
      .border(1.dp, Color.Red)
  ) {
    val MAX_WIDTH = maxWidth
    val MAX_HEIGHT = maxHeight

    GodotTestScreen()

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
//            "SETTING UP YOUR ENVIRONMENT...",
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
  }
}