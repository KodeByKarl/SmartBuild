package com.example.smart_build.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smart_build.screens.authenticationpage.AuthPage
import com.example.smart_build.screens.homepage.HomePage
import com.example.smart_build.viewmodel.auth.AuthStatusState
import com.example.smart_build.viewmodel.auth.AuthViewModel1

@Composable
fun AppNav(navController : NavHostController, authViewModel1: AuthViewModel1) {
  val uiState by authViewModel1.uiState.collectAsStateWithLifecycle()

//  LaunchedEffect(uiState) {
//
//    when (uiState) {
//
//      AuthStatusState.SignedIn -> {
//
//        navController.navigate(
//          Routes.HomePage.route
//        ) {
//          popUpTo(
//            Routes.LoginPage.route
//          ) {
//            inclusive = true
//          }
//          launchSingleTop = true
//        }
//      }
//
//      AuthStatusState.SignedOut -> {
//
//        navController.navigate(
//          Routes.LoginPage.route
//        ) {
//          popUpTo(
//            Routes.HomePage.route
//          ) {
//            inclusive = true
//          }
//          launchSingleTop = true
//        }
//      }
//
//      else -> {
//        // Loading, Submitting, Error, etc.
//      }
//    }
//  }

  NavHost(navController = navController, startDestination = Routes.LoginPage.route) {
    composable(Routes.LoginPage.route) {
      AuthPage(navController, authViewModel1)
    }
    composable(Routes.HomePage.route) {
      HomePage(navController)
    }
  }
}