package com.example.smart_build.screens.homepage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.viewmodel.home.ChangePasswordState

@Composable
fun ChangePasswordDialog(
  state: ChangePasswordState,
  onSubmit: (current: String, new: String, confirm: String) -> Unit,
  onDismiss: () -> Unit
) {
  if (state == ChangePasswordState.Done) {
    AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Password changed", fontWeight = FontWeight.Bold) },
      text = { Text("Use your new password the next time you sign in.") },
      confirmButton = {
        TextButton(onClick = onDismiss) { Text("OK") }
      }
    )
    return
  }

  var current by rememberSaveable { mutableStateOf("") }
  var new by rememberSaveable { mutableStateOf("") }
  var confirm by rememberSaveable { mutableStateOf("") }
  val submitting = state == ChangePasswordState.Submitting
  val error = (state as? ChangePasswordState.Editing)?.error

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Change Password", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        PasswordField("Current password", current, !submitting) { current = it }
        PasswordField("New password", new, !submitting) { new = it }
        PasswordField("Confirm new password", confirm, !submitting) { confirm = it }
        Text(
          "At least 8 characters with letters, numbers, and special characters.",
          fontSize = 12.sp
        )
        if (error != null) {
          Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        if (submitting) {
          CircularProgressIndicator()
        }
      }
    },
    confirmButton = {
      TextButton(
        enabled = !submitting,
        onClick = { onSubmit(current, new, confirm) }
      ) {
        Text("Change")
      }
    },
    dismissButton = {
      TextButton(enabled = !submitting, onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun PasswordField(
  label: String,
  value: String,
  enabled: Boolean,
  onValueChange: (String) -> Unit
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label) },
    enabled = enabled,
    singleLine = true,
    visualTransformation = PasswordVisualTransformation(),
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    modifier = Modifier.fillMaxWidth()
  )
}
