package com.example.smart_build.viewmodel.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_build.data.client.SupabaseClient
import com.example.smart_build.viewmodel.auth.AuthStatusState
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
  private val supabase = SupabaseClient.client

  private val _authState = MutableStateFlow<AuthStatusState>(AuthStatusState.Loading)
  private val _showDeleteDialog = MutableStateFlow(false)
  val authState = _authState.asStateFlow()
  val session = supabase.auth.sessionStatus.value
  val emailAdd = supabase.auth.currentUserOrNull()!!.email

//  var showDeleteDialog by remember {
//    mutableStateOf(false)
//  }
  val showDeleteDialog = _showDeleteDialog.asStateFlow()

  fun changeShowDeleteDialog(show: Boolean) {
    _showDeleteDialog.value = show
  }

  fun signOut() {
    viewModelScope.launch {
      try {
        supabase.auth.signOut()
        _authState.value = AuthStatusState.SignedOut
      } catch(e: Exception) {
        Log.e("AUTH", "${e.message}")
      }
    }
  }


}