package com.example.smart_build.viewmodel.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_build.data.ModuleProgressStore
import com.example.smart_build.data.client.SupabaseClient
import com.example.smart_build.viewmodel.auth.AuthStatusState
import com.example.smart_build.viewmodel.auth.newPasswordProblem
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ChangePasswordState {
  data object Hidden : ChangePasswordState
  data class Editing(val error: String? = null) : ChangePasswordState
  data object Submitting : ChangePasswordState
  data object Done : ChangePasswordState
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
  private val supabase = SupabaseClient.client

  private val _authState = MutableStateFlow<AuthStatusState>(AuthStatusState.Loading)
  private val _showDeleteDialog = MutableStateFlow(false)
  private val _moduleProgress = MutableStateFlow<Map<Int, Float>>(emptyMap())
  private val _guidedDone = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
  private val _assessmentDone = MutableStateFlow<Map<Int, Boolean>>(emptyMap())

  val authState = _authState.asStateFlow()
  val showDeleteDialog = _showDeleteDialog.asStateFlow()
  val moduleProgress = _moduleProgress.asStateFlow()
  val guidedDoneMap = _guidedDone.asStateFlow()
  val assessmentDoneMap = _assessmentDone.asStateFlow()

  val session = supabase.auth.sessionStatus.value
  val emailAdd = supabase.auth.currentUserOrNull()?.email.orEmpty()

  init {
    refreshProgress()
  }

  fun changeShowDeleteDialog(show: Boolean) {
    _showDeleteDialog.value = show
  }

  private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Hidden)
  val changePasswordState = _changePasswordState.asStateFlow()

  fun openChangePassword() {
    _changePasswordState.value = ChangePasswordState.Editing()
  }

  fun closeChangePassword() {
    if (_changePasswordState.value != ChangePasswordState.Submitting) {
      _changePasswordState.value = ChangePasswordState.Hidden
    }
  }

  fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
    val current = currentPassword.trim()
    val next = newPassword.trim()
    val confirm = confirmPassword.trim()

    val problem = when {
      current.isBlank() -> "Enter your current password."
      next.isBlank() -> "Enter a new password."
      else -> newPasswordProblem(next)
        ?: if (next != confirm) "New passwords do not match."
        else if (next == current) "The new password must be different from the current one."
        else null
    }
    if (problem != null) {
      _changePasswordState.value = ChangePasswordState.Editing(problem)
      return
    }

    val email = supabase.auth.currentUserOrNull()?.email
    if (email.isNullOrBlank()) {
      _changePasswordState.value = ChangePasswordState.Editing("Session expired. Please sign in again.")
      return
    }

    _changePasswordState.value = ChangePasswordState.Submitting
    viewModelScope.launch {
      // Re-entering the current password proves it is the account owner, and the fresh
      // sign-in satisfies Supabase's "recent login" check for password changes.
      try {
        supabase.auth.signInWith(Email) {
          this.email = email
          this.password = current
        }
      } catch (e: Exception) {
        Log.e("AUTH", "Change password: current password check failed", e)
        _changePasswordState.value = ChangePasswordState.Editing("Current password is incorrect.")
        return@launch
      }
      try {
        supabase.auth.updateUser { password = next }
        _changePasswordState.value = ChangePasswordState.Done
      } catch (e: Exception) {
        Log.e("AUTH", "Change password failed", e)
        _changePasswordState.value = ChangePasswordState.Editing(
          "Unable to change the password. Check your connection and try again."
        )
      }
    }
  }

  fun refreshProgress() {
    val ctx = getApplication<Application>()
    ModuleProgressStore.bindCurrentUser(ctx)
    // Show this account's local cache immediately.
    _moduleProgress.value = ModuleProgressStore.allProgress(ctx)
    _guidedDone.value = (0..4).associateWith { ModuleProgressStore.guidedDone(ctx, it) }
    _assessmentDone.value = (0..4).associateWith { ModuleProgressStore.assessmentDone(ctx, it) }

    viewModelScope.launch {
      try {
        ModuleProgressStore.pullFromRemote(ctx)
        _moduleProgress.value = ModuleProgressStore.allProgress(ctx)
        _guidedDone.value = (0..4).associateWith { ModuleProgressStore.guidedDone(ctx, it) }
        _assessmentDone.value = (0..4).associateWith { ModuleProgressStore.assessmentDone(ctx, it) }
      } catch (e: Exception) {
        Log.e("HomeViewModel", "progress pull failed: ${e.message}", e)
      }
    }
  }

  fun progressOf(moduleId: Int): Float =
    _moduleProgress.value[moduleId] ?: ModuleProgressStore.progress(getApplication(), moduleId)

  fun signOut() {
    viewModelScope.launch {
      try {
        supabase.auth.signOut()
        // Detach the previous student's cache so Home never shows their bars.
        ModuleProgressStore.unbindAccount()
        _moduleProgress.value = emptyMap()
        _guidedDone.value = emptyMap()
        _assessmentDone.value = emptyMap()
        _authState.value = AuthStatusState.SignedOut
      } catch (e: Exception) {
        Log.e("AUTH", "${e.message}")
      }
    }
  }
}
