package com.example.smart_build.viewmodel.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_build.data.ModuleProgressStore
import com.example.smart_build.data.client.SupabaseClient
import com.example.smart_build.viewmodel.auth.AuthStatusState
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

  fun refreshProgress() {
    val ctx = getApplication<Application>()
    // Show local cache immediately.
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
        _authState.value = AuthStatusState.SignedOut
      } catch (e: Exception) {
        Log.e("AUTH", "${e.message}")
      }
    }
  }
}
