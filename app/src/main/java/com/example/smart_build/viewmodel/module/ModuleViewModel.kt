package com.example.smart_build.viewmodel.module

import androidx.lifecycle.ViewModel
import com.example.smart_build.SmartBuildBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ModuleViewModel: ViewModel() {
  private var _godotEnvState = MutableStateFlow<GodotEnvState>(GodotEnvState.Preparing)
  val godotEnvState = _godotEnvState.asStateFlow()

  fun prepareGodot(moduleId: Int, simulationType: Int, progress: Float) {
    SmartBuildBridge.prepare(
      moduleId = moduleId,
      simulationType = simulationType,
      progress = progress
    )
  }

  fun changeGodotEnvState(state: GodotEnvState) {
    _godotEnvState.value = state
  }
}