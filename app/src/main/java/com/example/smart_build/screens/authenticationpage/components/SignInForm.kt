package com.example.smart_build.screens.authenticationpage.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smart_build.components.AuthFieldStyles
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White
import com.example.smart_build.viewmodel.auth.AuthFormState
import com.example.smart_build.viewmodel.auth.AuthMode
import com.example.smart_build.viewmodel.auth.AuthStatusState
//import com.example.smart_build.viewmodel.auth.AuthViewModel
import com.example.smart_build.viewmodel.auth.AuthViewModel
import com.example.smart_build.viewmodel.auth.ErrorType

@Composable
fun SignInForm(authMode: AuthMode, modifier: Modifier, viewModel: AuthViewModel, maxWidthScreen: Dp, maxHeightScreen: Dp) {
  var submitting by remember { mutableStateOf(false) }
  var isPWVisible by remember { mutableStateOf(false) }

  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val formState by viewModel.formState.collectAsStateWithLifecycle()
  val signInFormState by viewModel.signInFormState.collectAsStateWithLifecycle()
  val authError by viewModel.authError.collectAsStateWithLifecycle()

  Box(
    contentAlignment = Alignment.CenterStart,
    modifier = modifier
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Column {
          AnimatedContent(
            targetState = authMode
          ) { state ->
            when(state) {
              AuthMode.SignIn -> Column {
                Text(
                  "Get Started!",
                  style = Typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  fontSize = (maxWidthScreen.value * 0.028f).sp,
                  color = Primary
                )
                Text(
                  "Authenticate to sync your progress to the server.",
                  style = Typography.labelLarge,
                  fontSize = (maxWidthScreen.value * 0.011f).sp,
                  color = White.copy(alpha = 0.7f)
                )
              }
              AuthMode.SignUp -> Column {
                Text(
                "Create Your Account!",
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = (maxWidthScreen.value * 0.028f).sp,
                color = Primary
                )
                Text(
                  "Create an account to save and sync your progress across your devices.",
                  style = Typography.labelLarge,
                  fontSize = (maxWidthScreen.value * 0.011f).sp,
                  color = White.copy(alpha = 0.7f)
                )
              }
            }
          }
        }
        Spacer(Modifier.size((maxHeightScreen.value * 0.035f).dp))
        Column {
          Column {
            Box {
              OutlinedTextField(
                label = { Text("Email", color = White.copy(alpha = 0.7f)) },
                value = signInFormState.email,
                singleLine = true,
                textStyle = AuthFieldStyles.textStyle,
                colors = AuthFieldStyles.colors(),
                isError = uiState is AuthStatusState.Error && (authError.type.equals(ErrorType.EMAIL_BLANK) || authError.type.equals(ErrorType.EMAIL_INVALID) || authError.type.equals(
                  ErrorType.INVALID_CREDENTIALS) || authError.type.equals(ErrorType.ACCOUNT_EXISTING) || authError.type.equals(ErrorType.ACCOUNT_NOT_VERIFIED)),
                onValueChange = { value ->
                  viewModel.clearError()
                  viewModel.onEmailSIChanged(value)
                },
                modifier = Modifier.fillMaxWidth(0.9f)
              )
              androidx.compose.animation.AnimatedVisibility(
                visible = (uiState is AuthStatusState.Error && (authError.type.equals(ErrorType.EMAIL_BLANK) || authError.type.equals(ErrorType.EMAIL_INVALID) || authError.type.equals(ErrorType.INVALID_CREDENTIALS) || authError.type.equals(
                  ErrorType.ACCOUNT_EXISTING) || authError.type.equals(ErrorType.ACCOUNT_NOT_VERIFIED))) || (uiState is AuthStatusState.Registered),
                enter = fadeIn(tween(1000)),
                exit = fadeOut(tween(1000))
              ) {
                Text(
                  authError.message,
                  style = Typography.bodySmall.copy(color = (if(uiState is AuthStatusState.Error) Color.Red else Primary), fontSize = (maxWidthScreen.value * 0.009f).sp),
                  modifier = Modifier
                    .offset(y = (maxHeightScreen.value * 0.075f).dp)
                )
              }
            }
            Spacer(Modifier.size((maxHeightScreen.value * 0.025f).dp))
            Box {
              OutlinedTextField(
                label = { Text("Password", color = White.copy(alpha = 0.7f)) },
                value = signInFormState.password,
                singleLine = true,
                textStyle = AuthFieldStyles.textStyle,
                colors = AuthFieldStyles.colors(),
                isError = uiState is AuthStatusState.Error && (authError.type.equals(ErrorType.PW_BLANK) || authError.type.equals(ErrorType.PW_SHORT)),
                modifier = Modifier.fillMaxWidth(0.9f),
                onValueChange = { value ->
                  viewModel.clearError()
                  viewModel.onPWSIChanged(value)
                },
                visualTransformation = if(!isPWVisible) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = {
                  val icon = if(isPWVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                  val desc = if(isPWVisible) "Hide Password" else "Show Password"

                  IconButton(onClick = { isPWVisible = !isPWVisible }) {
                    Icon(
                      imageVector = icon,
                      contentDescription = desc,
                      tint = White.copy(alpha = 0.7f)
                    )
                  }
                },
              )
              androidx.compose.animation.AnimatedVisibility(
                visible = uiState is AuthStatusState.Error && (authError.type.equals(ErrorType.PW_BLANK) || authError.type.equals(ErrorType.PW_SHORT) || authError.type.equals(ErrorType.PW_WEAK) || authError.type.equals(ErrorType.PW_MISMATCH)),
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
          }
          Spacer(Modifier.size((maxHeightScreen.value * 0.03f).dp))
          Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Row() {
              Button(
                onClick = {
                  if(authMode == AuthMode.SignIn) viewModel.signIn(signInFormState.email, signInFormState.password) else viewModel.signUp(signInFormState.email, signInFormState.password)
                },
                enabled = uiState != AuthStatusState.Submitting && formState is AuthFormState.SignIn,
                contentPadding = PaddingValues(horizontal = (maxWidthScreen.value * 0.016f).dp, vertical = (maxHeightScreen.value * 0.02f).dp),
                shape = RoundedCornerShape((maxWidthScreen.value * 0.013f).dp),
                modifier = Modifier.height((maxHeightScreen.value * 0.07f).dp),
                colors = ButtonColors(
                  containerColor = Primary,
                  contentColor = White,
                  disabledContainerColor = Primary,
                  disabledContentColor = White
                )
              ) {
//                if(submitting) {
                if(uiState is AuthStatusState.Submitting) {
                  CircularProgressIndicator( color = White, modifier = Modifier.size((maxWidthScreen.value * 0.019f).dp))
                } else {
                  AnimatedContent(
                    targetState = authMode
                  ) { state ->
                    when(state) {
                      AuthMode.SignIn -> Text(
                          "START SESSION",
                          fontFamily = GSFlex,
                          style = Typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          fontSize = (maxWidthScreen.value * 0.013f).sp,
                          letterSpacing = (maxWidthScreen.value * 0.00012f).sp,
                          color = White,
                          modifier = Modifier.padding(0.dp),
                        )
                      AuthMode.SignUp -> Text(
                        "REGISTER",
                        fontFamily = GSFlex,
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = (maxWidthScreen.value * 0.013f).sp,
                        letterSpacing = (maxWidthScreen.value * 0.00012f).sp,
                        color = White,
                        modifier = Modifier.padding(0.dp),
                      )
                    }
                  }
                }
              }
              Spacer(Modifier.width((maxWidthScreen.value * 0.009f).dp))
              Button(
                onClick = {
                  viewModel.changeAuthMode(if(authMode == AuthMode.SignIn) AuthMode.SignUp else AuthMode.SignIn)
                },
                enabled = uiState != AuthStatusState.Submitting && formState is AuthFormState.SignIn,
                contentPadding = PaddingValues(horizontal = (maxWidthScreen.value * 0.016f).dp, vertical = (maxHeightScreen.value * 0.02f).dp),
                shape = RoundedCornerShape((maxWidthScreen.value * 0.013f).dp),
                modifier = Modifier.height((maxHeightScreen.value * 0.07f).dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = White.copy(alpha = 0.4f),
                  contentColor = Primary.copy(alpha = 0.4f)
                )
              ) {
                AnimatedContent(
                  targetState = authMode
                ) { state ->
                  when(state) {
                    AuthMode.SignIn -> Text(
                      "Sign Up",
                      fontFamily = GSFlex,
                      style = Typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      fontSize = (maxWidthScreen.value * 0.013f).sp,
                      letterSpacing = (maxWidthScreen.value * 0.00012f).sp,
                      color = White,
                      modifier = Modifier.padding(0.dp),
                    )
                    AuthMode.SignUp -> Text(
                      "Go Back",
                      fontFamily = GSFlex,
                      style = Typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      fontSize = (maxWidthScreen.value * 0.013f).sp,
                      letterSpacing = (maxWidthScreen.value * 0.00012f).sp,
                      color = White,
                      modifier = Modifier.padding(0.dp),
                    )
                  }
                }
              }
            }
            AnimatedVisibility(
              visible = authMode == AuthMode.SignIn,
            ) {
              TextButton(
                onClick = {viewModel.onChangeAuthFormState(AuthFormState.ForgotPassword)},
                enabled = uiState is AuthStatusState.SignedOut && formState is AuthFormState.SignIn,
                contentPadding = PaddingValues(horizontal = (maxWidthScreen.value * 0.016f).dp, vertical = (maxHeightScreen.value * 0.02f).dp),
                shape = RoundedCornerShape((maxWidthScreen.value * 0.013f).dp),
                modifier = Modifier.height((maxHeightScreen.value * 0.07f).dp),
              ) {
                Text(
                  "Forgot Password?",
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
      Spacer(modifier = Modifier.size((maxWidthScreen.value * 0.025).dp))
      VerticalDivider(color = White)
    }
  }
}