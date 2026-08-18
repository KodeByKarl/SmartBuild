package com.example.smart_build

import android.util.Log
import kotlinx.serialization.json.Json
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot

// This class is the actual Bridge (Plugin) between Compose and Godot.
// This acts as the way or bridge so the Compose and Godot can communicate.
class SmartBuildGodotPlugin(godot: Godot) : GodotPlugin(godot) {

  companion object {
    val MESSAGE_FROM_COMPOSE = SignalInfo("message_from_compose", String::class.java)
  }

  override fun getPluginName(): String {
    return "SmartBuildBridge"
  }

  override fun getPluginSignals(): Set<SignalInfo?> {
    return setOf(MESSAGE_FROM_COMPOSE)
  }

  fun sendMessageToGodot(message: String) {
    Log.d("GODOT_COMM", "Compose -> Godot: $message")

    emitSignal(MESSAGE_FROM_COMPOSE.name, message)
  }

  @UsedByGodot
  fun sendMessageToCompose(message: String) {
//    println("Godot → Compose: $message")
    Log.d("GODOT_COMM", "Godot -> Compose: $message")

    SmartBuildBridge.receiveFromGodot(message)
  }

  @UsedByGodot
  fun sendPong() {
    Log.d("GODOT_COMM", "Godot: ")

  }
}