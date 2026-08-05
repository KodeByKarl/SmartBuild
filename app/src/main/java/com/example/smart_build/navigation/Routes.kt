package com.example.smart_build.navigation

sealed class Routes(val route: String) {
  data object LoginPage : Routes("ap/lp")
  data object ForgotPasswordPage : Routes("ap/fp")
  data object ResetPasswordPage : Routes("ap/rp")
  data object HomePage : Routes("hp")
}