package com.pirillo.tagforge.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pirillo.tagforge.AppMode
import com.pirillo.tagforge.TagForgeViewModel
import com.pirillo.tagforge.ui.components.*
import com.pirillo.tagforge.ui.theme.*

@Composable
fun ToolsScreen(viewModel: TagForgeViewModel) {
    val mode = viewModel.mode
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp)) {
        when (mode) {
            AppMode.TOOL_ERASE -> ToolWaiting("Tap a tag to erase", Warning, WarningDim) { EraseIcon(size = 44.dp, color = Warning) }
                .also { CancelBtn { viewModel.cancelTool() } }
            AppMode.TOOL_LOCK -> ToolWaiting("Tap a tag to lock forever", Warning, WarningDim) { LockIcon(size = 44.dp, color = Warning) }
                .also { CancelBtn { viewModel.cancelTool() } }
            AppMode.SCAN_LAUNCH -> ToolWaiting("Tap tags to auto-open URLs", InfoBlue, InfoBlueDim) { LaunchIcon(size = 44.dp, color = InfoBlue) }
                .also { CancelBtn { viewModel.stopScanning() } }
            AppMode.CLONE_READ_SOURCE, AppMode.CLONE_WRITING -> CloneFlow(viewModel)
            else -> {
                if (viewModel.cloneCount > 0 && viewModel.mode == AppMode.IDLE) {
                    CloneDone(viewModel)
                } else {
                    ToolsMenu(viewModel)
                }
            }
        }
    }
}

@Composable
private fun ToolsMenu(viewModel: TagForgeViewModel) {
    Text("Tools", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(4.dp))
    Text("Tag utilities and operations", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(20.dp))

    ToolCard("Erase Tag", "Wipe all data from a tag", Warning, WarningDim, { EraseIcon(size = 22.dp, color = Warning) }) { viewModel.startErase() }
    ToolCard("Lock Tag", "Make permanently read-only", Warning, WarningDim, { LockIcon(size = 22.dp, color = Warning) }) { viewModel.startLock() }
    ToolCard("Clone Tags", "Duplicate one tag to many", Clone, CloneDim, { CloneIcon(size = 22.dp, color = Clone) }) { viewModel.startCloneRead() }
    ToolCard("Scan & Launch", "Scan tags and auto-open their URLs", InfoBlue, InfoBlueDim, { LaunchIcon(size = 22.dp, color = InfoBlue) }) { viewModel.startScanning(launch = true) }
    ToolCard("Verify Last Write", "Re-scan to confirm data was written", Accent, AccentDim, { ScanIcon(size = 22.dp, color = Accent) }) { viewModel.verifyWrite() }
}

@Composable
private fun ToolCard(name: String, desc: String, color: Color, bgColor: Color, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Surface, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp).background(bgColor, RoundedCornerShape(12.dp))) { icon() }
            Spacer(Modifier.width(14.dp))
            Column { Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary); Text(desc, fontSize = 12.sp, color = TextDim) }
        }
    }
}

@Composable
private fun ToolWaiting(text: String, color: Color, bgColor: Color, icon: @Composable () -> Unit) {
    val pa by rememberInfiniteTransition(label = "tw").animateFloat(1f, 0.5f, infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse), label = "twa")
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 60.dp).alpha(pa)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp).background(bgColor, CircleShape)) { icon() }
        Spacer(Modifier.height(24.dp))
        Text(text, color = color, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text("Hold tag against your phone", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CancelBtn(onClick: () -> Unit) {
    Spacer(Modifier.height(32.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        TextButton(onClick = onClick) { Text("Cancel", color = TextMuted) }
    }
}

@Composable
private fun CloneFlow(viewModel: TagForgeViewModel) {
    Text("Clone Tags", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(4.dp))
    Text("Duplicate one tag to many", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(24.dp))

    val step = if (viewModel.mode == AppMode.CLONE_READ_SOURCE) 0 else 1
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf("Read Source", "Tap Targets", "Done").forEachIndexed { i, label ->
            Column(Modifier.weight(1f)) {
                Surface(color = if (i <= step) Clone else Border, shape = RoundedCornerShape(2.dp), modifier = Modifier.fillMaxWidth().height(3.dp)) {}
                Spacer(Modifier.height(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall.copy(color = if (i <= step) Clone else TextDim, fontWeight = if (i == step) FontWeight.Bold else FontWeight.Normal))
            }
        }
    }

    Spacer(Modifier.height(32.dp))

    if (viewModel.mode == AppMode.CLONE_READ_SOURCE) {
        val pa by rememberInfiniteTransition(label = "cr").animateFloat(1f, 0.6f, infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse), label = "cra")
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().alpha(pa)) {
            NfcIcon(size = 48.dp, color = Clone)
            Spacer(Modifier.height(16.dp))
            Text("Tap source tag\u2026", color = Clone, fontWeight = FontWeight.SemiBold)
        }
        CancelBtn { viewModel.finishClone() }
    } else {
        // Writing mode
        Surface(color = Surface, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border), modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text("${viewModel.cloneCount}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Clone)
                Text("tags cloned", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                val pa by rememberInfiniteTransition(label = "ct").animateFloat(1f, 0.6f, infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse), label = "cta")
                Surface(color = CloneDim, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().alpha(pa)) {
                    Text("Tap next tag\u2026", color = Clone, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(14.dp).wrapContentWidth(Alignment.CenterHorizontally))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { viewModel.finishClone() }, colors = ButtonDefaults.buttonColors(containerColor = SurfaceLight, contentColor = TextPrimary),
            shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(vertical = 14.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Finish Batch", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CloneDone(viewModel: TagForgeViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 60.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp).background(AccentDim, CircleShape)) { NfcIcon(size = 36.dp, color = Accent) }
        Spacer(Modifier.height(20.dp))
        Text("Batch Complete", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text("${viewModel.cloneCount} tag${if (viewModel.cloneCount != 1) "s" else ""} cloned", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { viewModel.startCloneRead() }, colors = ButtonDefaults.buttonColors(containerColor = SurfaceLight, contentColor = TextPrimary),
            shape = RoundedCornerShape(14.dp)) { Text("New Batch", fontWeight = FontWeight.SemiBold) }
    }
}
