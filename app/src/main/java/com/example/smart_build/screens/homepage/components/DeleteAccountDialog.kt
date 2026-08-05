package com.example.smart_build.screens.homepage.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DeleteAccountDialog() {
  AlertDialog(
    onDismissRequest = {
//      showDeleteDialog = false
    },

    title = {
      Text("Delete Account?")
    },

    text = {
      Text(
        "Are you sure you want to delete your account? " +
            "This action cannot be undone."
      )
    },

    confirmButton = {

      TextButton(
        onClick = {

//          showDeleteDialog = false

//          viewModel().deleteAccount()
        }
      ) {
        Text("Delete")
      }
    },

    dismissButton = {

      TextButton(
        onClick = {
//          showDeleteDialog = false
        }
      ) {
        Text("Cancel")
      }
    }
  )
}