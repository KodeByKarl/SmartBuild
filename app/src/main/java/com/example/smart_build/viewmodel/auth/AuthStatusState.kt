package com.example.smart_build.viewmodel.auth

sealed interface AuthStatusState {
  data object Loading : AuthStatusState
  data object Submitting : AuthStatusState
  data object Registered: AuthStatusState
  data object SignedIn : AuthStatusState
  data object SignedOut : AuthStatusState
  data object Error : AuthStatusState
}