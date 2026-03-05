package com.pirillo.tagforge.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pirillo.tagforge.AppMode
import com.pirillo.tagforge.TagForgeViewModel
import com.pirillo.tagforge.WriteType
import com.pirillo.tagforge.ui.components.*
import com.pirillo.tagforge.ui.theme.*

@Composable
fun WriteScreen(viewModel: TagForgeViewModel) {
    val isReady = viewModel.mode == AppMode.WRITE_READY

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp)) {
        Text("Write to Tag", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text("Choose a record type, enter data, tap a tag", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))

        // Type selector with scroll fade hint
        Box {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(end = 32.dp)) {
                WriteType.entries.forEach { type ->
                    val sel = viewModel.writeType == type
                    Surface(color = if (sel) AccentDim else Surface, shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, if (sel) Accent else Border),
                        modifier = Modifier.width(64.dp).clickable { viewModel.writeType = type }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp)) {
                            val c = if (sel) Accent else TextDim
                            when (type) {
                                WriteType.URL -> NfcIcon(size = 18.dp, color = c); WriteType.TEXT -> WriteIcon(size = 18.dp, color = c)
                                WriteType.WIFI -> ScanIcon(size = 18.dp, color = c); WriteType.PHONE -> NfcIcon(size = 18.dp, color = c)
                                WriteType.EMAIL -> WriteIcon(size = 18.dp, color = c); WriteType.SMS -> CloneIcon(size = 18.dp, color = c)
                                WriteType.LOCATION -> LocationIcon(size = 18.dp, color = c); WriteType.CONTACT -> ContactIcon(size = 18.dp, color = c)
                                WriteType.APP -> AppIcon(size = 18.dp, color = c); WriteType.POSTER -> PosterIcon(size = 18.dp, color = c)
                            }
                            Spacer(Modifier.height(5.dp))
                            Text(type.label, color = if (sel) Accent else TextMuted, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 9.sp))
                        }
                    }
                }
            }
            // Fade hint on right edge
            Box(Modifier.align(Alignment.CenterEnd).width(40.dp).fillMaxHeight()
                .background(Brush.horizontalGradient(listOf(Color.Transparent, Void))))
        }
        Text("Scroll for more \u203A", color = TextDim, fontSize = 10.sp, modifier = Modifier.align(Alignment.End).padding(top = 4.dp))

        Spacer(Modifier.height(16.dp))

        // Input fields
        Surface(color = Surface, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Border), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                when (viewModel.writeType) {
                    WriteType.URL -> { FL("URL"); VF(viewModel.writeUrl, { viewModel.writeUrl = it }, "https://arcade.pirillo.com", err = viewModel.writeUrl.let { it.isNotBlank() && it != "https://" && !viewModel.isValidUrl(it) })
                        if (viewModel.writeUrl.isNotBlank() && viewModel.writeUrl != "https://" && "://" !in viewModel.writeUrl) Text("https:// added automatically", color = Accent, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp)) }
                    WriteType.TEXT -> { FL("Text"); VF(viewModel.writeText, { viewModel.writeText = it }, "Enter text\u2026", lines = 3) }
                    WriteType.WIFI -> { FL("Network"); VF(viewModel.writeWifiSsid, { viewModel.writeWifiSsid = it }, "MyNetwork_5G"); Spacer(Modifier.height(12.dp)); FL("Password"); VF(viewModel.writeWifiPassword, { viewModel.writeWifiPassword = it }, "Password") }
                    WriteType.PHONE -> { FL("Phone"); VF(viewModel.writePhone, { viewModel.writePhone = it }, "+1 555-123-4567", err = viewModel.writePhone.let { it.isNotBlank() && !viewModel.isValidPhone(it) }) }
                    WriteType.EMAIL -> { FL("Email"); VF(viewModel.writeEmail, { viewModel.writeEmail = it }, "hello@example.com", err = viewModel.writeEmail.let { it.isNotBlank() && !viewModel.isValidEmail(it) }) }
                    WriteType.SMS -> { FL("Phone"); VF(viewModel.writeSms, { viewModel.writeSms = it }, "+1 555-123-4567", err = viewModel.writeSms.let { it.isNotBlank() && !viewModel.isValidPhone(it) }) }
                    WriteType.LOCATION -> { FL("Latitude"); VF(viewModel.writeLat, { viewModel.writeLat = it }, "47.6062"); Spacer(Modifier.height(12.dp)); FL("Longitude"); VF(viewModel.writeLon, { viewModel.writeLon = it }, "-122.3321") }
                    WriteType.CONTACT -> { FL("Name"); VF(viewModel.writeContactName, { viewModel.writeContactName = it }, "Jane Smith"); Spacer(Modifier.height(12.dp)); FL("Phone"); VF(viewModel.writeContactPhone, { viewModel.writeContactPhone = it }, "+1 555-123-4567"); Spacer(Modifier.height(12.dp)); FL("Email"); VF(viewModel.writeContactEmail, { viewModel.writeContactEmail = it }, "jane@example.com") }
                    WriteType.APP -> { FL("Package"); VF(viewModel.writeAppPkg, { viewModel.writeAppPkg = it }, "com.example.myapp", err = viewModel.writeAppPkg.let { it.isNotBlank() && '.' !in it }) }
                    WriteType.POSTER -> { FL("Title"); VF(viewModel.writePosterTitle, { viewModel.writePosterTitle = it }, "My Smart Poster"); Spacer(Modifier.height(12.dp)); FL("URL"); VF(viewModel.writePosterUrl, { viewModel.writePosterUrl = it }, "https://example.com", err = viewModel.writePosterUrl.let { it.isNotBlank() && !viewModel.isValidUrl(it) }) }
                }
                // Byte counter
                val bytes = viewModel.estimateBytes(); val pct = (bytes.toFloat() / 137 * 100).coerceAtMost(100f)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("\u2248 ${bytes}B", fontSize = 11.sp, color = TextDim)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).height(4.dp).background(SurfaceLight, RoundedCornerShape(2.dp))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(pct / 100f).background(if (pct > 90) Warning else if (pct > 60) Color(0xFFFFA500) else Accent, RoundedCornerShape(2.dp)))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(if (pct > 90) "Tight!" else if (pct > 60) "OK" else "Plenty", fontSize = 10.sp, color = TextDim)
                }
            }
        }

        // Multi-record
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("MULTI-RECORD", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = { viewModel.addMultiRecord() }, enabled = viewModel.hasValidInput()) { Text("+ Add", color = if (viewModel.hasValidInput()) Accent else TextDim, fontSize = 11.sp) }
        }
        viewModel.multiRecords.forEachIndexed { i, mr ->
            Surface(color = SurfaceLight, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
                    Surface(color = AccentDim, shape = RoundedCornerShape(4.dp)) { Text(mr.type.label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Accent, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) }
                    Spacer(Modifier.width(10.dp))
                    Text(mr.label, fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(1f), maxLines = 1)
                    Surface(onClick = { viewModel.removeMultiRecord(i) }, color = Color.Transparent) { Text("\u00D7", fontSize = 16.sp, color = TextDim) }
                }
            }
        }

        // Lock toggle
        Surface(color = if (viewModel.lockAfterWrite) WarningDim else Surface, shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (viewModel.lockAfterWrite) Warning.copy(alpha = 0.5f) else Border),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.lockAfterWrite = !viewModel.lockAfterWrite }.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Column(Modifier.weight(1f)) { Text("Lock after writing", color = if (viewModel.lockAfterWrite) Warning else TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(if (viewModel.lockAfterWrite) "PERMANENTLY read-only" else "Tag stays writable", color = if (viewModel.lockAfterWrite) Warning.copy(alpha = 0.7f) else TextDim, fontSize = 11.sp) }
                Switch(checked = viewModel.lockAfterWrite, onCheckedChange = { viewModel.lockAfterWrite = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Warning, checkedTrackColor = Warning.copy(alpha = 0.3f), uncheckedThumbColor = TextDim, uncheckedTrackColor = SurfaceLight))
            }
        }

        // Batch mode toggle
        Surface(color = if (viewModel.batchMode) AccentDim else Surface, shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (viewModel.batchMode) Accent.copy(alpha = 0.4f) else Border),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.batchMode = !viewModel.batchMode }.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Column(Modifier.weight(1f)) { Text("Batch write mode", color = if (viewModel.batchMode) Accent else TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(if (viewModel.batchMode) "Stay in write mode after each tag" else "Single write", color = if (viewModel.batchMode) Accent.copy(alpha = 0.6f) else TextDim, fontSize = 11.sp) }
                Switch(checked = viewModel.batchMode, onCheckedChange = { viewModel.batchMode = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Accent, checkedTrackColor = Accent.copy(alpha = 0.3f), uncheckedThumbColor = TextDim, uncheckedTrackColor = SurfaceLight))
            }
        }

        if (viewModel.batchMode && viewModel.batchCount > 0) {
            Surface(color = AccentDim, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Accent.copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Text("${viewModel.batchCount}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Accent)
                    Spacer(Modifier.width(12.dp))
                    Text("tags written", fontSize = 12.sp, color = TextMuted)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (!isReady) {
            Button(onClick = { if (viewModel.canWrite()) viewModel.attemptWrite() }, enabled = viewModel.canWrite(),
                colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.canWrite()) Accent else SurfaceLight, contentColor = if (viewModel.canWrite()) Void else TextDim),
                shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(vertical = 16.dp), modifier = Modifier.fillMaxWidth()
            ) { Text(if (viewModel.multiRecords.isNotEmpty()) "Write ${viewModel.multiRecords.size} Records" else "Write to Tag", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
        } else {
            val pa by rememberInfiniteTransition(label = "wp").animateFloat(1f, 0.6f, infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse), label = "wpa")
            Surface(color = AccentDim, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Accent), modifier = Modifier.fillMaxWidth().alpha(pa)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                    NfcIcon(size = 36.dp, color = Accent); Spacer(Modifier.height(12.dp))
                    Text(if (viewModel.lockAfterWrite) "Tap to write & lock" else "Tap a tag to write", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { viewModel.cancelWrite() }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Cancel", color = TextMuted, fontSize = 12.sp) }
        }

        // Lock confirmation dialog
        if (viewModel.showLockConfirm) {
            AlertDialog(onDismissRequest = { viewModel.showLockConfirm = false },
                title = { Text("Write & Lock", color = TextPrimary) },
                text = { Text("This will write data AND permanently lock the tag. It can NEVER be written to again. Are you sure?", color = TextMuted) },
                confirmButton = { TextButton(onClick = { viewModel.confirmLockAndWrite() }) { Text("Write & Lock", color = Warning, fontWeight = FontWeight.SemiBold) } },
                dismissButton = { TextButton(onClick = { viewModel.showLockConfirm = false }) { Text("Cancel", color = TextMuted) } },
                containerColor = Surface, shape = RoundedCornerShape(20.dp))
        }

        // Size warning dialog
        if (viewModel.showSizeWarning) {
            AlertDialog(onDismissRequest = { viewModel.showSizeWarning = false },
                title = { Text("Large Data", color = TextPrimary) },
                text = { Text("Your data is ~${viewModel.estimateBytes()} bytes. Smaller tags (NTAG213) only hold 137 bytes. If the write fails, try a larger tag (NTAG215: 504B, NTAG216: 888B).", color = TextMuted) },
                confirmButton = { TextButton(onClick = { viewModel.confirmSizeAndWrite() }) { Text("Write Anyway", color = Accent, fontWeight = FontWeight.SemiBold) } },
                dismissButton = { TextButton(onClick = { viewModel.showSizeWarning = false }) { Text("Cancel", color = TextMuted) } },
                containerColor = Surface, shape = RoundedCornerShape(20.dp))
        }
    }
}

@Composable private fun FL(text: String) { Text("$text DATA", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 8.dp)) }

@Composable
private fun VF(value: String, onChange: (String) -> Unit, placeholder: String, lines: Int = 1, err: Boolean = false) {
    OutlinedTextField(value = value, onValueChange = onChange, placeholder = { Text(placeholder, color = TextDim, fontSize = 14.sp) },
        isError = err, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent, unfocusedBorderColor = Border, errorBorderColor = Warning,
            focusedContainerColor = SurfaceLight, unfocusedContainerColor = SurfaceLight, cursorColor = Accent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary, fontSize = 14.sp),
        shape = RoundedCornerShape(10.dp), minLines = lines, modifier = Modifier.fillMaxWidth())
}
