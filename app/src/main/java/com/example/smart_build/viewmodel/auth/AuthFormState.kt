package com.example.smart_build.viewmodel.auth

sealed interface AuthFormState {
  data object None : AuthFormState
//  data object NoneToSignIn : AuthFormState
  data object SignIn : AuthFormState
//  data object SignInToForgotPassword : AuthFormState
  data object ForgotPassword : AuthFormState
//  data object ForgotPasswordToResetPassword : AuthFormState
  data object ResetPassword : AuthFormState
}