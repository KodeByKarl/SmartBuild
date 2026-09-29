package com.example.smart_build.screens.serverlab

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.White

internal enum class LanDeskPage { HOME, IP, CMD }

@Composable
internal fun LanDeviceDesk(
  hostname: String,
  isPc: Boolean,
  configured: Boolean,
  currentIp: String,
  currentMask: String,
  canPing: Boolean,
  page: LanDeskPage,
  onPage: (LanDeskPage) -> Unit,
  onApply: (String, String) -> Unit,
  onPingOk: () -> Unit,
  onClose: () -> Unit,
) {
  val wallpaper = if (isPc) {
    Brush.verticalGradient(listOf(Color(0xFF1A365D), Color(0xFF2E86C1)))
  } else {
    Brush.verticalGradient(listOf(Color(0xFF1A5276), Color(0xFF148F77)))
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(wallpaper)
      .padding(top = 108.dp),
  ) {
    Column(
      modifier = Modifier.padding(start = 12.dp, top = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      DeskTile("IP", "IP Configuration", page == LanDeskPage.IP) { onPage(LanDeskPage.IP) }
      if (isPc) {
        DeskTile("CMD", "Command Prompt", page == LanDeskPage.CMD) { onPage(LanDeskPage.CMD) }
      }
      Text(
        if (configured) currentIp else "No IP address",
        color = White,
        fontSize = 11.sp,
        modifier = Modifier.padding(start = 4.dp),
      )
    }

    if (page == LanDeskPage.IP) {
      IpWindow(
        hostname = hostname,
        currentIp = currentIp,
        currentMask = currentMask,
        configured = configured,
        onApply = onApply,
        onClose = { onPage(LanDeskPage.HOME) },
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(start = 92.dp, top = 8.dp, end = 12.dp),
      )
    }
    if (page == LanDeskPage.CMD && isPc) {
      PingWindow(
        currentIp = currentIp,
        currentMask = currentMask,
        configured = configured,
        canPing = canPing,
        onPingOk = onPingOk,
        onClose = { onPage(LanDeskPage.HOME) },
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(start = 92.dp, top = 8.dp, end = 12.dp),
      )
    }

    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .height(40.dp)
        .background(Color(0xFF1B4F72)),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        "Network",
        color = White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .padding(start = 8.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color(0xFF2E86C1))
          .clickable(onClick = onClose)
          .padding(horizontal = 10.dp, vertical = 6.dp),
      )
      Spacer(Modifier.weight(1f))
      Text(
        hostname,
        color = White.copy(alpha = 0.9f),
        fontSize = 12.sp,
        modifier = Modifier.padding(end = 12.dp),
      )
    }
  }
}

@Composable
private fun DeskTile(mark: String, label: String, active: Boolean, onClick: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(78.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(if (active) Color.White.copy(alpha = 0.28f) else Color.Transparent)
      .border(
        1.dp,
        if (active) Color(0xFFE8B84A) else Color.Transparent,
        RoundedCornerShape(6.dp),
      )
      .clickable(onClick = onClick)
      .padding(vertical = 6.dp),
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(Color(0xFF1B2631)),
      contentAlignment = Alignment.Center,
    ) {
      Text(mark, color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
    Text(
      label,
      color = White,
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 4.dp),
    )
  }
}

@Composable
private fun IpWindow(
  hostname: String,
  currentIp: String,
  currentMask: String,
  configured: Boolean,
  onApply: (String, String) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier,
) {
  var ip by remember(hostname) { mutableStateOf(currentIp) }
  var mask by remember(hostname) { mutableStateOf(currentMask) }
  LaunchedEffect(currentIp, currentMask) {
    if (configured) {
      ip = currentIp
      mask = currentMask
    }
  }

  DeskFrame("IP Configuration", onClose, modifier) {
    Text(
      "Static address for $hostname",
      color = Color(0xFF1C2833),
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(8.dp))
    IpField("IP Address", ip) { ip = it }
    Spacer(Modifier.height(6.dp))
    IpField("Subnet Mask", mask) { mask = it }
    Spacer(Modifier.height(10.dp))
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .background(Color(0xFF1A5276))
        .clickable { onApply(ip, mask) }
        .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
      Text("OK", color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
  }
}

@Composable
private fun IpField(label: String, value: String, onChange: (String) -> Unit) {
  Text(label, color = Color(0xFF333333), fontSize = 11.sp)
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 2.dp)
      .clip(RoundedCornerShape(3.dp))
      .background(White)
      .border(1.dp, Color(0xFFB0B0B0), RoundedCornerShape(3.dp))
      .padding(horizontal = 8.dp, vertical = 8.dp),
  ) {
    BasicTextField(
      value = value,
      onValueChange = { raw -> onChange(raw.filter { it.isDigit() || it == '.' }.take(15)) },
      textStyle = TextStyle(
        color = Color(0xFF1C2833),
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
      ),
      cursorBrush = SolidColor(Color(0xFF1C2833)),
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
  }
}

@Composable
private fun PingWindow(
  currentIp: String,
  currentMask: String,
  configured: Boolean,
  canPing: Boolean,
  onPingOk: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier,
) {
  val banner = remember {
    listOf(
      "Microsoft Windows [Version 10.0.19045]",
      "(c) Microsoft Corporation. All rights reserved.",
      "",
    )
  }
  var lines by remember { mutableStateOf(banner) }
  var input by remember { mutableStateOf("") }
  val scroll = rememberScrollState()
  val focus = remember { FocusRequester() }
  val keyboard = LocalSoftwareKeyboardController.current
  val mono = TextStyle(color = Color(0xFFCCCCCC), fontFamily = FontFamily.Monospace, fontSize = 11.sp)

  LaunchedEffect(Unit) {
    focus.requestFocus()
    keyboard?.show()
  }
  LaunchedEffect(lines.size) {
    scroll.animateScrollTo(scroll.maxValue)
  }

  fun run(raw: String) {
    val typed = raw.trim()
    val norm = typed.lowercase().replace(Regex("\\s+"), " ")
    val out = mutableListOf("C:\\Users\\Tech>$typed")
    when {
      norm.isEmpty() -> Unit
      norm == "ipconfig" -> {
        out += if (configured) {
          listOf(
            "",
            "Ethernet adapter Local Area Connection:",
            "",
            "   IPv4 Address. . . . . . . . . . . : $currentIp",
            "   Subnet Mask . . . . . . . . . . . : $currentMask",
            "",
          )
        } else {
          listOf(
            "",
            "Ethernet adapter Local Area Connection:",
            "",
            "   Media State . . . . . . . . . . . : Media disconnected",
            "   Set an IP address first.",
            "",
          )
        }
      }
      norm == "ping 192.168.10.10" || norm.startsWith("ping 192.168.10.10 ") -> {
        if (canPing) {
          out += listOf(
            "",
            "Pinging 192.168.10.10 with 32 bytes of data:",
            "Reply from 192.168.10.10: bytes=32 time<1ms TTL=128",
            "Reply from 192.168.10.10: bytes=32 time<1ms TTL=128",
            "Reply from 192.168.10.10: bytes=32 time=1ms TTL=128",
            "Reply from 192.168.10.10: bytes=32 time<1ms TTL=128",
            "",
            "Ping statistics for 192.168.10.10:",
            "    Packets: Sent = 4, Received = 4, Lost = 0 (0% loss),",
            "",
          )
          onPingOk()
        } else {
          out += listOf(
            "",
            "Pinging 192.168.10.10 with 32 bytes of data:",
            "Request timed out.",
            "Request timed out.",
            "Request timed out.",
            "Request timed out.",
            "",
            "Ping statistics for 192.168.10.10:",
            "    Packets: Sent = 4, Received = 0, Lost = 4 (100% loss),",
            "",
            "Set the IP on PC0 and Server0, then ping again.",
            "",
          )
        }
      }
      norm.startsWith("ping ") -> {
        val host = norm.removePrefix("ping ").substringBefore(' ').ifEmpty { "host" }
        out += listOf(
          "",
          "Pinging $host with 32 bytes of data:",
          "Request timed out.",
          "Request timed out.",
          "Request timed out.",
          "Request timed out.",
          "",
          "Ping statistics for $host:",
          "    Packets: Sent = 4, Received = 0, Lost = 4 (100% loss),",
          "",
        )
      }
      else -> {
        val token = typed.substringBefore(' ').ifEmpty { typed }
        out += listOf(
          "'$token' is not recognized as an internal or external command,",
          "operable program or batch file.",
          "",
        )
      }
    }
    lines = lines + out
    input = ""
  }

  DeskFrame("Command Prompt", onClose, modifier) {
    Column(
      modifier = Modifier
        .heightIn(min = 180.dp, max = 280.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(2.dp))
        .background(Color(0xFF0C0C0C))
        .verticalScroll(scroll)
        .clickable {
          focus.requestFocus()
          keyboard?.show()
        }
        .padding(8.dp),
    ) {
      lines.forEach { line -> Text(line, style = mono) }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("C:\\Users\\Tech>", style = mono.copy(color = White))
        BasicTextField(
          value = input,
          onValueChange = { v ->
            if (!v.contains('\n')) input = v
            else run(v.substringBefore('\n'))
          },
          modifier = Modifier.weight(1f).focusRequester(focus),
          textStyle = mono.copy(color = White),
          cursorBrush = SolidColor(White),
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { run(input) }),
        )
      }
    }
  }
}

@Composable
private fun DeskFrame(
  title: String,
  onClose: () -> Unit,
  modifier: Modifier,
  content: @Composable () -> Unit,
) {
  Column(
    modifier = modifier
      .widthIn(max = 360.dp)
      .fillMaxWidth()
      .clip(RoundedCornerShape(4.dp))
      .background(Color(0xFFF4F6F7))
      .border(1.dp, Color(0xFF5D6D7E), RoundedCornerShape(4.dp)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF1B4F72))
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        title,
        color = White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.weight(1f),
      )
      Text(
        "X",
        color = White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable(onClick = onClose).padding(4.dp),
      )
    }
    Column(modifier = Modifier.padding(12.dp)) { content() }
  }
}
