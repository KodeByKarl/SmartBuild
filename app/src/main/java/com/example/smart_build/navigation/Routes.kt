package com.example.smart_build.navigation

sealed class Routes(val route: String) {
  data object LoginPage : Routes("ap")
  data object HomePage : Routes("hp")
  data object ModulePage : Routes("mp/{moduleId}/{moduleName}/{simulationType}/{progress}") {
    fun createRoute(moduleId: Int, moduleName: String, simulationType: Int, progress: Float): String {
      return "mp/$moduleId/$moduleName/$simulationType/$progress"
    }
  }
}