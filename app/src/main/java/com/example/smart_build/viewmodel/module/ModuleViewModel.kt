package com.example.smart_build.viewmodel.module

import androidx.lifecycle.ViewModel
import com.example.smart_build.SmartBuildBridge
import com.example.smart_build.data.client.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ModuleViewModel: ViewModel() {
  private var _godotEnvState = MutableStateFlow<GodotEnvState>(GodotEnvState.Preparing)
  val godotEnvState = _godotEnvState.asStateFlow()

  fun prepareGodot(moduleId: Int, simulationType: Int, progress: Float) {
    val session = runCatching {
      SupabaseClient.client.auth.currentSessionOrNull()
    }.getOrNull()
    val user = runCatching {
      SupabaseClient.client.auth.currentUserOrNull()
    }.getOrNull()

    SmartBuildBridge.prepare(
      moduleId = moduleId,
      simulationType = simulationType,
      progress = progress,
      accessToken = session?.accessToken,
      refreshToken = session?.refreshToken,
      userId = user?.id,
      userEmail = user?.email,
    )
  }

  fun changeGodotEnvState(state: GodotEnvState) {
    _godotEnvState.value = state
  }
}