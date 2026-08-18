package com.example.smart_build.viewmodel.module

sealed interface GodotEnvState {
  data object Preparing: GodotEnvState
  data object Ready: GodotEnvState
  data object Error: GodotEnvState
}