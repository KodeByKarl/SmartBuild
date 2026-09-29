package com.example.smart_build.screens.homepage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HowToUseDialog(onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("How SmartBuild works", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
      ) {
        HowToBlock(
          "1. Sign in",
          "Your Progress is saved to your account. Switching accounts shows that student's progress only — not the previous user's.",
        )
        HowToBlock(
          "2. Home modules",
          "Swipe the module cards. You may open any module from Home. Inside a module, pages are not skippable — finish each simulation before Next unlocks.",
        )
        HowToBlock(
          "3. Guided path",
          "Open Guided Simulation. Module 1 uses the 3D Godot bench. Modules 2–4 use Compose labs with DO THIS coaches. Repeat until you are confident.",
        )
        HowToBlock(
          "4. Scenario Assessment",
          "Unlocks after Guided. You get a short scenario with faults to identify and correct (TESDA-aligned), then complete the lab without yellow coaches.",
        )
        HowToBlock(
          "5. Rule-based feedback",
          "Wrong order or incomplete steps show immediate corrective messages (e.g. wrong topology order, missing links). Fix before you can finish.",
        )
        HowToBlock(
          "6. Component Search",
          "Search / browse CSS parts (Case, PSU, Optical Drive, HDD, Motherboard, RAM, CPU, CPU Fan, and more) for independent review.",
        )
        HowToBlock(
          "7. Progress",
          "Bars rise as you clear stations. Saved to your account — they should not jump to another user's 100%.",
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Got it")
      }
    },
  )
}

@Composable
private fun HowToBlock(title: String, body: String) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    Text(body, fontSize = 13.sp)
  }
}
