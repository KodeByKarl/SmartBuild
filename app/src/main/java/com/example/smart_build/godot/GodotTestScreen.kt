package com.example.smart_build.godot

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smart_build.SmartBuildBridge
import org.godotengine.godot.GodotFragment

/**
 * Persistent Godot surface. Creating a new GodotFragment reboots the whole
 * engine (minutes on low-end phones). Attach once and never replace it.
 */
@Composable
fun GodotHostLayer() {
  val activity = LocalActivity.current as FragmentActivity
  val containerId = remember { View.generateViewId() }
  val surfaceVisible by SmartBuildBridge.godotSurfaceVisible.collectAsStateWithLifecycle()

  AndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { context ->
      FrameLayout(context).apply {
        id = containerId
        // Keep VISIBLE so the engine can boot. Park off-screen until a module is Ready
        // (View.GONE zeros the surface and Godot never sends engine_initialized).
        translationX = 10_000f
        post { GodotRuntime.ensureAttached(activity, containerId) }
      }
    },
    update = { view ->
      view.translationX = if (surfaceVisible) 0f else 10_000f
    }
  )
}

/** @deprecated Use [GodotHostLayer] at the Activity level. Kept for call-site compatibility. */
@Composable
fun GodotTestScreen() {
  GodotHostLayer()
}

object GodotRuntime {
  const val GODOT_TAG = "GODOT"

  @Synchronized
  fun ensureAttached(activity: FragmentActivity, containerId: Int) {
    val fm = activity.supportFragmentManager
    val existing = fm.findFragmentByTag(GODOT_TAG) as GodotFragment?

    if (existing != null && existing.isAdded) {
      if (existing.id == containerId) {
        return
      }
      // Engine is already running in another container — do NOT destroy it.
      Log.d("GODOT_COMM", "GodotFragment already attached; skipping recreate")
      return
    }

    val tx = fm.beginTransaction()
    if (existing != null) {
      tx.add(containerId, existing, GODOT_TAG)
    } else {
      tx.add(containerId, GodotFragment(), GODOT_TAG)
    }
    tx.commitNowAllowingStateLoss()
    Log.d("GODOT_COMM", "GodotFragment attached to container $containerId")
  }
}
