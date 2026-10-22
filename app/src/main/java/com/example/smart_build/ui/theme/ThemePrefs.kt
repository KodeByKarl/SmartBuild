package com.example.smart_build.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * App-wide Light / Dark choice. One setting for the whole system: Compose screens read it,
 * and it is sent to Godot with every `prepare`, so all modules follow it.
 */
object ThemePrefs {
  private const val FILE = "smartbuild_display"
  private const val KEY = "light_mode"

  var lightMode by mutableStateOf(false)
    private set

  fun init(context: Context) {
    lightMode = runCatching {
      context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY, false)
    }.getOrDefault(false)
  }

  fun set(context: Context, light: Boolean) {
    lightMode = light
    runCatching {
      context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY, light).apply()
    }
  }
}
