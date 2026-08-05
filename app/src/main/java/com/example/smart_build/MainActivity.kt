package com.example.smart_build

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.smart_build.components.ConnectionLostDialog
import com.example.smart_build.data.client.SupabaseClient
import com.example.smart_build.navigation.AppNav
import com.example.smart_build.network.NetworkConnectivityObserver
import com.example.smart_build.ui.theme.Smart_BuildTheme
import com.example.smart_build.viewmodel.auth.AuthViewModel1
import io.github.jan.supabase.auth.handleDeeplinks
//import org.godotengine.godot.GodotFragment

class MainActivity : ComponentActivity() {
  private val authViewModel: AuthViewModel1 by viewModels()
//  private var godotFragment: GodotFragment? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    Log.d("AUTH_DEEPLINK", "onCreate intent = $intent")
    Log.d("AUTH_DEEPLINK", "data = ${intent?.data}")
    handleAuthIntent(intent)


//    enableEdgeToEdge()

    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).apply {
      hide(WindowInsetsCompat.Type.systemBars())
      systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

//    val currentGodotFragment = supportFragmentManager.findFragmentById(R.id.godot_fragment_container)
//    if (currentGodotFragment is GodotFragment) {
//      godotFragment = currentGodotFragment
//    } else {
//      godotFragment = GodotFragment()
//      supportFragmentManager.beginTransaction()
//        .replace(R.id.godot_fragment_container, godotFragment!!)
//        .commitNowAllowingStateLoss()
//    }

    setContent {
      val navController = rememberNavController()
      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

      val context = LocalContext.current

      val connectivityObserver = remember {
        NetworkConnectivityObserver(context)
      }

      val isConnected by connectivityObserver.isConnected
        .collectAsStateWithLifecycle(initialValue = false)

      Smart_BuildTheme {
//        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//          AppNav(navController)
//        }
//        Box {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
          val MAX_WIDTH = maxWidth
          val MAX_HEIGHT = maxHeight

          AppNav(navController, authViewModel)
          if(!isConnected) ConnectionLostDialog(maxWidth = MAX_WIDTH, maxHeight = MAX_HEIGHT)
        }
//        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)

    setIntent(intent)

    Log.d("AUTH_DEEPLINK", "onNewIntent intent = $intent")
    Log.d("AUTH_DEEPLINK", "data = ${intent.data}")
    handleAuthIntent(intent)
  }

  private fun isPasswordRecoveryIntent(intent: Intent?): Boolean {
    val fragment = intent?.data?.fragment ?: return false

    return fragment
      .split("&")
      .any { parameter ->
        parameter == "type=recovery"
      }
  }

  /*
  private fun handleAuthIntent(intent: Intent?) {
    if (intent == null) { return }
    Log.d("AUTH_DEEPLINK", "Handling auth intent: ${intent.data}")

    if (isPasswordRecoveryIntent(intent)) {
      Log.d("AUTH_DEEPLINK", "PASSWORD RECOVERY DETECTED")

      authViewModel.onPasswordRecoveryDetected()
    }

    SupabaseClient.client.handleDeeplinks(intent)
  }

   */

  private fun handleAuthIntent(
    intent: Intent?
  ) {

    if (intent == null) {
      return
    }

    Log.d(
      "AUTH_DEEPLINK",
      "Handling auth intent: ${intent.data}"
    )

    if (isPasswordRecoveryIntent(intent)) {

      Log.d(
        "AUTH_DEEPLINK",
        "PASSWORD RECOVERY DETECTED"
      )

      authViewModel
        .onPasswordRecoveryDetected()
    }

    if (intent.data != null) {

      SupabaseClient.client
        .handleDeeplinks(intent)
    }
  }
}