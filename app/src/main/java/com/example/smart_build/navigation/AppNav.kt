package com.example.smart_build.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smart_build.screens.authenticationpage.AuthPage
import com.example.smart_build.screens.homepage.HomePage
import com.example.smart_build.screens.modulepage.ModulePage
import com.example.smart_build.screens.search.ComponentSearchPage
import com.example.smart_build.viewmodel.auth.AuthStatusState
import com.example.smart_build.viewmodel.auth.AuthViewModel

@Composable
fun AppNav(navController: NavHostController, authViewModel1: AuthViewModel) {
  val uiState by authViewModel1.uiState.collectAsStateWithLifecycle()

  // Prototype flow: Sign In / Sign Up → Dashboard (HomePage) first — never straight into a module.
  LaunchedEffect(uiState) {
    when (uiState) {
      AuthStatusState.SignedIn -> {
        val current = navController.currentDestination?.route
        if (current == Routes.LoginPage.route || current == null) {
          navController.navigate(Routes.HomePage.route) {
            popUpTo(Routes.LoginPage.route) { inclusive = true }
            launchSingleTop = true
          }
        }
      }

      AuthStatusState.SignedOut -> {
        val current = navController.currentDestination?.route
        if (current != null && current != Routes.LoginPage.route) {
          navController.navigate(Routes.LoginPage.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
          }
        }
      }

      else -> Unit
    }
  }

  NavHost(navController = navController, startDestination = Routes.LoginPage.route) {
    composable(Routes.LoginPage.route) {
      AuthPage(navController, authViewModel1)
    }
    composable(Routes.HomePage.route) {
      HomePage(navController)
    }
    composable(Routes.ComponentSearch.route) {
      ComponentSearchPage(navController)
    }
    composable(
      route = Routes.ModulePage.route,
      arguments = listOf(
        navArgument("moduleId") { type = NavType.IntType },
        navArgument("moduleName") { type = NavType.StringType },
        navArgument("simulationType") { type = NavType.IntType },
        navArgument("progress") { type = NavType.FloatType }
      )
    ) { backStackEntry ->

      val moduleId = backStackEntry.arguments?.getInt("moduleId")
      val moduleName = backStackEntry.arguments?.getString("moduleName")
      val simulationType = backStackEntry.arguments?.getInt("simulationType")
      val progress = backStackEntry.arguments?.getFloat("progress")

      ModulePage(
        navController = navController,
        moduleId = moduleId!!,
        moduleName = moduleName!!,
        simulationType = simulationType!!,
        progress = progress!!
      )
    }
  }
}
