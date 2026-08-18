package com.example.smart_build

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
  private val _godotMessages = MutableSharedFlow<String>(extraBufferCapacity = 1000)
  val godotMessages = _godotMessages.asSharedFlow()

  private var currentPlugin: SmartBuildGodotPlugin? = null

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

  fun prepare(moduleId: Int, simulationType: Int, progress: Float) {
    val data = JSONObject().apply {
      put("moduleId", moduleId)
      put("simulationType", simulationType)
      put("progress", progress)
    }
    val message = SmartBuildMessage.Command("prepare", data)

    Log.d("GODOT_COMM", "Sending prepare: ${message.toJson()}")

    sendToGodot(message.toJson())
  }
}