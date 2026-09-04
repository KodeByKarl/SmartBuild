package com.example.smart_build.screens.homepage

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
import androidx.compose.runtime.DisposableEffect
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
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.smart_build.R
import com.example.smart_build.data.ModuleProgressStore
import com.example.smart_build.navigation.Routes
import com.example.smart_build.screens.homepage.components.ModuleCarousel
import com.example.smart_build.screens.homepage.components.ProfileOverlay
import com.example.smart_build.screens.homepage.components.TopBar
import com.example.smart_build.ui.theme.Black
import com.example.smart_build.viewmodel.auth.AuthStatusState
import com.example.smart_build.viewmodel.auth.AuthViewModel
import com.example.smart_build.viewmodel.home.HomeViewModel
import com.example.smart_build.viewmodel.home.ModuleCardData

@Composable
fun HomePage(
  navController : NavHostController,
  viewModel : HomeViewModel = viewModel()
) {

  val authState by viewModel.authState.collectAsStateWithLifecycle()
  val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
  val progressMap by viewModel.moduleProgress.collectAsStateWithLifecycle()
  val guidedMap by viewModel.guidedDoneMap.collectAsStateWithLifecycle()
  val assessmentMap by viewModel.assessmentDoneMap.collectAsStateWithLifecycle()
  val authViewModel1: AuthViewModel = viewModel()
  val context = LocalContext.current

  var profileMenuOpen by remember {
    mutableStateOf(false)
  }

  LaunchedEffect(Unit) {
    viewModel.refreshProgress()
  }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        viewModel.refreshProgress()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  fun openModule(moduleId: Int, moduleName: String, simulationType: Int) {
    // Reaching the last guided page caps progress at 99%. Treat that as guided
    // done so Assessment unlocks even if guided_completed was missed (Next-only).
    if (simulationType == 1 && moduleId >= 1) {
      val guided = ModuleProgressStore.guidedDone(context, moduleId)
      val pct = ModuleProgressStore.progress(context, moduleId)
      if (!guided && pct >= 99f) {
        ModuleProgressStore.markGuidedCompleted(context, moduleId)
      } else if (!guided) {
        Toast.makeText(
          context,
          "Finish Guided Simulation first (reach the last page). Assessment uses the same pages without guides.",
          Toast.LENGTH_SHORT
        ).show()
        return
      }
    }
    val progress = ModuleProgressStore.progress(context, moduleId)
    navController.navigate(
      Routes.ModulePage.createRoute(moduleId, moduleName, simulationType, progress)
    ) {
      launchSingleTop = true
    }
  }

  fun pct(id: Int) = progressMap[id] ?: ModuleProgressStore.progress(context, id)
  fun guided(id: Int) = guidedMap[id] ?: ModuleProgressStore.guidedDone(context, id)
  fun assessed(id: Int) = assessmentMap[id] ?: ModuleProgressStore.assessmentDone(context, id)

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
      moduleId = 0,
      title = "Introduction to Computer Systems Servicing",
      description = "This prerequisite module for Computer Systems Servicing (CSS) NC II covers the fundamentals of computer systems. It is recommended to complete this module first before proceeding to the other CSS modules and hands-on servicing activities.",
      image = R.drawable.module_0_card,
      contents = """
        Hello there!
        Welcome to the very first module of the core modules of this course, the Introduction to Computer Systems Servicing.
      """.trimIndent(),
      benefits = listOf(
        "At the end of this introductory, you will be able to:",
        "Explain what Computer Systems Servicing covers",
        "Describe technician roles and workplace pathways",
        "Apply basic OHS and quality habits",
        "Identify core computer components and connectors",
        "Recognize essential tools used in CSS work"
      ),
      progressPercent = pct(0),
      guidedDone = guided(0),
      assessmentDone = assessed(0),
      onGS = {
        openModule(0, "Introduction To Computer Systems Servicing", 0)
      },
      onAS = { /* Module 0 is lesson-only per flowchart */ }
    ),

    ModuleCardData(
      number = "Module 1",
      moduleId = 1,
      title = "Installing and Configuring Computer Systems",
      description = "Learn how to properly install, configure, and prepare computer systems for operation.",
      image = R.drawable.module_1_card,
      contents = """
        Hello there!
        Welcome to the first core module of this course, the Installing and Configuring Computer Systems.
      """.trimIndent(),
      benefits = listOf(
        "At the end of this Module 1, you will be able to:",
        "Prepare an ESD-safe bench and inspect parts",
        "Assemble a PC in a technician-safe order",
        "Configure BIOS boot settings for installation",
        "Install an OS and create the first user",
        "Install drivers and validate a deployment-ready PC"
      ),
      progressPercent = pct(1),
      guidedDone = guided(1),
      assessmentDone = assessed(1),
      onGS = {
        openModule(1, "Installing and Configuring Computer Systems", 0)
      },
      onAS = {
        openModule(1, "Installing and Configuring Computer Systems", 1)
      }
    ),

    ModuleCardData(
      number = "Module 2",
      moduleId = 2,
      title = "Setting Up Computer Networks",
      description = "Learn the fundamentals of networking and how to configure computer network connections.",
      image = R.drawable.module_2_card,
      contents = """
        Hello there!
        Welcome to the second core module of this course, the Setting Up Computer Networks.
      """.trimIndent(),
      benefits = listOf(
        "At the end of this Module 2, you will be able to:",
        "Identify modem, router, switch, AP, NIC, and cabling parts",
        "Create and test straight-through (T568B) and crossover (T568A/B) cables",
        "Connect a small-office wired topology with link lights",
        "Configure IP, subnet mask, gateway, and DNS",
        "Secure Wi-Fi and verify connectivity including file sharing"
      ),
      progressPercent = pct(2),
      guidedDone = guided(2),
      assessmentDone = assessed(2),
      onGS = {
        openModule(2, "Setting Up Computer Networks", 0)
      },
      onAS = {
        openModule(2, "Setting Up Computer Networks", 1)
      }
    ),

    ModuleCardData(
      number = "Module 3",
      moduleId = 3,
      title = "Setting Up Computer Servers",
      description = "Learn how to diagnose, maintain, and troubleshoot common computer system problems.",
      image = R.drawable.module_3_card,
      contents = """
        Hello there!
        Welcome to the third core module of this course, the Setting Up Computer Servers.
      """.trimIndent(),
      benefits = listOf(
        "At the end of this Module 3, you will be able to:",
        "Identify file-server, storage, and client roles",
        "Create department and user folder structures",
        "Create users and security groups",
        "Apply least-privilege share and NTFS permissions",
        "Validate authorized access and blocked cross-access"
      ),
      progressPercent = pct(3),
      guidedDone = guided(3),
      assessmentDone = assessed(3),
      onGS = {
        openModule(3, "Setting Up Computer Servers", 0)
      },
      onAS = {
        openModule(3, "Setting Up Computer Servers", 1)
      }
    ),

    ModuleCardData(
      number = "Module 4",
      moduleId = 4,
      title = "Maintaining Computer Systems",
      description = "Learn how to diagnose, maintain, and troubleshoot common computer system problems.",
      image = R.drawable.module_4_card,
      contents = """
        Hello there!
        Welcome to the last core module of this course, the Maintaining Computer Systems.
      """.trimIndent(),
      benefits = listOf(
        "At the end of this Module 4, you will be able to:",
        "Intake tickets and stage ESD-safe tools",
        "Perform preventive hardware maintenance",
        "Apply software updates, scans, and cleanup",
        "Diagnose and restore network connectivity",
        "Validate repairs and document service work"
      ),
      progressPercent = pct(4),
      guidedDone = guided(4),
      assessmentDone = assessed(4),
      onGS = {
        openModule(4, "Maintaining Computer Systems", 0)
      },
      onAS = {
        openModule(4, "Maintaining Computer Systems", 1)
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
        searchBarOnClick = {
          navController.navigate(Routes.ComponentSearch.route) {
            launchSingleTop = true
          }
        },
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