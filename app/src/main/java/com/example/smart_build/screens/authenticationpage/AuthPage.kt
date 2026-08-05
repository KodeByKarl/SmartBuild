package com.example.smart_build.screens.authenticationpage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smart_build.navigation.Routes
import com.example.smart_build.screens.authenticationpage.components.AppLogoWithLoading
import com.example.smart_build.screens.authenticationpage.components.FPForm
import com.example.smart_build.screens.authenticationpage.components.RPForm
import com.example.smart_build.screens.authenticationpage.components.SignInForm
import com.example.smart_build.ui.theme.Black
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.viewmodel.auth.AuthFormState
import com.example.smart_build.viewmodel.auth.AuthStatusState
//import com.example.smart_build.viewmodel.auth.AuthViewModel
import com.example.smart_build.viewmodel.auth.AuthViewModel1

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AuthPage(
  navController : NavHostController,
//  viewModel : AuthViewModel = viewModel()
  viewModel : AuthViewModel1
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val formState by viewModel.formState.collectAsStateWithLifecycle()
  val authMode by viewModel.authMode.collectAsStateWithLifecycle()

  LaunchedEffect(uiState) {
    when (uiState) {
      AuthStatusState.SignedIn -> {
        navController.navigate(Routes.HomePage.route) {
          popUpTo(Routes.LoginPage.route) {
            inclusive = true
          }
        }
      }

      else -> Unit
    }
  }

  val verySunnyShapeRotation by animateFloatAsState(
    targetValue = if(formState is AuthFormState.SignIn) -90f else 45f,
    animationSpec = tween(1000)
  )

  val softBurstShapeRotation by animateFloatAsState(
    targetValue = if(formState is AuthFormState.SignIn) 90f else -45f,
    animationSpec = tween(1000)
  )

  val appLogoScale by animateFloatAsState(
    targetValue = if(uiState is AuthStatusState.Loading) 1f else 0.8f,
    animationSpec = tween(1000)
  )

  BoxWithConstraints (
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .fillMaxSize()
      .background(Black)
  ) {

    val MAX_WIDTH  = maxWidth
    val MAX_HEIGHT  = maxHeight

    val appLogoXSet by animateDpAsState(
      targetValue = when(formState) {
        AuthFormState.None -> 0.dp
        AuthFormState.SignIn -> (MAX_WIDTH.value * 0.3f).dp
        AuthFormState.ForgotPassword -> -(MAX_WIDTH.value * 0.3f).dp
        AuthFormState.ResetPassword -> -(MAX_WIDTH.value * 0.3f).dp
      },
      animationSpec = tween(1000)
    )
    val spacerSize1: Float = maxWidth.value * 0.026f // max width * 2.6%

    val circularLoadingSize: Float = maxWidth.value * 0.03f // max width * 3%
    val circularStrokeWidth: Float = maxWidth.value * 0.003f // max width * 0.3%

    val archShapeSize: Float = maxWidth.value * 0.244f // max width * 24.4%
    val softBurstShapeSize: Float = maxWidth.value * 0.206f // max width * 20.6%
    val gemShapeSize: Float = maxWidth.value * 0.356f // max width * 35.6%
    val verySunnyShapeSize: Float = maxWidth.value * 0.562f // max width * 56.2%

    val verySunnyShapeTargetX = when (formState) {
      AuthFormState.SignIn -> (verySunnyShapeSize / 2.85f).dp
      AuthFormState.ForgotPassword -> -(verySunnyShapeSize / 2.85f).dp
      AuthFormState.ResetPassword -> -(verySunnyShapeSize / 2.85f).dp
      else -> MAX_WIDTH
    }

    val verySunnyShapeOffsetX by animateDpAsState(
      targetValue = verySunnyShapeTargetX,
      animationSpec = tween(1000),
      label = "VerySunnyOffset"
    )

    val gemShapeTargetX = when (formState) {
      AuthFormState.SignIn -> (gemShapeSize / 2.8f).dp
      AuthFormState.ForgotPassword -> -(gemShapeSize / 2.8f).dp
      AuthFormState.ResetPassword -> -(gemShapeSize / 2.8f).dp
      else -> MAX_WIDTH
    }

    val gemShapeOffsetX by animateDpAsState(
      targetValue = gemShapeTargetX,
      animationSpec = tween(1000),
      label = "GemOffset"
    )

    // -------------------------(Sign In Form)-------------------------

    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .offset((MAX_WIDTH.value * -0.15f).dp)
    ) {
      AnimatedVisibility(
        visible = formState is AuthFormState.SignIn,
        enter = slideInHorizontally(tween(1000), initialOffsetX = { -it * 2 }),
        exit = slideOutHorizontally(tween(1000), targetOffsetX = { -it * 2 })
      ) {
        Box(
          modifier = Modifier
            .offset(x = (archShapeSize * -0.8f).dp, y = (archShapeSize * -0.7f).dp)
            .size(archShapeSize.dp)
            .clip(MaterialShapes.Arch.toShape())
            .background(Primary.copy(alpha = 0.25f))
        ) // Arch Shape (Box).
      }

      AnimatedVisibility(
        visible = formState is AuthFormState.SignIn,
        enter = slideInHorizontally(tween(1000), { -it * 2 }),
        exit = slideOutHorizontally(tween(1000), { -it * 2 })
      ) {
        SignInForm(
          authMode = authMode,
          viewModel = viewModel,
          maxWidthScreen = MAX_WIDTH,
          maxHeightScreen = MAX_HEIGHT,
          modifier = Modifier
            .width((MAX_WIDTH.value * 0.6f).dp)
            .height(IntrinsicSize.Min)
        )
      }

      AnimatedVisibility(
        visible = formState is AuthFormState.SignIn,
        enter = slideInHorizontally(tween(1000), { -it * 2 }),
        exit = slideOutHorizontally(tween(1000), { -it * 2 })
      ) {
        Box(
          modifier = Modifier
            .offset(x = (softBurstShapeSize * -0.8f).dp, y = (softBurstShapeSize * 1.2f).dp)
            .graphicsLayer{ rotationZ = softBurstShapeRotation}
            .size(softBurstShapeSize.dp)
            .clip(MaterialShapes.SoftBurst.toShape())
            .background(Primary.copy(alpha = 0.1f))
        ) // Soft Burst Shape (Box).
      }
    } // Sign In Form with Shapes (Box).

    // -------------------------(App Logo)-------------------------

    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .offset(x = appLogoXSet)
    ) {
      Box(
        modifier = Modifier
          .offset(x = gemShapeOffsetX, y = (gemShapeSize * -0.8f).dp)
          .size(gemShapeSize.dp)
          .clip(MaterialShapes.Gem.toShape())
          .background(Primary.copy(alpha = 0.1f))
      ) // Gem Shape (Box).
      AppLogoWithLoading(
        modifier = Modifier.border(0.dp, Color.Transparent),
        maxWidthScreen = MAX_WIDTH,
        uiState = uiState,
        gap = spacerSize1,
        appLogoScale = appLogoScale,
        loadingSize = circularLoadingSize,
        loadingStroke = circularStrokeWidth,
      ) // AppLogoWithLoading.
      Box(
        modifier = Modifier
          .offset(x = verySunnyShapeOffsetX, y = (verySunnyShapeSize / 2.1f).dp)
          .graphicsLayer {rotationZ = verySunnyShapeRotation}
          .size(verySunnyShapeSize.dp)
          .clip(MaterialShapes.VerySunny.toShape())
          .background(Primary.copy(alpha = 0.1f))
      )
    } // AppLogoWithLoading with Shapes (Box).

    // -------------------------(Forgot Password Form)-------------------------
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .offset(-(MAX_WIDTH.value * -0.2f).dp)
    ) {
      AnimatedVisibility(
        visible = formState is AuthFormState.ForgotPassword || formState is AuthFormState.ResetPassword,
        enter = slideInHorizontally(tween(1000), initialOffsetX = { it * 2 }),
        exit = slideOutHorizontally(tween(1000), targetOffsetX = { it * 2 })
      ) {
        Box(
          modifier = Modifier
            .offset(x = (archShapeSize * 0.6f).dp, y = (archShapeSize * -0.7f).dp)
            .size(archShapeSize.dp)
            .clip(MaterialShapes.Arch.toShape())
            .background(Primary.copy(alpha = 0.25f))
        ) // Arch Shape (Box).
      }

      AnimatedVisibility(
        visible = formState is AuthFormState.ForgotPassword,
        enter = slideInHorizontally(tween(1000), { it * 2 }),
        exit = slideOutHorizontally(tween(1000), { it * 2 })
      ) {
        FPForm(
          viewModel = viewModel,
          maxWidthScreen = MAX_WIDTH,
          maxHeightScreen = MAX_HEIGHT,
          modifier = Modifier
            .width((MAX_WIDTH.value * 0.6f).dp)
            .height(IntrinsicSize.Min)
        )
      }

      AnimatedVisibility(
        visible = formState is AuthFormState.ResetPassword,
        enter = slideInHorizontally(tween(1000), { it * 2 }),
        exit = slideOutHorizontally(tween(1000), { it * 2 })
      ) {
        RPForm(
          viewModel = viewModel,
          maxWidthScreen = MAX_WIDTH,
          maxHeightScreen = MAX_HEIGHT,
          modifier = Modifier
            .width((MAX_WIDTH.value * 0.6f).dp)
            .height(IntrinsicSize.Min)
        )
      }

      AnimatedVisibility(
        visible = formState is AuthFormState.ForgotPassword || formState is AuthFormState.ResetPassword,
        enter = slideInHorizontally(tween(1000), { it * 2 }),
        exit = slideOutHorizontally(tween(1000), { it * 2 })
      ) {
        Box(
          modifier = Modifier
            .offset(x = (softBurstShapeSize * 0.5f).dp, y = (softBurstShapeSize * 1.2f).dp)
            .graphicsLayer{ rotationZ = softBurstShapeRotation}
            .size(softBurstShapeSize.dp)
            .clip(MaterialShapes.SoftBurst.toShape())
            .background(Primary.copy(alpha = 0.1f))
        ) // Soft Burst Shape (Box).
      } // Sign In Form with Shapes (Box).
    } // Forgot Password Form (Box).
  }
}

