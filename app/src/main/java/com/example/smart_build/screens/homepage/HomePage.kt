package com.example.smart_build.screens.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smart_build.navigation.Routes
import com.example.smart_build.screens.homepage.components.ModuleCarousel
import com.example.smart_build.screens.homepage.components.ProfileOverlay
import com.example.smart_build.screens.homepage.components.TopBar
import com.example.smart_build.ui.theme.Black
import com.example.smart_build.viewmodel.auth.AuthStatusState
import com.example.smart_build.viewmodel.auth.AuthViewModel1
//import com.example.smart_build.viewmodel.auth.AuthViewModel
import com.example.smart_build.viewmodel.home.HomeViewModel
import com.example.smart_build.viewmodel.home.ModuleCardData

@Composable
fun HomePage(
  navController : NavHostController,
  viewModel : HomeViewModel = viewModel()
) {

  val authState by viewModel.authState.collectAsStateWithLifecycle()
  val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
  val authViewModel1: AuthViewModel1 = viewModel()

  var profileMenuOpen by remember {
    mutableStateOf(false)
  }

  LaunchedEffect(authState) {
    when (authState) {
      AuthStatusState.SignedOut -> {
        navController.navigate(Routes.LoginPage.route) {
          popUpTo(Routes.HomePage.route) {
            inclusive = true
          }
        }
      }

      else -> Unit
    }
  }

  val modules = listOf(

    ModuleCardData(
      number = "Module 0",
      title = "Introduction to Computer Systems Servicing",
      description = "This prerequisite module for Computer Systems Servicing (CSS) NC II covers the fundamentals of computer systems. It is recommended to complete this module first before proceeding to the other CSS modules and hands-on servicing activities.",
//      image = R.drawable.module_0
    ),

    ModuleCardData(
      number = "Module 1",
      title = "Installing and Configuring Computer Systems",
      description = "Learn how to properly install, configure, and prepare computer systems for operation.",
//      image = R.drawable.module_1
    ),

    ModuleCardData(
      number = "Module 2",
      title = "Setting Up Computer Networks",
      description = "Learn the fundamentals of networking and how to configure computer network connections.",
//      image = R.drawable.module_2
    ),

    ModuleCardData(
      number = "Module 3",
      title = "Maintaining Computer Systems",
      description = "Learn how to diagnose, maintain, and troubleshoot common computer system problems.",
//      image = R.drawable.module_3
    )
  )

  BoxWithConstraints(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .fillMaxSize()
      .background(Black)
  ) {
    val MAX_WIDTH = maxWidth
    val MAX_HEIGHT = maxHeight

    Column(
      modifier = Modifier
        .fillMaxSize()
    ) {
      TopBar(
        maxWidth = MAX_WIDTH,
        maxHeight = MAX_HEIGHT,
        searchBarOnClick = {},
        iconButtonOnClick = {
          profileMenuOpen = !profileMenuOpen
        }
      )
      ModuleCarousel(
        modules = modules,
        maxWidthh = MAX_WIDTH,
        maxHeightt = MAX_HEIGHT,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 40.dp)
      )
    }

    if (profileMenuOpen) {
      ProfileOverlay(
        maxWidth = MAX_WIDTH,
        maxHeight = MAX_HEIGHT,
        onDismiss = {
          profileMenuOpen = false
        }
      )
    }

    if (showDeleteDialog) {
      AlertDialog(
        onDismissRequest = {
          viewModel.changeShowDeleteDialog(false)
        },
        title = {
          Text("Delete Account?")
        },
        text = {
          Text(
            "Are you sure you want to delete your account? " +
                "This action cannot be undone."
          )
        },
        confirmButton = {

          TextButton(
            onClick = {
              viewModel.changeShowDeleteDialog(false)
//              showDeleteDialog = false
//              viewModel.deleteAccount()
              authViewModel1.deleteAccount()
            }
          ) {
            Text("Delete")
          }
        },

        dismissButton = {
          TextButton(onClick = { viewModel.changeShowDeleteDialog(false) }) {
            Text("Cancel")
          }
        }
      )
    }
  }
}