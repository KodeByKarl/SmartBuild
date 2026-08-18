package com.example.smart_build.godot

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.example.smart_build.SmartBuildBridge
import com.example.smart_build.ui.theme.White
import org.godotengine.godot.GodotFragment

@Composable
fun GodotTestScreen() {
  val activity = LocalActivity.current as FragmentActivity

  val containerId = remember { View.generateViewId() }

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
  }

  AndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { context -> FrameLayout(context).apply { id = containerId } }
  )
}