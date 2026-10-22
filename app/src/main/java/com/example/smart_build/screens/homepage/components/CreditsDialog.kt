package com.example.smart_build.screens.homepage.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class ModelCredit(
  val title: String,
  val author: String,
  val license: String,
  val url: String
)

private const val CC_BY_4 = "CC BY 4.0"
private const val SKETCHFAB_STANDARD = "Sketchfab Standard"
private const val CC_BY_4_URL = "https://creativecommons.org/licenses/by/4.0/"

// Only the glTF models actually loaded by the simulations (Module 1 bench:
// motherboard + CPU cooler). Add an entry here when a new Sketchfab model ships.
private val MODEL_CREDITS = listOf(
  ModelCredit("CPU Cooler", "Fochdog", CC_BY_4, "https://sketchfab.com/3d-models/cpu-cooler-672a0a74a98c452a862016bee99f3579"),
  ModelCredit("Motherboard", "tom.grzembke", SKETCHFAB_STANDARD, "https://sketchfab.com/3d-models/motherboard-09eada6916bd438489a73a7e1326e8e1"),
)

@Composable
fun CreditsDialog(onDismiss: () -> Unit) {
  val uriHandler = LocalUriHandler.current

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Credits", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          "3D models from Sketchfab. Models may have been scaled or adjusted for use in this app. Tap a model to open its source page.",
          fontSize = 13.sp
        )
        MODEL_CREDITS.forEach { credit ->
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { uriHandler.openUri(credit.url) }
          ) {
            Text("\"${credit.title}\" by ${credit.author}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
              "License: ${credit.license}",
              fontSize = 12.sp
            )
            Text(
              credit.url,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.primary,
              textDecoration = TextDecoration.Underline
            )
          }
        }
        Text(
          "CC BY 4.0 license: $CC_BY_4_URL",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clickable { uriHandler.openUri(CC_BY_4_URL) }
        )
        Text(
          "Built with Godot Engine (MIT license) and Supabase.",
          fontSize = 12.sp
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Close") }
    }
  )
}
