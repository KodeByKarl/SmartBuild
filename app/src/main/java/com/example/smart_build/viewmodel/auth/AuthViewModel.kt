package com.example.smart_build.viewmodel.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import io.github.jan.supabase.SupabaseClient
import com.example.smart_build.data.client.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
//import io.github.jan.supabase.auth.SessionStatus
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


// ============================================================================
// ERROR
// ============================================================================

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


// ============================================================================
// AUTH FORM MODE
// ============================================================================

enum class AuthMode {
  SignIn,
  SignUp
}


// ============================================================================
// FORM STATES
// ============================================================================

data class SignInFormState(
  val email: String = "",
  val password: String = ""
)

data class ForgotPasswordFormState(
  val email: String = ""
)

data class ResetPasswordFormState(
  val password: String = "",
  val confirmPassword: String = ""
)

data class Error(
  val type: ErrorType = ErrorType.NONE,
  val message: String = ""
)


// ============================================================================
// AUTH FORM STATE
// ============================================================================
/*
sealed interface AuthFormState {

  data object None : AuthFormState

  data object SignIn : AuthFormState

  data object ForgotPassword : AuthFormState

  data object ResetPassword : AuthFormState
}

 */


// ============================================================================
// AUTH STATUS STATE
// ============================================================================
/*
sealed interface AuthStatusState {

  data object Loading : AuthStatusState

  data object SignedOut : AuthStatusState

  data object Submitting : AuthStatusState

  data object SignedIn : AuthStatusState

  data object Registered : AuthStatusState

  data object Error : AuthStatusState
}

 */


// ============================================================================
// VIEWMODEL
// ============================================================================

class AuthViewModel : ViewModel() {

  private val supabase = SupabaseClient.client

//  private var isPasswordRecovery = false
  private val _isPasswordRecovery = MutableStateFlow(false)

  val isPasswordRecovery = _isPasswordRecovery.asStateFlow()

  // ------------------------------------------------------------------------
  // UI STATE
  // ------------------------------------------------------------------------

  private val _uiState =
    MutableStateFlow<AuthStatusState>(
      AuthStatusState.Loading
    )

  private val _formState =
    MutableStateFlow<AuthFormState>(
      AuthFormState.None
    )

  private val _authMode =
    MutableStateFlow(
      AuthMode.SignIn
    )


  // ------------------------------------------------------------------------
  // FORM STATE
  // ------------------------------------------------------------------------

  private val _signInFormState =
    MutableStateFlow(
      SignInFormState()
    )

  private val _fpFormState =
    MutableStateFlow(
      ForgotPasswordFormState()
    )

  private val _rpFormState =
    MutableStateFlow(
      ResetPasswordFormState()
    )


  // ------------------------------------------------------------------------
  // ERROR
  // ------------------------------------------------------------------------

  private val _error =
    MutableStateFlow(
      Error()
    )


  // ------------------------------------------------------------------------
  // PUBLIC STATE
  // ------------------------------------------------------------------------

  val uiState =
    _uiState.asStateFlow()

  val formState =
    _formState.asStateFlow()

  val authMode =
    _authMode.asStateFlow()

  val signInFormState =
    _signInFormState.asStateFlow()

  val fpFormState =
    _fpFormState.asStateFlow()

  val rpFormState =
    _rpFormState.asStateFlow()

  val authError =
    _error.asStateFlow()


  // =========================================================================
  // INITIALIZATION
  // =========================================================================

  init {
    observeAuthSession()
    observeAuthEvents()
  }


  // =========================================================================
  // SESSION
  // =========================================================================
  @OptIn(SupabaseExperimental::class)
  private fun observeAuthEvents() {

    viewModelScope.launch {

      supabase.auth.events.collect { event ->

        Log.d(
          "AUTH_EVENT",
          "Supabase auth event: $event"
        )
      }
    }
  }

  @OptIn(SupabaseExperimental::class)
  private fun observeAuthSession() {
    viewModelScope.launch {
      supabase.auth.sessionStatus.collect { status ->
        Log.d(
          "AUTH_SESSION",
          "Session status: $status"
        )

        when (status) {
          // --------------------------------------------------------
          // Supabase is loading the saved session.
          // --------------------------------------------------------
          is SessionStatus.Initializing -> {
            _uiState.value = AuthStatusState.Loading
          }


          // --------------------------------------------------------
          // User has an authenticated session.
          // --------------------------------------------------------
//          is SessionStatus.Authenticated -> {
//
//            /*
//             * IMPORTANT:
//             *
//             * If the user is currently on ResetPassword,
//             * don't immediately navigate them to Home.
//             *
//             * The reset link creates an authenticated
//             * recovery session, which is exactly what we
//             * need for updateUser().
//             */
//
//            if (_formState.value != AuthFormState.ResetPassword) {
//              _uiState.value = AuthStatusState.SignedOut
//              _formState.value = AuthFormState.ResetPassword
//            }
//          }

          /*
          is SessionStatus.Authenticated -> {
            Log.d(
              "AUTH_SESSION",
              "Authenticated recovery=${_isPasswordRecovery.value}"
            )

            if (_isPasswordRecovery.value) {

              /*
               * This session came from a password recovery
               * deeplink.
               *
               * Keep the authenticated recovery session because
               * updateUser() needs it.
               */

              _uiState.value =
                AuthStatusState.SignedOut

              _formState.value =
                AuthFormState.ResetPassword

              /*
               * Important:
               *
               * Consume the recovery state.
               *
               * If the application is restarted later,
               * isPasswordRecovery will be false.
               */

//              _isPasswordRecovery.value = false

            } else {

              /*
               * Normal authenticated session.
               *
               * This includes:
               *
               * - normal sign in
               * - restored session after app restart
               * - refreshed session
               */

              _uiState.value =
                AuthStatusState.SignedIn

              _formState.value =
                AuthFormState.None
            }
          }

           */

          is SessionStatus.Authenticated -> {

            val session = status.session

            Log.d(
              "AUTH_SESSION",
              "Authenticated type=${session.type}"
            )

            if (session.type == "recovery") {

              Log.d(
                "AUTH_RECOVERY",
                "Recovery session detected"
              )

              _isPasswordRecovery.value = true

              _uiState.value =
                AuthStatusState.SignedOut

              _formState.value =
                AuthFormState.ResetPassword

            } else {

              Log.d(
                "AUTH_SESSION",
                "Normal authenticated session"
              )

              _isPasswordRecovery.value = false

              _uiState.value =
                AuthStatusState.SignedIn

              _formState.value =
                AuthFormState.None
            }
          }

          // --------------------------------------------------------
          // No authenticated session.
          // --------------------------------------------------------
          is SessionStatus.NotAuthenticated -> {
            _uiState.value = AuthStatusState.SignedOut

            /*
             * Don't destroy the current form if the user
             * is already on ForgotPassword / SignUp / etc.
             */

            if (_formState.value == AuthFormState.None) {
              _formState.value = AuthFormState.SignIn
            }
          }

          // --------------------------------------------------------
          // Session refresh failed.
          // --------------------------------------------------------
          is SessionStatus.RefreshFailure -> {
            _uiState.value = AuthStatusState.SignedOut
            _formState.value = AuthFormState.SignIn
            _authMode.value = AuthMode.SignIn
          }
        }
      }
    }
  }


  // =========================================================================
  // SIGN IN / SIGN UP MODE
  // =========================================================================

  fun changeAuthMode(
    mode: AuthMode
  ) {

    clearErrorOnly()

    _authMode.value =
      mode

    when (mode) {

      AuthMode.SignIn -> {

        _formState.value =
          AuthFormState.SignIn
      }

      AuthMode.SignUp -> {

        _formState.value =
          AuthFormState.SignIn
      }
    }
  }


  // =========================================================================
  // SIGN UP
  // =========================================================================

  fun signUp(
    email: String,
    password: String
  ) {

    clearErrorOnly()

    val trimmedEmail =
      email.trim()

    val trimmedPassword =
      password.trim()


    // ---------------------------------------------------------------------
    // VALIDATION
    // ---------------------------------------------------------------------

    if (trimmedEmail.isBlank()) {

      showError(
        type = ErrorType.EMAIL_BLANK,
        message = "Do not leave the email blank."
      )

      return
    }


    if (!isValidEmail(trimmedEmail)) {

      showError(
        type = ErrorType.EMAIL_INVALID,
        message = "Please enter a valid email address."
      )

      return
    }


    if (trimmedPassword.isBlank()) {

      showError(
        type = ErrorType.PW_BLANK,
        message = "Do not leave the password blank."
      )

      return
    }


    if (trimmedPassword.length < 8) {

      showError(
        type = ErrorType.PW_SHORT,
        message =
          "Password must have at least 8 characters."
      )

      return
    }


    // ---------------------------------------------------------------------
    // SUPABASE SIGN UP
    // ---------------------------------------------------------------------

    viewModelScope.launch {

      _uiState.value =
        AuthStatusState.Submitting

      try {

        val user =
          supabase.auth.signUpWith(Email) {

            this.email =
              trimmedEmail

            this.password =
              trimmedPassword
          }


        /*
         * IMPORTANT:
         *
         * signUpWith() returning does NOT automatically mean
         * the user is signed in.
         *
         * With email confirmation enabled:
         *
         *      user != null
         *      session == null
         *
         * The user must verify their email first.
         */

        val session =
          supabase.auth.currentSessionOrNull()


        if (session != null) {

          /*
           * This happens when email confirmation is disabled.
           *
           * The Supabase session observer will normally handle
           * SignedIn, but we can leave this here as a fallback.
           */

          _uiState.value =
            AuthStatusState.SignedIn

          _formState.value =
            AuthFormState.None

        } else {

          /*
           * Email confirmation is enabled.
           *
           * Tell the UI that registration succeeded.
           */

          _uiState.value =
            AuthStatusState.Registered

          _error.value =
            Error(
              type = ErrorType.NONE,
              message =
                "Account created! Check your email to verify your account."
            )

          /*
           * Keep the user on the sign-in form.
           */

          _formState.value =
            AuthFormState.SignIn

          _authMode.value =
            AuthMode.SignIn
        }

      } catch (e: Exception) {

        Log.e(
          "AUTH",
          "Sign up failed",
          e
        )

        val message =
          e.message
            ?.lowercase()
            ?: ""

        /*
         * If Supabase is configured to reveal an existing
         * account, it can return "User already registered".
         *
         * When email enumeration protection is enabled,
         * Supabase may intentionally return an obfuscated
         * response instead.
         */

        if (
          message.contains(
            "user already registered"
          ) ||
          message.contains(
            "already registered"
          )
        ) {

          showError(
            type =
              ErrorType.ACCOUNT_EXISTING,
            message =
              "An account with this email already exists."
          )

        } else {

          showError(
            type =
              ErrorType.AUTH_FAILED,
            message =
              "Unable to create your account."
          )
        }
      }
    }
  }


  // =========================================================================
  // SIGN IN
  // =========================================================================

  fun signIn(
    email: String,
    password: String
  ) {

    clearErrorOnly()

    val trimmedEmail =
      email.trim()

    val trimmedPassword =
      password.trim()


    // ---------------------------------------------------------------------
    // VALIDATION
    // ---------------------------------------------------------------------

    if (trimmedEmail.isBlank()) {

      showError(
        type = ErrorType.EMAIL_BLANK,
        message = "Do not leave the email blank."
      )

      return
    }


    if (!isValidEmail(trimmedEmail)) {

      showError(
        type = ErrorType.EMAIL_INVALID,
        message = "Please enter a valid email address."
      )

      return
    }


    if (trimmedPassword.isBlank()) {

      showError(
        type = ErrorType.PW_BLANK,
        message = "Do not leave the password blank."
      )

      return
    }


    if (trimmedPassword.length < 8) {

      showError(
        type = ErrorType.PW_SHORT,
        message =
          "Password must have at least 8 characters."
      )

      return
    }


    // ---------------------------------------------------------------------
    // SUPABASE SIGN IN
    // ---------------------------------------------------------------------

    viewModelScope.launch {

      _uiState.value =
        AuthStatusState.Submitting

      try {

        supabase.auth.signInWith(Email) {

          this.email =
            trimmedEmail

          this.password =
            trimmedPassword
        }


        /*
         * If signInWith() succeeds, Supabase has created/
         * restored the authenticated session.
         *
         * sessionStatus will also emit Authenticated.
         */

        _uiState.value =
          AuthStatusState.SignedIn

        _formState.value =
          AuthFormState.None

      } catch (e: Exception) {

        Log.e(
          "AUTH",
          "Sign in failed",
          e
        )

        val message =
          e.message
            ?.lowercase()
            ?: ""


        /*
         * Supabase commonly returns an email-confirmation
         * error when Confirm Email is enabled.
         */

        if (
          message.contains(
            "email not confirmed"
          ) ||
          message.contains(
            "email_not_confirmed"
          ) ||
          message.contains(
            "email is not confirmed"
          )
        ) {

          showError(
            type =
              ErrorType.ACCOUNT_NOT_VERIFIED,
            message =
              "Please verify your email before signing in."
          )

        } else {

          /*
           * IMPORTANT:
           *
           * invalid_credentials can mean:
           *
           * - email doesn't exist
           * - password is wrong
           *
           * We deliberately don't tell the user which one.
           */

          showError(
            type =
              ErrorType.INVALID_CREDENTIALS,
            message =
              "Email or password is incorrect."
          )
        }

        _formState.value =
          AuthFormState.SignIn
      }
    }
  }


  // =========================================================================
  // SIGN OUT
  // =========================================================================

  fun signOut() {

    viewModelScope.launch {

      try {

        supabase.auth.signOut()

        /*
         * SessionStatus.NotAuthenticated will update
         * the UI state to SignedOut.
         */

      } catch (e: Exception) {

        Log.e(
          "AUTH",
          "Sign out failed",
          e
        )

        showError(
          type = ErrorType.AUTH_FAILED,
          message = "Unable to sign out."
        )
      }
    }
  }


  // =========================================================================
  // FORGOT PASSWORD
  // =========================================================================

  fun forgotPassword(
    email: String
  ) {

    clearErrorOnly()

    val trimmedEmail =
      email.trim()


    // ---------------------------------------------------------------------
    // VALIDATION
    // ---------------------------------------------------------------------

    if (trimmedEmail.isBlank()) {

      showError(
        type = ErrorType.EMAIL_BLANK,
        message = "Do not leave the email blank."
      )

      return
    }


    if (!isValidEmail(trimmedEmail)) {

      showError(
        type = ErrorType.EMAIL_INVALID,
        message = "Please enter a valid email address."
      )

      return
    }


    // ---------------------------------------------------------------------
    // SEND RESET EMAIL
    // ---------------------------------------------------------------------

    viewModelScope.launch {

      _uiState.value =
        AuthStatusState.Submitting

      try {
        supabase.auth.resetPasswordForEmail(
          email = trimmedEmail,
          redirectUrl = "smartbuild://auth"
        )

        supabase.auth.signOut()

        _uiState.value =
          AuthStatusState.Registered

        _formState.value =
          AuthFormState.SignIn

        _authMode.value =
          AuthMode.SignIn

        /*
        _uiState.value =
          AuthStatusState.Registered

        _error.value =
          Error(
            type = ErrorType.NONE,
            message =
              "If an account exists for this email, a password reset link has been sent."
          )

        _formState.value =
          AuthFormState.SignIn

         */

      } catch (e: Exception) {

        Log.e(
          "AUTH",
          "Password reset request failed",
          e
        )

        showError(
          type =
            ErrorType.RESET_FAILED,
          message =
            "Unable to send the password reset email."
        )

        _formState.value =
          AuthFormState.ForgotPassword
      }
    }
  }


  // =========================================================================
  // RESET PASSWORD
  // =========================================================================

  fun resetPassword(
    password: String,
//    confirmPassword: String
  ) {

    clearErrorOnly()

    val trimmedPassword =
      password.trim()

//    val trimmedConfirmPassword =
//      confirmPassword.trim()


    // ---------------------------------------------------------------------
    // VALIDATION
    // ---------------------------------------------------------------------

    if (trimmedPassword.isBlank()) {

      showError(
        type = ErrorType.PW_BLANK,
        message = "Do not leave the password blank."
      )

      return
    }


    if (trimmedPassword.length < 8) {

      showError(
        type = ErrorType.PW_SHORT,
        message =
          "Password must have at least 8 characters."
      )

      return
    }


//    if (
//      trimmedPassword !=
//      trimmedConfirmPassword
//    ) {
//
//      showError(
//        type = ErrorType.PW_MISMATCH,
//        message =
//          "Passwords do not match."
//      )
//
//      return
//    }


    // ---------------------------------------------------------------------
    // CHECK RECOVERY SESSION
    // ---------------------------------------------------------------------

    viewModelScope.launch {

      _uiState.value =
        AuthStatusState.Submitting

      try {

        /*
         * The reset link should have created an
         * authenticated recovery session.
         */

        val session =
          supabase.auth.currentSessionOrNull()

        if (session == null) {

          showError(
            type =
              ErrorType.RESET_SESSION_EXPIRED,
            message =
              "The password reset session has expired. Please request a new reset link."
          )

          _formState.value =
            AuthFormState.ForgotPassword

          return@launch
        }


        // -------------------------------------------------------------
        // UPDATE PASSWORD
        // -------------------------------------------------------------

        supabase.auth.updateUser {

          this.password =
            trimmedPassword
        }


        // -------------------------------------------------------------
        // SUCCESS
        // -------------------------------------------------------------

        _error.value =
          Error(
            type = ErrorType.NONE,
            message =
              "Password successfully changed. You can now sign in with your new password."
          )


        /*
         * Sign out after changing the password so the user
         * goes through the normal login flow.
         */

        supabase.auth.signOut()

        _uiState.value =
          AuthStatusState.Registered

        _formState.value =
          AuthFormState.SignIn

        _authMode.value =
          AuthMode.SignIn

      } catch (e: Exception) {

        Log.e(
          "AUTH",
          "Password reset failed",
          e
        )

        val message =
          e.message
            ?.lowercase()
            ?: ""

        if (
          message.contains(
            "session"
          ) ||
          message.contains(
            "expired"
          ) ||
          message.contains(
            "not authenticated"
          )
        ) {

          showError(
            type =
              ErrorType.RESET_SESSION_EXPIRED,
            message =
              "The password reset session has expired. Please request a new reset link."
          )

        } else {

          showError(
            type =
              ErrorType.RESET_FAILED,
            message =
              "Unable to change the password."
          )
        }

        _formState.value =
          AuthFormState.ResetPassword
      }
    }
  }


  // =========================================================================
  // FORM NAVIGATION
  // =========================================================================

  fun changeForm(
    state: AuthFormState
  ) {

    clearErrorOnly()

    _formState.value =
      state
  }




  fun onChangeAuthFormState(
    state: AuthFormState
  ) {

    clearErrorOnly()

    _formState.value =
      state
  }


  // =========================================================================
  // SIGN IN / SIGN UP FORM
  // =========================================================================

  fun onEmailSIChanged(
    email: String
  ) {

    _signInFormState.update {

      it.copy(
        email = email
      )
    }
  }


  fun onPWSIChanged(
    password: String
  ) {

    _signInFormState.update {

      it.copy(
        password = password
      )
    }
  }


  // =========================================================================
  // FORGOT PASSWORD FORM
  // =========================================================================

  fun onEmailFPChanged(
    email: String
  ) {

    _fpFormState.update {

      it.copy(
        email = email
      )
    }
  }


  // =========================================================================
  // RESET PASSWORD FORM
  // =========================================================================

  fun onPWRPChanged(
    password: String
  ) {

    _rpFormState.update {

      it.copy(
        password = password
      )
    }
  }


  fun onConfirmPWRPChanged(
    password: String
  ) {

    _rpFormState.update {

      it.copy(
        confirmPassword = password
      )
    }
  }


  // =========================================================================
  // ERROR
  // =========================================================================

  fun clearError() {

    _error.value =
      Error()

    if (
      _uiState.value ==
      AuthStatusState.Error
    ) {

      _uiState.value =
        AuthStatusState.SignedOut
    }
  }

  /*
  fun deleteAccount() {

    viewModelScope.launch {

      try {

        _uiState.value =
          AuthStatusState.Submitting

        Log.d(
          "DELETE_ACCOUNT",
          "Calling delete-user Edge Function..."
        )

        val result =
          SupabaseClient.client.functions.invoke(
            "delete-user"
          )

        Log.d(
          "DELETE_ACCOUNT",
          "Delete result: $result"
        )

        /*
         * The Edge Function has successfully
         * deleted the Auth user.
         *
         * Now sign out locally.
         */
        SupabaseClient.client.auth.signOut()

        _uiState.value =
          AuthStatusState.SignedOut

        _formState.value =
          AuthFormState.SignIn

      } catch (e: Exception) {

        Log.e(
          "DELETE_ACCOUNT",
          "Failed to delete account",
          e
        )

        _uiState.value =
          AuthStatusState.Error

        _error.value =
          Error(
            type = ErrorType.NONE,
            message = "Unable to delete your account."
          )
      }
    }
  }

   */
//  fun deleteAccount() {
//
//    viewModelScope.launch {
//
//      try {
//
//        _uiState.value =
//          AuthStatusState.Submitting
//
//        Log.d(
//          "DELETE_ACCOUNT",
//          "Calling delete-user Edge Function..."
//        )
//
//        val result =
//          supabase.functions.invoke(
//            "delete-user"
//          )
//
//        Log.d(
//          "DELETE_ACCOUNT",
//          "Delete result: $result"
//        )
//
//        /*
//         * The Auth user has now been deleted
//         * by the Edge Function.
//         *
//         * Clear the local Supabase session.
//         */
//        supabase.auth.signOut()
//
//        _uiState.value =
//          AuthStatusState.SignedOut
//
//        _formState.value =
//          AuthFormState.SignIn
//
//        _authMode.value =
//          AuthMode.SignIn
//
//      } catch (e: Exception) {
//
//        Log.e(
//          "DELETE_ACCOUNT",
//          "Failed to delete account",
//          e
//        )
//
//        showError(
//          type = ErrorType.AUTH_FAILED,
//          message =
//            "Unable to delete your account. Please try again."
//        )
//      }
//    }
//  }

  fun deleteAccount() {

    viewModelScope.launch {

      try {

        _uiState.value =
          AuthStatusState.Submitting

        Log.d(
          "DELETE_ACCOUNT",
          "Calling delete-user Edge Function..."
        )

        SupabaseClient.client.functions.invoke(
          "delete-user"
        )

        Log.d(
          "DELETE_ACCOUNT",
          "Account deleted successfully."
        )

        // ---------------------------------------------------------
        // IMPORTANT:
        // The Edge Function deleted the Auth user on the server.
        // Now remove the locally stored session.
        // ---------------------------------------------------------

        SupabaseClient.client.auth.signOut()

        Log.d(
          "DELETE_ACCOUNT",
          "Local session signed out."
        )

        // ---------------------------------------------------------
        // Return to authentication screen.
        // ---------------------------------------------------------

        _uiState.value =
          AuthStatusState.SignedOut

        _formState.value =
          AuthFormState.SignIn

        _authMode.value =
          AuthMode.SignIn

      } catch (e: Exception) {

        Log.e(
          "DELETE_ACCOUNT",
          "Failed to delete account",
          e
        )

        _uiState.value =
          AuthStatusState.Error

        _error.value =
          Error(
            type = ErrorType.AUTH_FAILED,
            message =
              "Unable to delete your account."
          )
      }
    }
  }

  private fun clearErrorOnly() {

    _error.value =
      Error()
  }


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


  // =========================================================================
  // VALIDATION
  // =========================================================================

  private fun isValidEmail(
    email: String
  ): Boolean {

    return android.util.Patterns.EMAIL_ADDRESS
      .matcher(email)
      .matches()
  }

  fun onPasswordRecoveryDetected() {

    _isPasswordRecovery.value = true

    _formState.value =
      AuthFormState.ResetPassword

    Log.d(
      "AUTH_RECOVERY",
      "RECOVERY FLAG SET"
    )
  }
}