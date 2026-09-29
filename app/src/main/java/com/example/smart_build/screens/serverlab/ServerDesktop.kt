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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_build.ui.theme.White

@Composable
internal fun ServerDesktop(
  cfg: ServerConfig,
  hints: Boolean,
  onServer: Boolean,
  window: SrvWindow,
  startOpen: Boolean,
  onStartOpen: (Boolean) -> Unit,
  onWindow: (SrvWindow) -> Unit,
  onToggleMachine: () -> Unit,
  onInstallRole: (Boolean, Boolean, Boolean) -> Unit,
  onCreateFolder: (String) -> Unit,
  onCreateGroup: (String) -> Unit,
  onGrant: (String, String) -> Unit,
  onApplyShare: (Boolean, Boolean, Boolean) -> Unit,
  onOpenPath: (String) -> Unit,
) {
  val wallpaper = if (onServer) {
    Brush.verticalGradient(listOf(Color(0xFF1A5276), Color(0xFF148F77)))
  } else {
    Brush.verticalGradient(listOf(Color(0xFF1A365D), Color(0xFF2E86C1)))
  }

  fun highlighted(target: SrvWindow): Boolean {
    if (!hints) return false
    return when {
      !cfg.fileServerRole -> target == SrvWindow.ROLES
      !cfg.folderHr || !cfg.folderIt -> target == SrvWindow.EXPLORER
      !cfg.groupHr || !cfg.groupIt -> target == SrvWindow.GROUPS
      !cfg.leastPrivilege -> target == SrvWindow.SECURITY
      !cfg.shareOn || !cfg.shareChangeBoth -> target == SrvWindow.SHARING
      !onServer && (!cfg.allowOk || !cfg.denyOk) -> target == SrvWindow.PC_FILES
      else -> false
    }
  }

  Box(modifier = Modifier.fillMaxSize().background(wallpaper)) {
    Column(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(top = 78.dp, start = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (onServer) {
        DeskIcon("SM", "Server Manager", highlighted(SrvWindow.ROLES)) { onWindow(SrvWindow.ROLES) }
        DeskIcon("FE", "File Explorer", highlighted(SrvWindow.EXPLORER)) { onWindow(SrvWindow.EXPLORER) }
        DeskIcon("GR", "Groups", highlighted(SrvWindow.GROUPS)) { onWindow(SrvWindow.GROUPS) }
        DeskIcon("SC", "Security", highlighted(SrvWindow.SECURITY)) { onWindow(SrvWindow.SECURITY) }
        DeskIcon("SH", "Sharing", highlighted(SrvWindow.SHARING)) { onWindow(SrvWindow.SHARING) }
      } else {
        DeskIcon("FE", "File Explorer", highlighted(SrvWindow.PC_FILES)) { onWindow(SrvWindow.PC_FILES) }
      }
    }

    if (window != SrvWindow.NONE) {
      WinFrame(
        title = when (window) {
          SrvWindow.ROLES -> "Server Manager"
          SrvWindow.EXPLORER -> "File Explorer"
          SrvWindow.GROUPS -> "Computer Management"
          SrvWindow.SECURITY -> "Folder Security"
          SrvWindow.SHARING -> "Advanced Sharing"
          SrvWindow.PC_FILES -> "File Explorer"
          SrvWindow.NONE -> ""
        },
        onClose = { onWindow(SrvWindow.NONE) },
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(start = 86.dp, top = 72.dp, end = 12.dp),
      ) {
        when (window) {
          SrvWindow.ROLES -> RolesWindow(cfg.fileServerRole, onInstallRole)
          SrvWindow.EXPLORER -> ExplorerWindow(cfg, onCreateFolder)
          SrvWindow.GROUPS -> GroupsWindow(cfg, onCreateGroup)
          SrvWindow.SECURITY -> SecurityWindow(cfg, onGrant)
          SrvWindow.SHARING -> SharingWindow(cfg, onApplyShare)
          SrvWindow.PC_FILES -> PcFilesWindow(onOpenPath)
          SrvWindow.NONE -> Unit
        }
      }
    }

    if (startOpen) {
      StartMenu(
        onServer = onServer,
        highlight = { highlighted(it) },
        modifier = Modifier.align(Alignment.BottomStart).padding(start = 4.dp, bottom = 44.dp),
        onPick = {
          onStartOpen(false)
          onWindow(it)
        },
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
        "Start",
        color = White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .padding(start = 6.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(if (startOpen) Color(0xFF1A5276) else Color(0xFF2E86C1))
          .clickable { onStartOpen(!startOpen) }
          .padding(horizontal = 10.dp, vertical = 6.dp),
      )
      Spacer(Modifier.width(8.dp))
      Text(
        if (onServer) "Switch to PC" else "Switch to Server",
        color = White,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable { onToggleMachine() }.padding(horizontal = 6.dp, vertical = 6.dp),
      )
      Spacer(Modifier.weight(1f))
      Text(
        if (onServer) "FILESERVER" else "PC0",
        color = White.copy(alpha = 0.85f),
        fontSize = 11.sp,
        modifier = Modifier.padding(end = 10.dp),
      )
    }
  }
}

@Composable
private fun RolesWindow(installed: Boolean, onInstall: (Boolean, Boolean, Boolean) -> Unit) {
  var page by remember { mutableStateOf(if (installed) 2 else 0) }
  var file by remember { mutableStateOf(installed) }
  var dns by remember { mutableStateOf(false) }
  var dhcp by remember { mutableStateOf(false) }
  when (page) {
    0 -> {
      WinText("Add Roles and Features")
      WinText("This wizard installs a role on FILESERVER.")
      Spacer(Modifier.height(8.dp))
      WinButton("Next") { page = 1 }
    }
    1 -> {
      WinText("Select server roles")
      RoleCheck("File Server", file) { file = !file }
      RoleCheck("DNS Server", dns) { dns = !dns }
      RoleCheck("DHCP Server", dhcp) { dhcp = !dhcp }
      Spacer(Modifier.height(8.dp))
      WinButton("Install") { onInstall(file, dns, dhcp) }
    }
    else -> WinText("File Server is installed on this server.")
  }
}

@Composable
private fun ExplorerWindow(cfg: ServerConfig, onCreate: (String) -> Unit) {
  var naming by remember { mutableStateOf(false) }
  var draft by remember { mutableStateOf("") }
  WinText("This PC  >  Local Disk (C:)  >  DeptShares")
  if (cfg.folderHr) FolderLine("HR")
  if (cfg.folderIt) FolderLine("IT")
  if (!cfg.folderHr && !cfg.folderIt) WinText("This folder is empty.")
  Spacer(Modifier.height(8.dp))
  if (!naming) {
    WinButton("New folder") {
      naming = true
      draft = ""
    }
  } else {
    WinText("Type the folder name, then Create.")
    WinField(draft, { draft = it }, "HR or IT") {
      onCreate(draft.trim())
      draft = ""
      naming = false
    }
    Spacer(Modifier.height(6.dp))
    WinButton("Create") {
      onCreate(draft.trim())
      draft = ""
      naming = false
    }
  }
}

@Composable
private fun GroupsWindow(cfg: ServerConfig, onCreate: (String) -> Unit) {
  var draft by remember { mutableStateOf("") }
  WinText("Local Users and Groups  >  Groups")
  if (cfg.groupHr) FolderLine("HR_Users")
  if (cfg.groupIt) FolderLine("IT_Users")
  Spacer(Modifier.height(8.dp))
  WinText("New group name")
  WinField(draft, { draft = it }, "HR_Users") { onCreate(draft.trim()); draft = "" }
  Spacer(Modifier.height(6.dp))
  WinButton("Create") {
    onCreate(draft.trim())
    draft = ""
  }
}

@Composable
private fun SecurityWindow(cfg: ServerConfig, onGrant: (String, String) -> Unit) {
  val ready = cfg.folderHr && cfg.folderIt && cfg.groupHr && cfg.groupIt
  if (!ready) {
    WinText("Create the HR and IT folders and both groups before you edit security.")
    return
  }
  AclBlock("HR", cfg.hrAclGroup) { onGrant("HR", it) }
  Spacer(Modifier.height(8.dp))
  AclBlock("IT", cfg.itAclGroup) { onGrant("IT", it) }
}

@Composable
private fun SharingWindow(cfg: ServerConfig, onApply: (Boolean, Boolean, Boolean) -> Unit) {
  var share by remember { mutableStateOf(cfg.shareOn) }
  var hr by remember { mutableStateOf(cfg.shareChangeBoth) }
  var it by remember { mutableStateOf(cfg.shareChangeBoth) }
  WinText("DeptShares  >  Properties  >  Sharing")
  RoleCheck("Share this folder as DeptShares", share) { share = !share }
  Spacer(Modifier.height(6.dp))
  WinText("Share permissions: Change")
  RoleCheck("HR_Users", hr) { hr = !hr }
  RoleCheck("IT_Users", it) { it = !it }
  Spacer(Modifier.height(8.dp))
  WinButton("Apply") { onApply(share, hr, it) }
}

@Composable
private fun PcFilesWindow(onOpen: (String) -> Unit) {
  var path by remember { mutableStateOf("") }
  WinText("Signed in as HR_Users. Type a path and press Go.")
  WinField(path, { path = it }, "\\\\FileServer\\DeptShares\\HR") { onOpen(path) }
  Spacer(Modifier.height(6.dp))
  WinButton("Go") { onOpen(path) }
}

@Composable
private fun AclBlock(folder: String, selected: String?, onPick: (String) -> Unit) {
  WinText("$folder  -  Security  -  Grant Modify")
  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
    listOf("HR_Users", "IT_Users").forEach { group ->
      val on = selected == group
      Text(
        group,
        color = if (on) White else Color(0xFF1C2833),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(if (on) Color(0xFF1E8449) else Color(0xFFE5E8E8))
          .clickable { onPick(group) }
          .padding(horizontal = 8.dp, vertical = 8.dp),
      )
    }
  }
}

@Composable
private fun StartMenu(
  onServer: Boolean,
  highlight: (SrvWindow) -> Boolean,
  modifier: Modifier,
  onPick: (SrvWindow) -> Unit,
) {
  Column(
    modifier = modifier
      .width(210.dp)
      .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
      .background(Color(0xEE1C2833))
      .border(1.dp, Color(0xFF5D6D7E), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
  ) {
    Text(
      if (onServer) "FILESERVER" else "PC0",
      color = White,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    )
    if (onServer) {
      StartItem("Server Manager", highlight(SrvWindow.ROLES)) { onPick(SrvWindow.ROLES) }
      StartItem("File Explorer", highlight(SrvWindow.EXPLORER)) { onPick(SrvWindow.EXPLORER) }
      StartItem("Computer Management", highlight(SrvWindow.GROUPS)) { onPick(SrvWindow.GROUPS) }
      StartItem("Folder Security", highlight(SrvWindow.SECURITY)) { onPick(SrvWindow.SECURITY) }
      StartItem("Advanced Sharing", highlight(SrvWindow.SHARING)) { onPick(SrvWindow.SHARING) }
    } else {
      StartItem("File Explorer", highlight(SrvWindow.PC_FILES)) { onPick(SrvWindow.PC_FILES) }
    }
    Spacer(Modifier.height(6.dp))
  }
}

@Composable
private fun StartItem(label: String, highlight: Boolean, onClick: () -> Unit) {
  Text(
    label,
    color = White,
    fontSize = 13.sp,
    modifier = Modifier
      .fillMaxWidth()
      .background(if (highlight) Color(0xFFE8B84A).copy(alpha = 0.28f) else Color.Transparent)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 9.dp),
  )
}

@Composable
private fun DeskIcon(mark: String, label: String, highlight: Boolean, onClick: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(72.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(if (highlight) Color.White.copy(alpha = 0.22f) else Color.Transparent)
      .border(
        if (highlight) 1.dp else 0.dp,
        if (highlight) Color(0xFFE8B84A) else Color.Transparent,
        RoundedCornerShape(6.dp),
      )
      .clickable(onClick = onClick)
      .padding(4.dp),
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(Color.White.copy(alpha = 0.92f)),
      contentAlignment = Alignment.Center,
    ) {
      Text(mark, color = Color(0xFF1A5276), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
    Text(
      label,
      color = White,
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.Center,
      maxLines = 2,
    )
  }
}

@Composable
private fun WinFrame(
  title: String,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(
    modifier = modifier
      .widthIn(max = 360.dp)
      .fillMaxWidth()
      .heightIn(max = 460.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(Color(0xFFF4F6F7))
      .border(1.dp, Color(0xFF7F8C8D), RoundedCornerShape(6.dp)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().background(Color(0xFF2874A6)).padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(title, color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
      Text("X", color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onClose).padding(4.dp))
    }
    Column(
      modifier = Modifier.verticalScroll(rememberScrollState()).padding(10.dp),
      content = { content() },
    )
  }
}

@Composable
private fun WinText(text: String) {
  Text(text, color = Color(0xFF1C2833), fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun FolderLine(name: String) {
  Text("📁  $name", color = Color(0xFF1C2833), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 2.dp))
}

@Composable
private fun RoleCheck(label: String, on: Boolean, onToggle: () -> Unit) {
  Text(
    if (on) "[x]  $label" else "[ ]  $label",
    color = Color(0xFF1C2833),
    fontSize = 13.sp,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(4.dp))
      .background(if (on) Color(0xFFD5F5E3) else Color.White)
      .border(1.dp, Color(0xFFBFC9CA), RoundedCornerShape(4.dp))
      .clickable(onClick = onToggle)
      .padding(horizontal = 8.dp, vertical = 8.dp),
  )
}

@Composable
private fun WinButton(label: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(Color(0xFF2E86C1))
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(label, color = White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
  }
}

@Composable
private fun WinField(
  value: String,
  onChange: (String) -> Unit,
  placeholder: String,
  onDone: () -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(36.dp)
      .clip(RoundedCornerShape(2.dp))
      .background(Color.White)
      .border(1.dp, Color(0xFF85929E), RoundedCornerShape(2.dp))
      .padding(horizontal = 8.dp),
    contentAlignment = Alignment.CenterStart,
  ) {
    if (value.isEmpty()) {
      Text(placeholder, color = Color(0xFF99A3A4), fontSize = 12.sp)
    }
    BasicTextField(
      value = value,
      onValueChange = { if (!it.contains('\n')) onChange(it) },
      modifier = Modifier.fillMaxWidth(),
      textStyle = TextStyle(color = Color.Black, fontSize = 13.sp),
      cursorBrush = SolidColor(Color.Black),
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      keyboardActions = KeyboardActions(onDone = { onDone() }),
    )
  }
}
