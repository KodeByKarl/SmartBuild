package com.example.smart_build

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.smart_build.components.ConnectionLostDialog
import com.example.smart_build.data.client.SupabaseClient
import com.example.smart_build.navigation.AppNav
import com.example.smart_build.network.NetworkConnectivityObserver
import com.example.smart_build.ui.theme.Smart_BuildTheme
import com.example.smart_build.viewmodel.auth.AuthViewModel
import io.github.jan.supabase.auth.handleDeeplinks
import org.godotengine.godot.Godot
import org.godotengine.godot.GodotFragment
import org.godotengine.godot.GodotHost
import org.godotengine.godot.plugin.GodotPlugin

//class MainActivity : ComponentActivity() {
//class MainActivity : FragmentActivity() {
class MainActivity : FragmentActivity(), GodotHost {

  // Not recommended that the MainActivity access the ViewModel,
  // but just to make our life easier, let's just do this HAHA T_T.
  private val authViewModel: AuthViewModel by viewModels()

  private var godotInstance: Godot? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Deeplink.
    Log.d("AUTH_DEEPLINK", "onCreate intent = $intent")
    Log.d("AUTH_DEEPLINK", "data = ${intent?.data}")
    handleAuthIntent(intent)

    // Hiding the Status bar.
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).apply {
      hide(WindowInsetsCompat.Type.systemBars())
      systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    // Setting the content.
    setContent {
      // Forcing the orientation to be in Landscape.
//      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

      // Getting NavHostController.
      val navController = rememberNavController()

      // For checking if there is an internet connection.
      val context = LocalContext.current
      val connectivityObserver = remember { NetworkConnectivityObserver(context) }
      val isConnected by connectivityObserver.isConnected.collectAsStateWithLifecycle(initialValue = true)

      Smart_BuildTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
          val MAX_WIDTH = maxWidth // Width size of the Device's Screen.
          val MAX_HEIGHT = maxHeight // Height size of the Device's Screen.

          // Boot Godot once, behind Compose. Surface stays GONE until a module is Ready.
          com.example.smart_build.godot.GodotHostLayer()
          AppNav(navController, authViewModel)

//           Will appear if there is no internet connection.
          if(!isConnected) ConnectionLostDialog(maxWidth = MAX_WIDTH, maxHeight = MAX_HEIGHT)

//          GodotTestScreen()
        }
      }
    }
  }

  @Composable
  fun GodotTestScreen1() {
    val activity = LocalActivity.current as FragmentActivity

    val containerId = remember { View.generateViewId() }

    var godotMessage by remember {
      mutableStateOf("")
    }

//    val res: JSONObject? = if(godotMessage == "") null else JSONObject(godotMessage)

    Column(modifier = Modifier.fillMaxSize()) {
      Text("Godot says: $godotMessage")

      Button(
        onClick = {
//          SmartBuildBridge.sendToGodot("Hello Godot!")
//          SmartBuildBridge.sendToGodot(
//            """
//              {
//                "action": "ping"
//              }
//            """.trimIndent()
//          )
//          SmartBuildBridge.startSimulation("module_0_unit_1_lesson_1")
        }
      ) {
        Text("Send to Godot")
      }

      AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context -> FrameLayout(context).apply { id = containerId } }
      )
    }

    LaunchedEffect(Unit) {
      val fragmentManager = activity.supportFragmentManager

      if (fragmentManager.findFragmentByTag("GODOT") == null) {
        val godotFragment = GodotFragment()

        fragmentManager.beginTransaction().replace(
          containerId,
          godotFragment,
          "GODOT"
        ).commit()
      }

      SmartBuildBridge.godotMessages.collect { message ->
        Log.d("GODOT_COMM", "Compose received: $message")

        godotMessage = message
      }
    }
  }

  // ===================================================== //
  //  Functions to handle the Deeplink for Reset Password  //
  // ===================================================== //
  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)

    setIntent(intent)

    Log.d("AUTH_DEEPLINK", "onNewIntent intent = $intent")
    Log.d("AUTH_DEEPLINK", "data = ${intent.data}")
    handleAuthIntent(intent)
  }

  private fun isPasswordRecoveryIntent(intent: Intent?): Boolean {
    val fragment = intent?.data?.fragment ?: return false

    return fragment.split("&").any { parameter -> parameter == "type=recovery" }
  }

  private fun handleAuthIntent(intent: Intent?) {
    if (intent == null) { return }

    Log.d("AUTH_DEEPLINK", "Handling auth intent: ${intent.data}")

    if (isPasswordRecoveryIntent(intent)) {
      Log.d("AUTH_DEEPLINK", "PASSWORD RECOVERY DETECTED")

      authViewModel.onPasswordRecoveryDetected()
    }

    if (intent.data != null) {
      SupabaseClient.client.handleDeeplinks(intent)
    }
  }


  // ======================== //
  //  Functions to for Godot  //
  // ======================== //
  override fun getActivity(): Activity? {
    return this
  }

  override fun getGodot(): Godot? {
    return Godot.getInstance(this)
  }

  override fun getCommandLine(): List<String> {
    return listOf("--main-pack", "res://SmartBuildGodot.pck")
  }

  override fun getHostPlugins(engine: Godot): Set<GodotPlugin> {
    Log.d("GODOT_COMM", "Registering SmartBuildGodotPlugin")

    val plugin = SmartBuildGodotPlugin(engine)
    SmartBuildBridge.setPlugin(plugin)
    return setOf(plugin)
  }
}