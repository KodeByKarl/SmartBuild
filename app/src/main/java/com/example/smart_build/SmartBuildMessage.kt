package com.example.smart_build

import org.json.JSONObject

sealed interface SmartBuildMessage {
  /*
  data class Command(
    val action: String,
    val simulationId: String? = null
  ) : SmartBuildMessage {
    fun toJson(): String {
      return JSONObject().apply {
        put("type", "command")
        put("action", action)

        simulationId?.let { put("simulationId", it) }
      }.toString()
    }
  }

  data class Event(
    val event: String,
    val simulationId: String? = null,
    val result: String? = null,
    val score: Int? = null
  ) : SmartBuildMessage
   */

  data class Command(
    val action: String,
    val data: JSONObject? = null
  ) : SmartBuildMessage {
    fun toJson(): String {
      return JSONObject().apply {
        put("type", "command")
        put("action", action)
        data?.let { put("data", it) }
      }.toString()
    }
  }

  data class Event(
    val event: String,
    val data: JSONObject? = null
  ) : SmartBuildMessage
}