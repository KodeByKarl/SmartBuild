package com.example.smart_build.screens.maintenancelab

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smart_build.ui.RuleFeedbackDialog
import com.example.smart_build.ui.theme.White
import kotlin.math.roundToInt

private enum class DeskWindow { NONE, TICKET, PICTURES, CLEANUP, CMD, ACTION }

private data class MaintState(
  val ticketAccepted: Boolean = false,
  val wallpaperSet: Boolean = false,
  val junkInTrash: Int = 0,
  val diskCleanupDone: Boolean = false,
  val pingGw: Boolean = false,
  val pingDns: Boolean = false,
  val ticketClosed: Boolean = false,
  val wallpaperStyle: Int = 0, // 0 default blue, 1 sun, 2 night
) {
  val junkCleared: Boolean get() = junkInTrash >= 2
  val netDone: Boolean get() = pingGw && pingDns

  /** Soft-only Windows repair steps (no hardware tools on the OS desktop). */
  val doneCount: Int
    get() = listOf(
      ticketAccepted, wallpaperSet, junkCleared,
      diskCleanupDone, netDone, ticketClosed,
    ).count { it }

  fun nextHint(): String = when {
    !ticketAccepted -> "Open Ticket #SB-4401 (desktop) and Accept"
    !wallpaperSet -> "Start → Pictures — drag a wallpaper onto the desktop"
    !junkCleared -> "Drag Temp and OldReports into Recycle Bin"
    !diskCleanupDone -> "Start → Disk Cleanup → Run"
    !pingGw -> "CMD — type: ping 192.168.1.1  then Enter"
    !pingDns -> "CMD — type: ping 8.8.8.8  then Enter"
    !ticketClosed -> "Action Center — Close ticket"
    else -> "Desktop repaired — Finish"
  }
}

@Composable
fun MaintenanceLabScreen(
  navController: NavHostController,
  hints: Boolean = true,
  embedded: Boolean = false,
  startFaulted: Boolean = false,
  onStationComplete: (() -> Unit)? = null,
  onLeave: (() -> Unit)? = null,
) {
  var state by remember {
    mutableStateOf(
      if (startFaulted) MaintState(ticketAccepted = true) else MaintState(),
    )
  }
  var window by remember { mutableStateOf(if (startFaulted) DeskWindow.ACTION else DeskWindow.NONE) }
  var passed by remember { mutableStateOf(false) }
  var dialog by remember { mutableStateOf<Pair<Boolean, Pair<String, String>>?>(null) }
  var junkTempOut by remember { mutableStateOf(true) }
  var junkReportsOut by remember { mutableStateOf(true) }
  var guideVisible by remember { mutableStateOf(hints) }

  fun goBack() {
    when {
      window != DeskWindow.NONE -> window = DeskWindow.NONE
      onLeave != null -> onLeave()
      !navController.popBackStack() -> navController.navigateUp()
    }
  }

  BackHandler { goBack() }

  fun reset() {
    dialog = null
    passed = false
    window = DeskWindow.NONE
    state = MaintState()
    junkTempOut = true
    junkReportsOut = true
  }

  fun finishModule() {
    if (state.doneCount < 6 && !passed) {
      dialog = false to ("Not finished" to "Still needed: ${state.nextHint()}")
      return
    }
    passed = true
    if (onStationComplete != null) onStationComplete()
    else goBack()
  }

  if (dialog != null) {
    val (ok, msg) = dialog!!
    RuleFeedbackDialog(
      correct = ok,
      title = msg.first,
      body = msg.second,
      onDismiss = { dialog = null },
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
    WindowsDesktop(
      state = state,
      window = window,
      junkTempOut = junkTempOut,
      junkReportsOut = junkReportsOut,
      passed = passed,
      onOpen = { window = it },
      onCloseWindow = { window = DeskWindow.NONE },
      onAcceptTicket = {
        state = state.copy(ticketAccepted = true)
        dialog = true to (
          "Ticket open" to
            "Slow PC + no internet. Fix software on this Windows desktop (wallpaper, junk, cleanup, ping)."
          )
        window = DeskWindow.NONE
      },
      onSetWallpaper = { style ->
        state = state.copy(wallpaperSet = true, wallpaperStyle = style)
        dialog = true to ("Desktop personalized" to "Wallpaper applied by drag.")
        window = DeskWindow.NONE
      },
      onTrashJunk = { which ->
        if (which == "temp") junkTempOut = false
        if (which == "reports") junkReportsOut = false
        val n = state.junkInTrash + 1
        state = state.copy(junkInTrash = n)
        if (n >= 2) dialog = true to ("Recycle Bin" to "Junk sent to Recycle Bin.")
      },
      onDiskCleanup = {
        state = state.copy(diskCleanupDone = true)
        dialog = true to ("Disk Cleanup" to "Free space recovered.")
        window = DeskWindow.NONE
      },
      onPing = { which ->
        // Feedback stays inside CMD transcript (typed commands).
        state = if (which == "gw") state.copy(pingGw = true) else state.copy(pingDns = true)
      },
      onCloseTicket = {
        if (state.doneCount < 5) {
          dialog = false to (
            "Not ready" to
              "Finish wallpaper, Recycle Bin, Disk Cleanup, and ping before closing."
            )
          return@WindowsDesktop
        }
        state = state.copy(ticketClosed = true)
        passed = true
        dialog = true to (
          "Service complete" to
            "Action Center clear. Tap Finish module when ready."
          )
        window = DeskWindow.ACTION
      },
      onFinish = { finishModule() },
      onReset = { reset() },
    )

    if (guideVisible && hints) {
      Row(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
      ) {
        Column(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .border(1.dp, Color(0xFFE8B84A).copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .clickable { guideVisible = false }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
          Text(
            "DO THIS · ${state.nextHint()}",
            color = Color(0xFFE8B84A),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            "Tap guide to hide · ${state.doneCount}/6",
            color = White.copy(alpha = 0.75f),
            fontSize = 10.sp,
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        FloatChip("Exit") { goBack() }
      }
    } else {
      Row(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        if (hints) {
          FloatChip("Guide") { guideVisible = true }
        }
        FloatChip("${state.doneCount}/6") {}
        FloatChip("Exit") { goBack() }
      }
    }

    if (passed) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 48.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF1E8449))
          .clickable { finishModule() }
          .padding(horizontal = 20.dp, vertical = 12.dp),
      ) {
        Text("Finish module ▶", color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }
    }
  }
}

@Composable
private fun FloatChip(label: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color.Black.copy(alpha = 0.55f))
      .border(1.dp, White.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 8.dp),
  ) {
    Text(label, color = White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
private fun WindowsDesktop(
  state: MaintState,
  window: DeskWindow,
  junkTempOut: Boolean,
  junkReportsOut: Boolean,
  passed: Boolean,
  onOpen: (DeskWindow) -> Unit,
  onCloseWindow: () -> Unit,
  onAcceptTicket: () -> Unit,
  onSetWallpaper: (Int) -> Unit,
  onTrashJunk: (String) -> Unit,
  onDiskCleanup: () -> Unit,
  onPing: (String) -> Unit,
  onCloseTicket: () -> Unit,
  onFinish: () -> Unit,
  onReset: () -> Unit,
) {
  var startOpen by remember { mutableStateOf(false) }

  val wallpaperBrush = when (state.wallpaperStyle) {
    1 -> Brush.verticalGradient(listOf(Color(0xFF87CEEB), Color(0xFFF4D03F), Color(0xFF27AE60)))
    2 -> Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF0D47A1), Color(0xFF311B92)))
    else -> Brush.verticalGradient(listOf(Color(0xFF5DADE2), Color(0xFF3498DB)))
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(wallpaperBrush)
      .clickable(enabled = startOpen) { startOpen = false },
  ) {
    val deskW = constraints.maxWidth.toFloat()
    val deskH = constraints.maxHeight.toFloat()

    // Desktop icons — Windows-style (no hardware tools)
    Column(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(top = 56.dp, start = 10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      DeskIcon("🎫", "Ticket", highlight = !state.ticketAccepted) {
        startOpen = false
        onOpen(DeskWindow.TICKET)
      }
      DeskIcon("📁", "Pictures", highlight = state.ticketAccepted && !state.wallpaperSet) {
        startOpen = false
        onOpen(DeskWindow.PICTURES)
      }
      DeskIcon("💻", "CMD", highlight = state.diskCleanupDone && !state.netDone) {
        startOpen = false
        onOpen(DeskWindow.CMD)
      }
    }

    if (junkTempOut) {
      DraggableDeskItem(
        label = "Temp",
        emoji = "📄",
        startX = 0.42f,
        startY = 0.35f,
        deskW = deskW,
        deskH = deskH,
        dropTrashZone = true,
        onDroppedInTrash = { onTrashJunk("temp") },
      )
    }
    if (junkReportsOut) {
      DraggableDeskItem(
        label = "OldReports",
        emoji = "📄",
        startX = 0.55f,
        startY = 0.48f,
        deskW = deskW,
        deskH = deskH,
        dropTrashZone = true,
        onDroppedInTrash = { onTrashJunk("reports") },
      )
    }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 16.dp, bottom = 48.dp),
    ) {
      Text("🗑️", fontSize = 28.sp)
      Text(
        if (state.junkCleared) "Recycle Bin" else "Recycle Bin",
        color = White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
      )
    }

    when (window) {
      DeskWindow.TICKET -> WinFrame("Ticket #SB-4401", onCloseWindow) {
        Text("Slow PC + no internet", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
        Text(
          "Customer reports full disk, cluttered desktop, dead WAN.",
          fontSize = 12.sp,
          color = Color(0xFF444444),
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (!state.ticketAccepted) {
          ActionBtn("Accept ticket") { onAcceptTicket() }
        } else {
          Text("✓ Accepted", color = Color(0xFF1E8449), fontWeight = FontWeight.Bold)
        }
      }
      DeskWindow.PICTURES -> WinFrame("Pictures", onCloseWindow) {
        Text("Drag a wallpaper onto the desktop background", fontSize = 11.sp, color = Color(0xFF555555))
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          WallpaperThumb("wallpaper1", sun = true, onSetWallpaper)
          WallpaperThumb("wallpaper2", sun = false, onSetWallpaper)
        }
      }
      DeskWindow.CLEANUP -> WinFrame("Disk Cleanup", onCloseWindow) {
        Text("Temporary files · Recycle Bin · Thumbnails", fontSize = 12.sp, color = Color(0xFF444444))
        Spacer(modifier = Modifier.height(8.dp))
        if (!state.diskCleanupDone) {
          ActionBtn("Run Disk Cleanup") { onDiskCleanup() }
        } else {
          Text("✓ Cleanup complete", color = Color(0xFF1E8449), fontWeight = FontWeight.Bold)
        }
      }
      DeskWindow.CMD -> CmdWindow(
        state = state,
        onClose = onCloseWindow,
        onPing = onPing,
      )
      DeskWindow.ACTION -> WinFrame("Action Center", onCloseWindow) {
        val warns = listOf(
          (!state.wallpaperSet) to "Default wallpaper / not personalized",
          (!state.junkCleared || !state.diskCleanupDone) to "Low disk / junk files",
          (!state.netDone) to "No internet",
          (!state.ticketClosed) to "Open service ticket",
        )
        warns.forEach { (active, msg) ->
          Text(
            if (active) "⚠ $msg" else "✓ $msg",
            color = if (active) Color(0xFFC0392B) else Color(0xFF1E8449),
            fontSize = 12.sp,
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (!state.ticketClosed) {
          ActionBtn("Close service ticket") { onCloseTicket() }
        } else {
          Text("✓ All clear", color = Color(0xFF1E8449), fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          ActionBtn("Finish module ▶") { onFinish() }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          "Reset desktop",
          color = Color(0xFFC0392B),
          fontSize = 12.sp,
          modifier = Modifier.clickable { onReset() },
        )
      }
      DeskWindow.NONE -> Unit
    }

    // Start menu (above taskbar)
    if (startOpen) {
      StartMenu(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(start = 4.dp, bottom = 44.dp),
        state = state,
        onPick = { win ->
          startOpen = false
          onOpen(win)
        },
      )
    }

    // Taskbar
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .height(40.dp)
        .background(Color(0xFF1B4F72)),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .padding(start = 6.dp)
          .size(28.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(if (startOpen) Color(0xFF1A5276) else Color(0xFF2E86C1))
          .clickable { startOpen = !startOpen },
        contentAlignment = Alignment.Center,
      ) {
        Text("⊞", color = White, fontSize = 14.sp)
      }
      // Pinned taskbar apps
      TaskbarPin("💻", highlight = state.diskCleanupDone && !state.netDone) {
        startOpen = false
        onOpen(DeskWindow.CMD)
      }
      TaskbarPin("📁") {
        startOpen = false
        onOpen(DeskWindow.PICTURES)
      }
      Spacer(modifier = Modifier.weight(1f))
      Text(
        if (passed) "Finish ✓" else "Action Center (${6 - state.doneCount})",
        color = White,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
          .clickable {
            startOpen = false
            if (passed) onFinish() else onOpen(DeskWindow.ACTION)
          }
          .padding(horizontal = 10.dp),
      )
      Text("🔊", modifier = Modifier.padding(end = 8.dp))
    }
  }
}

@Composable
private fun StartMenu(
  modifier: Modifier,
  state: MaintState,
  onPick: (DeskWindow) -> Unit,
) {
  Column(
    modifier = modifier
      .width(220.dp)
      .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
      .background(Color(0xEE1C2833))
      .border(1.dp, Color(0xFF5D6D7E), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
      .clickable(enabled = false) {},
  ) {
    Text(
      "SmartBuild PC",
      color = White,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
    )
    StartItem("🎫", "Ticket #SB-4401", highlight = !state.ticketAccepted) {
      onPick(DeskWindow.TICKET)
    }
    StartItem("📁", "Pictures", highlight = state.ticketAccepted && !state.wallpaperSet) {
      onPick(DeskWindow.PICTURES)
    }
    StartItem("💽", "Disk Cleanup", highlight = state.junkCleared && !state.diskCleanupDone) {
      onPick(DeskWindow.CLEANUP)
    }
    StartItem("💻", "Command Prompt", highlight = state.diskCleanupDone && !state.netDone) {
      onPick(DeskWindow.CMD)
    }
    StartItem("🔔", "Action Center", highlight = state.netDone && !state.ticketClosed) {
      onPick(DeskWindow.ACTION)
    }
    Spacer(modifier = Modifier.height(6.dp))
  }
}

@Composable
private fun StartItem(emoji: String, label: String, highlight: Boolean = false, onClick: () -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .background(if (highlight) Color(0xFFE8B84A).copy(alpha = 0.25f) else Color.Transparent)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 10.dp),
  ) {
    Text(emoji, fontSize = 16.sp, modifier = Modifier.width(28.dp))
    Text(label, color = White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
  }
}

@Composable
private fun TaskbarPin(emoji: String, highlight: Boolean = false, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .padding(start = 4.dp)
      .size(28.dp)
      .clip(RoundedCornerShape(4.dp))
      .background(if (highlight) Color(0xFFE8B84A).copy(alpha = 0.35f) else Color.Transparent)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Text(emoji, fontSize = 14.sp)
  }
}

@Composable
private fun CmdWindow(
  state: MaintState,
  onClose: () -> Unit,
  onPing: (String) -> Unit,
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
  val focusRequester = remember { FocusRequester() }
  val keyboard = LocalSoftwareKeyboardController.current
  val mono = TextStyle(
    color = Color(0xFFCCCCCC),
    fontFamily = FontFamily.Monospace,
    fontSize = 11.sp,
  )

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboard?.show()
  }
  LaunchedEffect(lines.size) {
    scroll.animateScrollTo(scroll.maxValue)
  }

  fun runCommand(raw: String) {
    val typed = raw.trimEnd()
    val shown = if (typed.isEmpty()) "C:\\Users\\Tech>" else "C:\\Users\\Tech>$typed"
    val norm = typed.trim().lowercase().replace(Regex("\\s+"), " ")
    val out = mutableListOf(shown)
    when {
      norm.isEmpty() -> Unit
      !state.pingGw && (norm == "ping 192.168.1.1" || norm.startsWith("ping 192.168.1.1 ")) -> {
        out += listOf(
          "",
          "Pinging 192.168.1.1 with 32 bytes of data:",
          "Reply from 192.168.1.1: bytes=32 time<1ms TTL=64",
          "Reply from 192.168.1.1: bytes=32 time<1ms TTL=64",
          "Reply from 192.168.1.1: bytes=32 time=1ms TTL=64",
          "Reply from 192.168.1.1: bytes=32 time<1ms TTL=64",
          "",
          "Ping statistics for 192.168.1.1:",
          "    Packets: Sent = 4, Received = 4, Lost = 0 (0% loss),",
          "",
        )
        onPing("gw")
      }
      state.pingGw && !state.pingDns && (norm == "ping 8.8.8.8" || norm.startsWith("ping 8.8.8.8 ")) -> {
        out += listOf(
          "",
          "Pinging 8.8.8.8 with 32 bytes of data:",
          "Reply from 8.8.8.8: bytes=32 time=12ms TTL=117",
          "Reply from 8.8.8.8: bytes=32 time=11ms TTL=117",
          "Reply from 8.8.8.8: bytes=32 time=13ms TTL=117",
          "Reply from 8.8.8.8: bytes=32 time=12ms TTL=117",
          "",
          "Ping statistics for 8.8.8.8:",
          "    Packets: Sent = 4, Received = 4, Lost = 0 (0% loss),",
          "",
          "Path restored.",
          "",
        )
        onPing("dns")
      }
      norm.startsWith("ping ") -> {
        val host = norm.removePrefix("ping ").trim().ifEmpty { "host" }
        out += listOf(
          "Ping request could not find host $host. Please check the name and try again.",
          "",
        )
      }
      else -> {
        val token = typed.trim().ifEmpty { typed }.substringBefore(' ')
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

  Column(
    modifier = Modifier
      .padding(start = 36.dp, top = 64.dp, end = 12.dp)
      .widthIn(max = 380.dp)
      .fillMaxWidth()
      .clip(RoundedCornerShape(4.dp))
      .background(Color(0xFF0C0C0C))
      .border(1.dp, Color(0xFF555555), RoundedCornerShape(4.dp))
      .clickable {
        focusRequester.requestFocus()
        keyboard?.show()
      },
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF1F1F1F))
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text("⊞", color = White, fontSize = 11.sp)
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        "Administrator: Command Prompt",
        color = White,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.weight(1f),
      )
      Text(
        "✕",
        color = White,
        fontSize = 14.sp,
        modifier = Modifier.clickable(onClick = onClose).padding(4.dp),
      )
    }
    Column(
      modifier = Modifier
        .heightIn(min = 200.dp, max = 320.dp)
        .verticalScroll(scroll)
        .padding(10.dp),
    ) {
      lines.forEach { line ->
        Text(line, style = mono)
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          "C:\\Users\\Tech>",
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          color = White,
        )
        BasicTextField(
          value = input,
          onValueChange = { v ->
            // Keep single-line CMD input
            if (!v.contains('\n')) input = v
            else runCommand(v.substringBefore('\n'))
          },
          modifier = Modifier
            .weight(1f)
            .focusRequester(focusRequester),
          textStyle = TextStyle(
            color = White,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
          ),
          cursorBrush = SolidColor(White),
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { runCommand(input) }),
        )
      }
    }
  }
}

@Composable
private fun DeskIcon(emoji: String, label: String, highlight: Boolean = false, onClick: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(64.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(if (highlight) Color.White.copy(alpha = 0.25f) else Color.Transparent)
      .border(
        if (highlight) 1.dp else 0.dp,
        if (highlight) Color(0xFFE8B84A) else Color.Transparent,
        RoundedCornerShape(6.dp),
      )
      .clickable(onClick = onClick)
      .padding(4.dp),
  ) {
    Text(emoji, fontSize = 26.sp)
    Text(label, color = White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
  }
}

@Composable
private fun DraggableDeskItem(
  label: String,
  emoji: String,
  startX: Float,
  startY: Float,
  deskW: Float,
  deskH: Float,
  dropTrashZone: Boolean,
  onDroppedInTrash: () -> Unit,
) {
  var ox by remember { mutableFloatStateOf(startX * deskW) }
  var oy by remember { mutableFloatStateOf(startY * deskH) }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .offset { IntOffset(ox.roundToInt(), oy.roundToInt()) }
      .pointerInput(deskW, deskH) {
        detectDragGestures(
          onDragEnd = {
            val inTrash = dropTrashZone &&
              ox > deskW * 0.72f && oy > deskH * 0.55f
            if (inTrash) onDroppedInTrash()
          },
          onDrag = { change, amount ->
            change.consume()
            ox = (ox + amount.x).coerceIn(0f, deskW - 80f)
            oy = (oy + amount.y).coerceIn(0f, deskH - 80f)
          },
        )
      }
      .padding(4.dp),
  ) {
    Text(emoji, fontSize = 24.sp)
    Text(label, color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun WallpaperThumb(
  name: String,
  sun: Boolean,
  onSetWallpaper: (Int) -> Unit,
) {
  var dragX by remember { mutableFloatStateOf(0f) }
  var dragY by remember { mutableFloatStateOf(0f) }

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier
        .size(72.dp, 48.dp)
        .offset { IntOffset(dragX.roundToInt(), dragY.roundToInt()) }
        .clip(RoundedCornerShape(4.dp))
        .background(
          if (sun) Brush.verticalGradient(listOf(Color(0xFF85C1E9), Color(0xFFF9E79F)))
          else Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF5B2C6F))),
        )
        .border(1.dp, Color(0xFF888888), RoundedCornerShape(4.dp))
        .pointerInput(Unit) {
          detectDragGestures(
            onDragEnd = {
              if (dragY > 40f || dragX * dragX + dragY * dragY > 2500f) {
                onSetWallpaper(if (sun) 1 else 2)
              }
              dragX = 0f
              dragY = 0f
            },
            onDrag = { change, amount ->
              change.consume()
              dragX += amount.x
              dragY += amount.y
            },
          )
        },
      contentAlignment = Alignment.Center,
    ) {
      Text(if (sun) "☀️" else "🌙", fontSize = 18.sp)
    }
    Text(name, fontSize = 10.sp, color = Color.Black)
    Text("drag to desktop", fontSize = 9.sp, color = Color(0xFF666666))
  }
}

@Composable
private fun WinFrame(title: String, onClose: () -> Unit, content: @Composable () -> Unit) {
  Column(
    modifier = Modifier
      .padding(start = 72.dp, top = 56.dp, end = 16.dp)
      .widthIn(max = 340.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(Color(0xFFF4F6F7))
      .border(1.dp, Color(0xFF7F8C8D), RoundedCornerShape(6.dp)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF2874A6))
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(title, color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
      Text(
        "✕",
        color = White,
        fontSize = 14.sp,
        modifier = Modifier.clickable(onClick = onClose).padding(4.dp),
      )
    }
    Column(modifier = Modifier.padding(10.dp), content = { content() })
  }
}

@Composable
private fun ActionBtn(label: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(Color(0xFF2E86C1))
      .clickable(onClick = onClick)
      .padding(10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(label, color = White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
  }
}
