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
          "Open Guided Simulation. Read the hero and hardware catalog (image previews), complete each simulation phase, then read the “Be Ready for Assessment” page before the graded scenario.",
        )
        HowToBlock(
          "4. Assessment",
          "Unlocks after Guided is finished. Same labs, no yellow hints. Starts directly on the assessment simulation.",
        )
        HowToBlock(
          "5. Controls",
          "Exit (top-left) leaves without skipping ahead. Prev / Next sit beside the page counter. Search opens the CSS Parts Encyclopedia; Help explains the current page.",
        )
        HowToBlock(
          "6. Progress bars",
          "Bars rise as you clear pages. They stay saved when you leave and resume when you return — they should not jump to another account's 100%.",
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
