package com.example.smart_build.components

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.Typography
import com.example.smart_build.ui.theme.White

/** Shared auth form field styling for dark login screens. */
object AuthFieldStyles {
  val textStyle: TextStyle
    get() = Typography.bodyLarge.copy(color = White)

  @Composable
  fun colors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = White,
    unfocusedTextColor = White,
    disabledTextColor = White.copy(alpha = 0.5f),
    focusedLabelColor = White,
    unfocusedLabelColor = White.copy(alpha = 0.7f),
    disabledLabelColor = White.copy(alpha = 0.5f),
    focusedBorderColor = Primary,
    unfocusedBorderColor = White.copy(alpha = 0.5f),
    cursorColor = Primary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
  )
}

@Composable
fun CustomColorOTF(
  label: () -> Unit,
  value: String,
  onValueChange: (String) -> Unit,
  isPW: Boolean = false
) {
  var isPWVisible by remember { mutableStateOf(false) }

  OutlinedTextField(
//    label = { Text(label, color = White.copy(alpha = 0.5f)) },
    label = { label },
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    textStyle = AuthFieldStyles.textStyle,
    modifier = Modifier.width(484.dp),
    visualTransformation = if(isPW && isPWVisible) {
      PasswordVisualTransformation()
    } else {
      VisualTransformation.None
    },
    trailingIcon = {
      if(isPW) {
        val icon = if(isPWVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
        val desc = if(isPWVisible) "Hide Password" else "Show Password"

        IconButton(onClick = { isPWVisible = !isPWVisible }) {
          Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = White.copy(alpha = 0.7f)
          )
        }
      } else { null }
    },
    colors = AuthFieldStyles.colors()
  )
}