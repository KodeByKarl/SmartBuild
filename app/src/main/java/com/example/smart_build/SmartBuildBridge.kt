package com.example.smart_build

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

// ========================================================================= //
//  This object is also a Bridge, but not as the SmartBuildGodotPlugin.      //
//  This serves to be the receiver of the actual data from Godot,            //
//  and we can process it here instead of doing it in SmartBuildGodotPlugin. //
//  And vice versa, receiver of data from Compose that can be processed also //
//  before sending to Godot.                                                 //
//  The processed data can be now sent to either Compose or Godot.           //
// ========================================================================= //
object SmartBuildBridge {
  // replay=0: a replayed assessment_completed / destroy from a previous module
  // was marking the next Guided run complete the moment ModulePage subscribed.
  // engine_initialized is tracked with the boolean below, not via replay.
  private val _godotMessages = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1000)
  val godotMessages = _godotMessages.asSharedFlow()

  private var currentPlugin: SmartBuildGodotPlugin? = null
  @Volatile
  var engineInitialized: Boolean = false
    private set

  // SurfaceView punches through Compose. Keep it GONE until a module is Ready
  // so Login / Home stay tappable while the engine warms up in the background.
  private val _godotSurfaceVisible = MutableStateFlow(false)
  val godotSurfaceVisible = _godotSurfaceVisible.asStateFlow()

  fun setGodotSurfaceVisible(visible: Boolean) {
    _godotSurfaceVisible.value = visible
  }

  fun isPluginReady(): Boolean = currentPlugin != null

  /** Call when the Godot fragment is recreated so Compose waits for a fresh engine_initialized. */
  fun resetEngineSession() {
    engineInitialized = false
    Log.d("GODOT_COMM", "Engine session reset")
  }

  fun setPlugin(plugin: SmartBuildGodotPlugin) {
    currentPlugin = plugin
    Log.d("GODOT_COMM", "SmartBuildGodotPlugin registered")
  }

  fun sendToGodot(message: String) {
    val plugin = currentPlugin

    if (plugin == null) {
      Log.e("GODOT_COMM", "Godot plugin is not registered!")
      return
    }

    plugin.sendMessageToGodot(message)
  }

  fun receiveFromGodot(message: String) {
    try {
      val json = JSONObject(message)

      val type = json.optString("type")
      val event = json.optString("event")
//      val simulationId = json.optString("simulationId")

      Log.d("GODOT_COMM", "{ type: $type, event: $event }")

      if (event == "engine_initialized") {
        engineInitialized = true
      }

//      _godotMessages.tryEmit(event)
      _godotMessages.tryEmit(json.toString())
    } catch(e: Exception) {
      Log.e("GODOT_COMM", "Failed to parse Godot message: $message")
    }
  }

//  fun startSimulation(simulationId: String) {
//    val message = SmartBuildMessage.Command("start_simulation", simulationId)
//
//    sendToGodot(message.toJson())
//  }

  /**
   * Launch a simulation module inside Godot.
   * Optional session fields let Godot sync progress without owning auth UI.
   * See SmartBuild-Godot/assets/docs/native_auth_handoff.md
   */
  fun prepare(
    moduleId: Int,
    simulationType: Int,
    progress: Float,
    accessToken: String? = null,
    refreshToken: String? = null,
    userId: String? = null,
    userEmail: String? = null,
  ) {
    val data = JSONObject().apply {
      put("moduleId", moduleId)
      put("simulationType", simulationType)
      put("progress", progress.toDouble())
      accessToken?.takeIf { it.isNotBlank() }?.let { put("accessToken", it) }
      refreshToken?.takeIf { it.isNotBlank() }?.let { put("refreshToken", it) }
      userId?.takeIf { it.isNotBlank() }?.let { put("userId", it) }
      userEmail?.takeIf { it.isNotBlank() }?.let { put("userEmail", it) }
    }
    val message = SmartBuildMessage.Command("prepare", data)

    Log.d("GODOT_COMM", "Sending prepare: ${message.toJson()}")

    sendToGodot(message.toJson())
  }
}