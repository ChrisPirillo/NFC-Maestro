package com.pirillo.tagforge.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pirillo.tagforge.HistoryEntry
import com.pirillo.tagforge.TagForgeViewModel
import com.pirillo.tagforge.ui.components.*
import com.pirillo.tagforge.ui.theme.*

@Composable
fun HistoryScreen(viewModel: TagForgeViewModel) {
    val uriHandler = LocalUriHandler.current
    var pendingDelete by remember { mutableStateOf<Long?>(null) }
    var showClearAll by remember { mutableStateOf(false) }

    pendingDelete?.let { id -> AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Remove Entry", color = TextPrimary) }, text = { Text("Remove this item?", color = TextMuted) },
        confirmButton = { TextButton(onClick = { viewModel.removeHistoryItem(id); pendingDelete = null }) { Text("Remove", color = Warning, fontWeight = FontWeight.SemiBold) } },
        dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel", color = TextMuted) } }, containerColor = Surface, shape = RoundedCornerShape(20.dp)) }

    if (showClearAll) AlertDialog(onDismissRequest = { showClearAll = false }, title = { Text("Clear History", color = TextPrimary) }, text = { Text("Remove all entries?", color = TextMuted) },
        confirmButton = { TextButton(onClick = { viewModel.clearHistory(); showClearAll = false }) { Text("Clear All", color = Warning, fontWeight = FontWeight.SemiBold) } },
        dismissButton = { TextButton(onClick = { showClearAll = false }) { Text("Cancel", color = TextMuted) } }, containerColor = Surface, shape = RoundedCornerShape(20.dp))

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("History", style = MaterialTheme.typography.headlineMedium); Text("Recent operations", style = MaterialTheme.typography.bodyMedium) }
            if (viewModel.history.isNotEmpty()) TextButton(onClick = { showClearAll = true }) { Text("Clear All", color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
        }
        Spacer(Modifier.height(20.dp))
        if (viewModel.history.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 60.dp)) {
                HistoryIcon(size = 48.dp, color = TextDim); Spacer(Modifier.height(16.dp)); Text("No activity yet", color = TextMuted, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(viewModel.history, key = { _, e -> e.id }) { _, entry ->
                    val (ic, bg) = when (entry.action) { "Read" -> Accent to AccentDim; "Write" -> InfoBlue to InfoBlueDim; "Write+Lock" -> Warning to WarningDim
                        "Clone" -> Clone to CloneDim; "Erase" -> Warning to WarningDim; "Lock" -> Warning to WarningDim; "Launch" -> InfoBlue to InfoBlueDim; else -> TextMuted to SurfaceLight }
                    val isUrl = entry.detail.startsWith("http://") || entry.detail.startsWith("https://")

                    SwipeToDismissBox(state = rememberSwipeToDismissBoxState(confirmValueChange = { v -> if (v != SwipeToDismissBoxValue.Settled) { pendingDelete = entry.id }; false }),
                        backgroundContent = { Surface(color = Warning.copy(alpha = 0.15f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxSize()) {
                            Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxSize().padding(end = 20.dp)) { EraseIcon(size = 20.dp, color = Warning) } } },
                        content = { Surface(color = Surface, shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                            modifier = Modifier.fillMaxWidth().then(if (isUrl) Modifier.clickable { uriHandler.openUri(entry.detail) } else Modifier)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                                Surface(color = bg, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { when (entry.action) {
                                        "Read" -> ScanIcon(size = 18.dp, color = ic); "Write" -> WriteIcon(size = 18.dp, color = ic); "Write+Lock" -> LockIcon(size = 18.dp, color = ic)
                                        "Clone" -> CloneIcon(size = 18.dp, color = ic); "Erase" -> EraseIcon(size = 18.dp, color = ic); "Lock" -> LockIcon(size = 18.dp, color = ic)
                                        "Launch" -> LaunchIcon(size = 18.dp, color = ic); else -> NfcIcon(size = 18.dp, color = ic) } } }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) { Text(entry.action, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                        Spacer(Modifier.width(8.dp)); Surface(color = SurfaceLight, shape = RoundedCornerShape(4.dp)) { Text(entry.tagType, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) } }
                                    Spacer(Modifier.height(2.dp)); Text(entry.detail, style = MaterialTheme.typography.bodyMedium.copy(color = if (isUrl) InfoBlue else TextMuted), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                Text(formatTime(entry.timestamp), style = MaterialTheme.typography.labelSmall)
                            }
                        } })
                }
            }
        }
    }
}

private fun formatTime(ts: Long): String { val d = System.currentTimeMillis() - ts; return when { d < 60_000 -> "Now"; d < 3_600_000 -> "${d / 60_000}m"; d < 86_400_000 -> "${d / 3_600_000}h"; else -> "${d / 86_400_000}d" } }
