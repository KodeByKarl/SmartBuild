package com.example.smart_build.screens.homepage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
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
import com.example.smart_build.viewmodel.auth.AuthViewModel
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
  val authViewModel1: AuthViewModel = viewModel()

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
      contents = """
        Hello there!
        Welcome to the very first module of the core modules of this course, the Introduction to Computer Systems Servicing.
      """.trimIndent(),
      benefits = listOf(
        "At the end of this introductory, you will be able to:",
        "Benefit 1",
        "Benefit 2",
        "Benefit 3",
        "Benefit 4",
        "Benefit 5"
      ),
//      image = R.drawable.module_0,
      onGS = {
        navController.navigate(Routes.ModulePage.createRoute(
          moduleId = 0,
          moduleName = "Introduction To Computer Systems Servicing",
          simulationType = 0,
          progress = 0f
        )) {
          popUpTo(Routes.HomePage.route) {
            inclusive = true
          }
        }
      },
      onAS = { /* Navigate */ }
    ),

    ModuleCardData(
      number = "Module 1",
      title = "Installing and Configuring Computer Systems",
      description = "Learn how to properly install, configure, and prepare computer systems for operation.",
      contents = """
        Hello there!
        Welcome to the first core module of this course, the Installing and Configuring Computer Systems.
      """.trimIndent(),
      benefits = listOf(
        "At the end of this Module 1, you will be able to:",
        "Benefit 1",
        "Benefit 2",
        "Benefit 3",
        "Benefit 4",
        "Benefit 5"
      ),
//      image = R.drawable.module_1,
      onGS = {
        navController.navigate(Routes.ModulePage.createRoute(
          moduleId = 1,
          moduleName = "Installing and Configuring Computer Systems",
          simulationType = 0,
          progress = 0f
        )) {
          popUpTo(Routes.HomePage.route) {
            inclusive = true
          }
        }
      },
      onAS = {
        navController.navigate(Routes.ModulePage.createRoute(
          moduleId = 1,
          moduleName = "Installing and Configuring Computer Systems",
          simulationType = 1,
          progress = 0f
        )) {
          popUpTo(Routes.HomePage.route) {
            inclusive = true
          }
        }
      }
    ),

    ModuleCardData(
      number = "Module 2",
      title = "Setting Up Computer Networks",
      description = "Learn the fundamentals of networking and how to configure computer network connections.",
      contents = """
        Hello there!
        Welcome to the second core module of this course, the Setting Up Computer Networks.
      """.trimIndent(),
      benefits = listOf(
        "At the end of this Module 2, you will be able to:",
        "Benefit 1",
        "Benefit 2",
        "Benefit 3",
        "Benefit 4",
        "Benefit 5"
      ),
//      image = R.drawable.module_2,
      onGS = {
        navController.navigate(Routes.ModulePage.createRoute(
          moduleId = 2,
          moduleName = "Setting Up Computer Networks",
          simulationType = 0,
          progress = 0f
        )) {
          popUpTo(Routes.HomePage.route) {
            inclusive = true
          }
        }
      },
      onAS = {
        navController.navigate(Routes.ModulePage.createRoute(
          moduleId = 2,
          moduleName = "Setting Up Computer Networks",
          simulationType = 1,
          progress = 0f
        )) {
          popUpTo(Routes.HomePage.route) {
            inclusive = true
          }
        }
      }
    ),

    ModuleCardData(
      number = "Module 3",
      title = "Setting Up Computer Servers",
      description = "Learn how to diagnose, maintain, and troubleshoot common computer system problems.",
      contents = """
        Hello there!
        Welcome to the third core module of this course, the Setting Up Computer Servers.
      """.trimIndent(),
      benefits = listOf(
        "At the end of this Module 3, you will be able to:",
        "Benefit 1",
        "Benefit 2",
        "Benefit 3",
        "Benefit 4",
        "Benefit 5"
      ),
//      image = R.drawable.module_3,
      onGS = {
        navController.navigate(Routes.ModulePage.createRoute(
          moduleId = 3,
          moduleName = "Setting Up Computer Servers",
          simulationType = 0,
          progress = 0f
        )) {
          popUpTo(Routes.HomePage.route) {
            inclusive = true
          }
        }
      },
      onAS = {
        navController.navigate(Routes.ModulePage.createRoute(
          moduleId = 3,
          moduleName = "Setting Up Computer Servers",
          simulationType = 1,
          progress = 0f
        )) {
          popUpTo(Routes.HomePage.route) {
            inclusive = true
          }
        }
      }
    ),

    ModuleCardData(
      number = "Module 4",
      title = "Maintaining Computer Systems",
      description = "Learn how to diagnose, maintain, and troubleshoot common computer system problems.",
      contents = """
        Hello there!
        Welcome to the last core module of this course, the Maintaining Computer Systems.
      """.trimIndent(),
      benefits = listOf(
        "At the end of this Module 4, you will be able to:",
        "Benefit 1",
        "Benefit 2",
        "Benefit 3",
        "Benefit 4",
        "Benefit 5"
      ),
//      image = R.drawable.module_4,
      onGS = {
        navController.navigate(Routes.ModulePage.createRoute(
          moduleId = 4,
          moduleName = "Maintaining Computer Systems",
          simulationType = 0,
          progress = 0f
        )) {
          popUpTo(Routes.HomePage.route) {
            inclusive = true
          }
        }
      },
      onAS = {
        navController.navigate(Routes.ModulePage.createRoute(
          moduleId = 4,
          moduleName = "Maintaining Computer Systems",
          simulationType = 1,
          progress = 0f
        )) {
          popUpTo(Routes.HomePage.route) {
            inclusive = true
          }
        }
      }
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