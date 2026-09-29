package com.example.smart_build

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.smart_build.components.ConnectionLostDialog
import com.example.smart_build.data.client.SupabaseClient
import com.example.smart_build.godot.GodotHostLayer
import com.example.smart_build.navigation.AppNav
import com.example.smart_build.network.NetworkConnectivityObserver
import com.example.smart_build.ui.theme.Smart_BuildTheme
import com.example.smart_build.viewmodel.auth.AuthRecoveryHold
import com.example.smart_build.viewmodel.auth.AuthViewModel
import io.github.jan.supabase.auth.handleDeeplinks
import org.godotengine.godot.Godot
import org.godotengine.godot.GodotHost
import org.godotengine.godot.plugin.GodotPlugin

class MainActivity : FragmentActivity(), GodotHost {

  // Not recommended that the MainActivity access the ViewModel,
  // but just to make our life easier, let's just do this HAHA T_T.
  private val authViewModel: AuthViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    Log.d("AUTH_DEEPLINK", "onCreate intent = $intent")
    Log.d("AUTH_DEEPLINK", "data = ${intent?.data}")
    handleAuthIntent(intent)

    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).apply {
      hide(WindowInsetsCompat.Type.systemBars())
      systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    setContent {
      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

      val navController = rememberNavController()
      val context = LocalContext.current
      val connectivityObserver = remember { NetworkConnectivityObserver(context) }
      val isConnected by connectivityObserver.isConnected.collectAsStateWithLifecycle(initialValue = true)

      Smart_BuildTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
          val MAX_WIDTH = maxWidth
          val MAX_HEIGHT = maxHeight

          // Boot Godot once, behind Compose. Surface stays off-screen until a module is Ready.
          GodotHostLayer()
          AppNav(navController, authViewModel)

          if (!isConnected) ConnectionLostDialog(maxWidth = MAX_WIDTH, maxHeight = MAX_HEIGHT)
        }
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
    val uri = intent?.data ?: return false
    val haystack = buildString {
      append(uri.toString())
      uri.host?.let { append(' '); append(it) }
      uri.path?.let { append(' '); append(it) }
      uri.query?.let { append('?'); append(it) }
      uri.fragment?.let { append('#'); append(it) }
    }.lowercase()
    return haystack.contains("type=recovery") ||
      haystack.contains("type%3drecovery") ||
      uri.path?.contains("reset", ignoreCase = true) == true
  }

  private fun handleAuthIntent(intent: Intent?) {
    if (intent == null) return

    Log.d("AUTH_DEEPLINK", "Handling auth intent: ${intent.data}")

    if (intent.data != null) {
      SupabaseClient.client.handleDeeplinks(intent)
    }

    if (isPasswordRecoveryIntent(intent) || AuthRecoveryHold.isExpecting()) {
      Log.d("AUTH_DEEPLINK", "PASSWORD RECOVERY DETECTED")
      authViewModel.onPasswordRecoveryDetected()
    }
  }

  override fun getActivity(): Activity? = this

  override fun getGodot(): Godot? = Godot.getInstance(this)

  override fun getCommandLine(): List<String> =
    listOf("--main-pack", "res://SmartBuildGodot.pck")

  override fun getHostPlugins(engine: Godot): Set<GodotPlugin> {
    Log.d("GODOT_COMM", "Registering SmartBuildGodotPlugin")
    val plugin = SmartBuildGodotPlugin(engine)
    SmartBuildBridge.setPlugin(plugin)
    return setOf(plugin)
  }
}
