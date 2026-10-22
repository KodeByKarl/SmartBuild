package com.example.smart_build.screens.crimplab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.GSFlex
import com.example.smart_build.ui.theme.Primary
import com.example.smart_build.ui.theme.White

/** One item in the technician's toolbox. */
data class CrimpTool(
  val id: String,
  val name: String,
  val use: String,
  /** True when the cable job really needs it; false items are distractors. */
  val needed: Boolean,
)

val CRIMP_TOOLBOX = listOf(
  CrimpTool("utp", "Cat 6 UTP cable", "The Ethernet cable that becomes the patch cord.", true),
  CrimpTool("rj45", "RJ45 connectors (2)", "One plug for each end of the cable.", true),
  CrimpTool("stripper", "Cable stripper", "Peels about 25 mm of jacket without nicking the pairs.", true),
  CrimpTool("crimper", "RJ45 crimping tool", "Presses the contacts into the conductors and locks the plug.", true),
  CrimpTool("tester", "Cable tester", "Lights LEDs 1–8 to prove the wire map after crimping.", true),
  CrimpTool("screwdriver", "Screwdriver set", "For case screws and brackets, not for cabling.", false),
  CrimpTool("solder", "Soldering iron", "RJ45 plugs are crimped, never soldered.", false),
  CrimpTool("hammer", "Hammer", "Never used on network cable or connectors.", false),
)

/**
 * First step of each cable station: pick the tools this job needs from the
 * toolbox. The straight-through and crossover stations each open with it and
 * state which wiring standard the cable will use.
 */
@Composable
fun CrimpToolPicker(
  mode: CableMode,
  selected: Set<String>,
  onToggle: (String) -> Unit,
  onConfirm: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val standard = if (mode == CableMode.STRAIGHT) {
    "Straight-through · T568B on both ends · joins UNLIKE devices (PC ↔ Switch)"
  } else {
    "Crossover · T568A on one end, T568B on the other · joins LIKE devices (Switch ↔ Switch)"
  }
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(8.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text(
      "Toolbox — tap every tool this cable needs",
      color = Color(0xFFE8B84A),
      fontFamily = GSFlex,
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp,
    )
    Text(standard, color = White, fontFamily = GSFlex, fontSize = 12.sp)
    Spacer(Modifier.height(2.dp))
    CRIMP_TOOLBOX.forEach { tool ->
      val on = tool.id in selected
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(if (on) Primary.copy(alpha = 0.28f) else Color(0xFF0A3A4A))
          .border(1.dp, if (on) Primary else White.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
          .clickable { onToggle(tool.id) }
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (on) Color(0xFF1E8449) else Color(0xFF1A2F38))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Text(if (on) "✓" else "+", color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Column(modifier = Modifier.padding(start = 10.dp)) {
          Text(tool.name, color = White, fontFamily = GSFlex, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Text(tool.use, color = White.copy(alpha = 0.7f), fontFamily = GSFlex, fontSize = 11.sp)
        }
      }
    }
    Box(
      modifier = Modifier
        .padding(top = 4.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0xFF1E8449))
        .clickable(onClick = onConfirm)
        .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
      Text("Take these tools to the bench ▶", color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
  }
}

/** Null when the pick is right; otherwise what the learner got wrong. */
fun toolPickProblem(selected: Set<String>): String? {
  val missing = CRIMP_TOOLBOX.filter { it.needed && it.id !in selected }
  val extra = CRIMP_TOOLBOX.filter { !it.needed && it.id in selected }
  if (missing.isEmpty() && extra.isEmpty()) return null
  return buildString {
    if (missing.isNotEmpty()) {
      append("Still missing: ")
      append(missing.joinToString(", ") { it.name })
      append(".")
    }
    if (extra.isNotEmpty()) {
      if (isNotEmpty()) append("\n")
      append("Not needed for crimping: ")
      append(extra.joinToString(", ") { it.name })
      append(". Put ")
      append(if (extra.size == 1) "it" else "them")
      append(" back.")
    }
  }
}
