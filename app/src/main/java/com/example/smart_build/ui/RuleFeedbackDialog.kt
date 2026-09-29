package com.example.smart_build.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.GSCode
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Primary

/**
 * Panelist: error/success dialog on incorrect and correct rule-based actions.
 */
@Composable
fun RuleFeedbackDialog(
  correct: Boolean,
  title: String,
  body: String,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (correct) "Correct" else "Incorrect",
        fontFamily = GSCode,
        fontWeight = FontWeight.Bold,
        color = if (correct) Color(0xFF1E8449) else Color(0xFFC0392B),
        fontSize = 20.sp,
      )
    },
    text = {
      Text(
        text = "$title\n\n$body",
        fontFamily = GSFlex,
        fontSize = 16.sp,
        lineHeight = 22.sp,
      )
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(if (correct) "Continue" else "Got it", color = Primary, fontFamily = GSFlex)
      }
    },
  )
}
