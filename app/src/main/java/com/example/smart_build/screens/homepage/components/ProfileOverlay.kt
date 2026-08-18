package com.example.smart_build.screens.homepage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smart_build.ui.theme.Black
import com.example.smart_build.viewmodel.auth.AuthViewModel
import com.example.smart_build.viewmodel.home.HomeViewModel

@Composable
fun ProfileOverlay(
  maxWidth: Dp,
  maxHeight: Dp,
  onDismiss: () -> Unit
) {
  val viewModel: HomeViewModel = viewModel()
  val authViewModel1: AuthViewModel = viewModel()

  Box(
    contentAlignment = Alignment.TopEnd,
    modifier = Modifier
//      .fillMaxSize()
      .width(maxWidth)
      .height(maxHeight)
  ) {

    // ======================================================
    // DIMMED BACKGROUND
    // ======================================================

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Black.copy(alpha = 0.65f)
        )
        .clickable {
          onDismiss()
        }
    )


    // ======================================================
    // EMAIL + MENU
    // ======================================================

    Column(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(
          top = (maxHeight.value * 0.018f).dp,
          end = (maxWidth.value * 0.019f).dp
        ),
      horizontalAlignment = Alignment.End
    ) {

      // ==================================================
      // EMAIL CONTAINER
      // ==================================================

      Row(
        modifier = Modifier
          .background(
            color = Color(0xFFD7ECFF),
            shape = RoundedCornerShape(100)
          )
          .clickable {
            onDismiss()
          }
          .padding(
            horizontal = (maxWidth.value * 0.008f).dp,
            vertical = (maxHeight.value * 0.01f).dp
          ),
        verticalAlignment = Alignment.CenterVertically
      ) {

        Icon(
          imageVector = Icons.Default.AccountCircle,
          contentDescription = null,
          tint = Black.copy(alpha = 0.8f),
          modifier = Modifier.size((maxWidth.value * 0.025f).dp)
        )

        Spacer(
          modifier = Modifier.width((maxWidth.value * 0.006f).dp)
        )

        Text(
//          text = "myhoneybunchsweetiepie@example.com",
          text = "${viewModel.emailAdd}",
          color = Black.copy(alpha = 0.85f),
          fontSize = (maxWidth.value * 0.011f).sp,
          fontWeight = FontWeight.Medium
        )
      }


      Spacer(
        modifier = Modifier.height((maxHeight.value * 0.01f).dp)
      )


      // ==================================================
      // MENU
      // ==================================================

      Column(
        modifier = Modifier
          .width(
            (maxWidth.value * 0.17f).dp
          )
          .background(
            color = Color(0xFFD7ECFF),
            shape = RoundedCornerShape((maxWidth.value * 0.003).dp)
          )
          .padding(
            vertical = (maxHeight.value * 0.001f).dp
          )
      ) {

        ProfileMenuItem(
          icon = Icons.Default.Edit,
          text = "Change Password",
          maxWidth = maxWidth,
          maxHeight = maxHeight,
          onClick = {
            // TODO: Change password
          }
        )

        ProfileMenuItem(
          icon = Icons.Default.Delete,
          text = "Delete Account",
          maxWidth = maxWidth,
          maxHeight = maxHeight,
          onClick = {
            // TODO: Delete account
            viewModel.changeShowDeleteDialog(true)
//            authViewModel1.deleteAccount()
          }
        )

        ProfileMenuItem(
          icon = Icons.AutoMirrored.Filled.Logout,
          text = "Sign Out",
          maxWidth = maxWidth,
          maxHeight = maxHeight,
          onClick = {
            // Relocate the function to AuthViewModel.
            viewModel.signOut()
          }
        )
      }
    }
  }
}