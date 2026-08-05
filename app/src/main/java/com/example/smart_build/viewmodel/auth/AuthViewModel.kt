package com.example.smart_build.viewmodel.auth

/*
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_build.data.client.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

//enum class ErrorType {
//  NONE,
//  EMAIL_BLANK,
//  EMAIL_INVALID,
//  PW_BLANK,
//  PW_SHORT,
//  PW_WEAK,
//  PW_MISMATCH
//}

enum class ErrorType {
  NONE,

  EMAIL_BLANK,
  EMAIL_INVALID,

  PW_BLANK,
  PW_SHORT,
  PW_WEAK,
  PW_MISMATCH,

  INVALID_CREDENTIALS,
  ACCOUNT_EXISTING,

  AUTH_FAILED,
  ACCOUNT_NOT_VERIFIED,
  RESET_FAILED,
  RESET_SESSION_EXPIRED,

  UNKNOWN
}

enum class AuthMode {
  SignUp,
  SignIn
}

data class SignInFormState(val email: String = "", val password: String = "")
data class ForgotPasswordFormState(val email: String = "")
data class ResetPasswordFormState(val password: String = "")
data class Error(val type: ErrorType = ErrorType.NONE, val message: String = "")

class AuthViewModel : ViewModel() {
  private val supabase = SupabaseClient.client

  private val _uiState = MutableStateFlow<AuthStatusState>(AuthStatusState.Loading)
  private val _formState = MutableStateFlow<AuthFormState>(AuthFormState.None)
  private val _signInFormState = MutableStateFlow(SignInFormState())
  private val _fpFormState = MutableStateFlow(ForgotPasswordFormState())
  private val _rpFormState = MutableStateFlow(ResetPasswordFormState())
  private val _error = MutableStateFlow(Error())
  private val _authMode = MutableStateFlow(AuthMode.SignIn)

  val uiState = _uiState.asStateFlow()
  val formState = _formState.asStateFlow()
  val signInFormState = _signInFormState.asStateFlow()
  val fpFormState = _fpFormState.asStateFlow()
  val rpFormState = _rpFormState.asStateFlow()
  val authError = _error.asStateFlow()
  val authMode = _authMode.asStateFlow()

  fun changeAuthMode(authMode: AuthMode) {
    _authMode.value = authMode
  }

  init {
    observeAuthSession()
  }

  // -------------------------------------------------------------------------
  // SESSION
  // -------------------------------------------------------------------------

  @OptIn(SupabaseExperimental::class)
  private fun observeAuthSession() {
    viewModelScope.launch {
      supabase.auth.sessionStatus.collect { status ->
        when (status) {
          is SessionStatus.Initializing -> {
            _uiState.value =
              AuthStatusState.Loading
          }
          is SessionStatus.Authenticated -> {
            /*
             * Don't immediately switch to SignedIn if
             * the user is currently doing password recovery.
             *
             * The recovery event/session will be handled
             * separately.
             */
            if (_formState.value != AuthFormState.ResetPassword) {
              _uiState.value =
                AuthStatusState.SignedIn

              _formState.value =
                AuthFormState.None
            }
          }

          is SessionStatus.NotAuthenticated -> {
            _uiState.value =
              AuthStatusState.SignedOut
            if (_formState.value == AuthFormState.None) {
              _formState.value =
                AuthFormState.SignIn
            }
          }

          is SessionStatus.RefreshFailure -> {
            _uiState.value =
              AuthStatusState.SignedOut
            _formState.value =
              AuthFormState.SignIn
          }
        }
      }
    }

    /*
     * Listen for authentication events.
     *
     * This is particularly important for password recovery.
     */
    viewModelScope.launch {
      supabase.auth.events.collect { event ->
        /*
         * The exact event classes can depend on the
         * supabase-kt version you're using.
         *
         * If your version exposes PasswordRecovery,
         * handle it here.
         */
      }
    }
  }

  /*
  init {
    checkUser()
  }

  private fun checkUser() {
    viewModelScope.launch {
      // Update this, check if there is a user signed in.
      delay(3000.milliseconds)
      _uiState.value = AuthStatusState.SignedOut
      _formState.value = AuthFormState.SignIn
    }
  }
   */

  fun signUp(email: String, password: String) {
    _uiState.value = AuthStatusState.Submitting

    val trimmed_email: String = email.trim()
    val trimmed_pw: String = password.trim()

    // Check if the text fields are empty.
    if(trimmed_email.isEmpty() || trimmed_email.isBlank()) {
      _uiState.value = AuthStatusState.Error
      _error.update {
        it.copy(
          type = ErrorType.EMAIL_BLANK,
          message = "Do not leave the email blank."
        )
      }
      return
    }

    if(trimmed_pw.isEmpty() || trimmed_pw.isBlank()) {
      _uiState.value = AuthStatusState.Error
      _error.update {
        it.copy(
          type = ErrorType.PW_BLANK,
          message = "Do not leave the password blank."
        )
      }
      return
    }

    // Check if email is valid.

    // Check if pw is valid:
    //   - Minimum of 8 characters.
    if(password.length < 8) {
      _uiState.value = AuthStatusState.Error
      _error.update {
        it.copy(
          type = ErrorType.PW_SHORT,
          message = "Password must have at least 8 characters."
        )
      }

      return
    }

    // Sign up user.
    viewModelScope.launch {
      try {
        supabase.auth.signUpWith(Email) {
          this.email = trimmed_email
          this.password = trimmed_pw
        }

        _uiState.value = AuthStatusState.SignedOut
        _formState.value = AuthFormState.SignIn
      } catch(e: Exception) {
        Log.e("AUTH", "${e.message}")


        _uiState.value = AuthStatusState.Error
        _error.update { it.copy(ErrorType.ACCOUNT_EXISTING, message = "Account is already existing.") }
        _formState.value = AuthFormState.SignIn
      }
    }
  }

  fun signIn(email: String, password: String) {
    _uiState.value = AuthStatusState.Submitting
    // TODO: Connect to Supabase.

    val trimmed_email: String = email.trim()
    val trimmed_pw: String = password.trim()

    // Check if the text fields are empty.
    if(trimmed_email.isEmpty() || trimmed_email.isBlank()) {
      _uiState.value = AuthStatusState.Error
      _error.update {
        it.copy(
          type = ErrorType.EMAIL_BLANK,
          message = "Do not leave the email blank."
        )
      }
      return
    }

    if(trimmed_pw.isEmpty() || trimmed_pw.isBlank()) {
      _uiState.value = AuthStatusState.Error
      _error.update {
        it.copy(
          type = ErrorType.PW_BLANK,
          message = "Do not leave the password blank."
        )
      }
      return
    }

    // Check if email is valid.

    // Check if pw is valid:
    //   - Minimum of 8 characters.
    if(password.length < 8) {
      _uiState.value = AuthStatusState.Error
      _error.update {
        it.copy(
          type = ErrorType.PW_SHORT,
          message = "Password must have at least 8 characters."
        )
      }

      return
    }
    //   - Do not accept if weak.

    // Check if account is registered.
    //   - Register the user if not.
    // Just simulation.
//    val fk_email: String = "kobeb7952@gmail.com"
//    val fk_pw: String = "12345678"
//    val fk_verified: Boolean = false
//    if(fk_email != email) { // Change the condition.
//      _uiState.value = AuthStatusState.Registered
//      _error.update {
//        it.copy(
//          type  = ErrorType.NONE,
//          message = "Account is successfully registered! Check your email to verify."
//        )
//      }
//
//      return
//    }
//
//    if(fk_pw != password) {
//      _uiState.value = AuthStatusState.Error
//      _error.update {
//        it.copy(
//          type  = ErrorType.PW_MISMATCH,
//          message = "Password did not match."
//        )
//      }
//
//      return
//    }

    // Check if account is verified.
    //   - Send email allowing user to verify their account.
//    if(fk_verified) { // Change the condition.
//      _uiState.value = AuthStatusState.Registered
//      _error.update {
//        it.copy(
//          type  = ErrorType.NONE,
//          message = "Account is not yet verified. Check your email."
//        )
//      }
//
//      return
//    }

    // Sign in user.
    viewModelScope.launch {
      try {
        supabase.auth.signInWith(Email) {
          this.email = trimmed_email
          this.password = trimmed_pw
        }

        _uiState.value = AuthStatusState.SignedIn
        _formState.value = AuthFormState.SignIn
      } catch(e: Exception) {
        Log.e("AUTH", "${e.message}")

        _uiState.value = AuthStatusState.Error
        _error.update { it.copy(ErrorType.INVALID_CREDENTIALS, "Invalid Credentials.") }
        _formState.value = AuthFormState.SignIn
      }
    }
  }

  fun changeForm(state: AuthFormState) {
    viewModelScope.launch {
      _formState.value = state
    }
  }

  fun onChangeAuthFormState(state: AuthFormState) {
    _formState.value = state
  }

  fun onEmailSIChanged(email: String) {
    _signInFormState.update {
      it.copy(email = email)
    }
  }

  fun onPWSIChanged(password: String) {
    _signInFormState.update {
      it.copy(password = password)
    }
  }
  fun onEmailFPChanged(email: String) {
    _fpFormState.update {
      it.copy(email = email)
    }
  }

  fun onPWRPChanged(password: String) {
    _rpFormState.update {
      it.copy(password = password)
    }
  }

  fun clearError() {
    _error.value = Error()

    if (_uiState.value == AuthStatusState.Error) {
      _uiState.value = AuthStatusState.SignedOut
    }
  }

  private fun clearErrorOnly() { _error.value = Error() }

  private fun showError(
    type: ErrorType,
    message: String
  ) {

    _error.value =
      Error(
        type = type,
        message = message
      )

    _uiState.value =
      AuthStatusState.Error
  }

  private fun isValidEmail(
    email: String
  ): Boolean {

    return android.util.Patterns.EMAIL_ADDRESS
      .matcher(email)
      .matches()
  }
}

 */