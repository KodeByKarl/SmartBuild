package com.example.smart_build.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.White


// TODO: Copy the behavior and some style of OutlinedTextField for full control.
// TODO: Add Support Text that will appear conditionally, can have animation, move.
@Composable
fun CSOutlinedTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  singleLine: Boolean = true,
  isError: Boolean = false,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  leadingIcon: (@Composable (() -> Unit))? = null,
  trailingIcon: (@Composable (() -> Unit))? = null,
) {

  val interactionSource = remember { MutableInteractionSource() }
  val focused by interactionSource.collectIsFocusedAsState()

  val borderColor by animateColorAsState(
    when {
      isError -> MaterialTheme.colorScheme.error
      focused -> MaterialTheme.colorScheme.primary
      else -> MaterialTheme.colorScheme.outline
    },
    label = ""
  )

  val labelColor by animateColorAsState(
    when {
      isError -> MaterialTheme.colorScheme.error
      focused -> White
      else -> White.copy(alpha = 0.7f)
    },
    label = ""
  )

  Column(modifier) {

    if (label.isNotEmpty()) {
      Text(
        text = label,
        color = labelColor,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
      )
    }

    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      enabled = enabled,
      singleLine = singleLine,
      interactionSource = interactionSource,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      visualTransformation = visualTransformation,
      textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = White
      ),
      modifier = Modifier
        .fillMaxWidth(),
      decorationBox = { innerTextField ->

        Row(
          modifier = Modifier
            .border(
              width = 1.dp,
              color = borderColor,
              shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {

          leadingIcon?.invoke()

          Box(
            modifier = Modifier.weight(1f)
          ) {

            if (value.isEmpty()) {
              Text(
                "Enter $label",
                color = White.copy(alpha = 0.5f)
              )
            }

            innerTextField()
          }

          trailingIcon?.invoke()
        }
      }
    )
  }
}