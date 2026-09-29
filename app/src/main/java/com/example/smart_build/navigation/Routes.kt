package com.example.smart_build.navigation

import android.net.Uri

sealed class Routes(val route: String) {
  data object LoginPage : Routes("ap")
  data object HomePage : Routes("hp")
  data object ComponentSearch : Routes("search")

  /** Compose Guided/Assessment for Modules 2–4. */
  data object ComposeModule : Routes("cm/{moduleId}/{simulationType}") {
    fun createRoute(moduleId: Int, simulationType: Int): String = "cm/$moduleId/$simulationType"
  }

  /** Godot ModulePage for Modules 0–1. */
  data object ModulePage : Routes("mp/{moduleId}/{moduleName}/{simulationType}/{progress}") {
    fun createRoute(moduleId: Int, moduleName: String, simulationType: Int, progress: Float): String {
      val safeName = Uri.encode(moduleName)
      return "mp/$moduleId/$safeName/$simulationType/$progress"
    }
  }
}
