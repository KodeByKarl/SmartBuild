package com.example.smart_build.screens.authenticationpage.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White
import com.example.smart_build.viewmodel.auth.AuthFormState
import com.example.smart_build.viewmodel.auth.AuthStatusState
//import com.example.smart_build.viewmodel.auth.AuthViewModel
import com.example.smart_build.viewmodel.auth.AuthViewModel
import com.example.smart_build.viewmodel.auth.ErrorType

@Composable
fun FPForm(modifier: Modifier, viewModel: AuthViewModel, maxWidthScreen: Dp, maxHeightScreen: Dp) {
  var submitting by remember { mutableStateOf(false) }

  val fpFormState by viewModel.fpFormState.collectAsStateWithLifecycle()
  val authState by viewModel.uiState.collectAsStateWithLifecycle()
  val formState by viewModel.formState.collectAsStateWithLifecycle()
  val authError by viewModel.authError.collectAsStateWithLifecycle()

  Box(
    contentAlignment = Alignment.CenterStart,
    modifier = modifier
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      VerticalDivider(color = White)
      Spacer(modifier = Modifier.size((maxWidthScreen.value * 0.025).dp))
      Column {
        Column {
          Text(
            "Retreive your account",
            style = Typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = (maxWidthScreen.value * 0.028f).sp,
            color = Primary
          )
          Text(
            "Enter your email address so we can send you a link for reset password.",
            style = Typography.labelLarge,
            fontSize = (maxWidthScreen.value * 0.011f).sp,
            color = White.copy(alpha = 0.7f)
          )
        }
        Spacer(Modifier.size((maxHeightScreen.value * 0.035f).dp))
        Column {
          Box {
            OutlinedTextField(
              label = { Text("Email")},
              value = fpFormState.email,
              singleLine = true,
              textStyle = Typography.bodyLarge.copy(color = White, lineHeight = 0.sp),
              onValueChange = { value -> viewModel.onEmailFPChanged(value) },
              isError = authState is AuthStatusState.Error && (authError.type.equals(ErrorType.EMAIL_BLANK) || authError.type.equals(ErrorType.EMAIL_INVALID) || authError.type.equals(ErrorType.RESET_FAILED)),
              modifier = Modifier
                .fillMaxWidth(0.9f)
                .height((maxHeightScreen.value * 0.07f).dp)
            )
            androidx.compose.animation.AnimatedVisibility(
              visible = (authState is AuthStatusState.Error && (authError.type.equals(ErrorType.EMAIL_BLANK) || authError.type.equals(ErrorType.EMAIL_INVALID) || authError.type.equals(ErrorType.RESET_FAILED)) || authState is AuthStatusState.Registered),
              enter = fadeIn(tween(1000)),
              exit = fadeOut(tween(1000))
            ) {
              Text(
                authError.message,
                style = Typography.bodySmall.copy(color = Color.Red, fontSize = (maxWidthScreen.value * 0.009f).sp),
                modifier = Modifier
                  .offset(y = (maxHeightScreen.value * 0.075f).dp)
              )
            }
          }
          Spacer(Modifier.size((maxHeightScreen.value * 0.03f).dp))
          Row(horizontalArrangement = Arrangement.Start) {
            Button(
//              onClick = { submitting = !submitting },
//              onClick = { viewModel.onChangeAuthFormState(AuthFormState.ResetPassword) },
              onClick = {
                viewModel.forgotPassword(fpFormState.email)
              },
              contentPadding = PaddingValues(horizontal = (maxWidthScreen.value * 0.016f).dp, vertical = (maxHeightScreen.value * 0.02f).dp),
              enabled = authState is AuthStatusState.SignedOut && formState is AuthFormState.ForgotPassword,
              shape = RoundedCornerShape((maxWidthScreen.value * 0.013f).dp),
              modifier = Modifier.height((maxHeightScreen.value * 0.07f).dp),
              colors = ButtonColors(
                containerColor = Primary,
                contentColor = White,
                disabledContainerColor = Primary,
                disabledContentColor = White
              )
            ) {
//              if(submitting) {
              if(authState is AuthStatusState.Submitting) {
                CircularProgressIndicator( color = White, modifier = Modifier.size((maxWidthScreen.value * 0.019f).dp))
              } else {
                Text(
                  "SUBMIT",
                  fontFamily = GSFlex,
                  style = Typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  fontSize = (maxWidthScreen.value * 0.013f).sp,
                  letterSpacing = (maxWidthScreen.value * 0.00012f).sp,
                  color = White,
                  modifier = Modifier.padding(0.dp)
                )
              }
            }
            Spacer(Modifier.width((maxWidthScreen.value * 0.009f).dp))
            TextButton(
              onClick = { viewModel.onChangeAuthFormState(AuthFormState.SignIn) },
              enabled = authState != AuthStatusState.Submitting,
              contentPadding = PaddingValues(horizontal = (maxWidthScreen.value * 0.016f).dp, vertical = (maxHeightScreen.value * 0.02f).dp),
              shape = RoundedCornerShape((maxWidthScreen.value * 0.013f).dp),
              modifier = Modifier.height((maxHeightScreen.value * 0.07f).dp),
            ) {
              Text(
                "Go Back",
                fontFamily = GSFlex,
                style = Typography.titleMedium.copy(textDecoration = TextDecoration.Underline),
                fontSize = (maxWidthScreen.value * 0.013f).sp,
                fontWeight = FontWeight.Medium,
                color = White.copy(alpha = 0.8f)
              )
            }
          }
        }
      }
    }
  }
}