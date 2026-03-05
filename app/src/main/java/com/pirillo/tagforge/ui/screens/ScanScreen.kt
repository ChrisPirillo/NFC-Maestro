package com.pirillo.tagforge.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.pirillo.tagforge.AppMode
import com.pirillo.tagforge.TagForgeViewModel
import com.pirillo.tagforge.nfc.TagInfo
import com.pirillo.tagforge.ui.components.*
import com.pirillo.tagforge.ui.theme.*

@Composable
fun ScanScreen(viewModel: TagForgeViewModel) {
    val isScanning = viewModel.mode == AppMode.SCANNING || viewModel.mode == AppMode.SCAN_LAUNCH
    val tag = viewModel.scannedTag
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (tag == null) ScanPrompt(isScanning, viewModel)
        else TagDetail(tag, viewModel)
    }
}

@Composable
private fun ScanPrompt(isScanning: Boolean, viewModel: TagForgeViewModel) {
    val inf = rememberInfiniteTransition(label = "s")

    Spacer(Modifier.height(20.dp))

    // Animated scan area
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
        // Rotating dashed rings
        for (i in 0..4) {
            val speed = listOf(12f, -8f, 15f, -6f, 10f)[i]
            val radius = listOf(56f, 72f, 90f, 108f, 120f)[i]
            val baseAlpha = listOf(0.15f, 0.08f, 0.18f, 0.06f, 0.12f)[i]
            val rot by inf.animateFloat(0f, 360f, infiniteRepeatable(tween((36000 / speed.toInt()).let { if (it < 0) -it else it }, easing = LinearEasing), RepeatMode.Restart), label = "r$i")
            val pulseAlpha by inf.animateFloat(baseAlpha * 0.5f, baseAlpha * (if (isScanning) 2.5f else 1.2f),
                infiniteRepeatable(tween(2000 + i * 300, easing = EaseInOut), RepeatMode.Reverse), label = "a$i")

            Box(modifier = Modifier.size(radius.dp * 2).rotate(if (speed < 0) -rot else rot).drawBehind {
                drawCircle(color = (if (isScanning) Accent else TextDim).copy(alpha = pulseAlpha), style = Stroke(width = if (isScanning) 2.5f else 1.5f, cap = StrokeCap.Round,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f + i * 2, 16f + i * 4), 0f)))
            })
        }

        // Sweeping arcs (scanning only)
        if (isScanning) {
            for (i in 0..2) {
                val sweep by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(3000 - i * 400, easing = LinearEasing), RepeatMode.Restart), label = "sw$i")
                Box(modifier = Modifier.size((80 + i * 30).dp).rotate(sweep).drawBehind {
                    drawArc(Accent.copy(alpha = 0.2f - i * 0.05f), startAngle = 0f, sweepAngle = 100f, useCenter = false, style = Stroke(width = 3f - i * 0.5f, cap = StrokeCap.Round))
                })
            }
        }

        // Expanding pulse rings (scanning only)
        if (isScanning) {
            for (i in 0..1) {
                val scale by inf.animateFloat(0.4f, 1.3f, infiniteRepeatable(tween(2000, delayMillis = i * 1000, easing = EaseOut), RepeatMode.Restart), label = "ps$i")
                val pAlpha by inf.animateFloat(0.25f, 0f, infiniteRepeatable(tween(2000, delayMillis = i * 1000, easing = EaseOut), RepeatMode.Restart), label = "pa$i")
                Box(Modifier.size(100.dp).scale(scale).alpha(pAlpha).border(1.5.dp, Accent, CircleShape))
            }
        }

        // Center glow
        val glowAlpha by inf.animateFloat(if (isScanning) 0.1f else 0.04f, if (isScanning) 0.25f else 0.08f,
            infiniteRepeatable(tween(if (isScanning) 1500 else 3000, easing = EaseInOut), RepeatMode.Reverse), label = "glow")
        Box(modifier = Modifier.size(130.dp).background(
            Brush.radialGradient(listOf((if (isScanning) Accent else TextDim).copy(alpha = glowAlpha), Color.Transparent)), CircleShape))

        // Center circle with icon
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(88.dp).background(if (isScanning) AccentDim else SurfaceLight, CircleShape)) {
            NfcIcon(size = 44.dp, color = if (isScanning) Accent else TextDim)
        }
    }

    Spacer(Modifier.height(20.dp))
    Text(if (isScanning) "Listening\u2026" else "Ready to Scan", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(8.dp))
    Text(if (isScanning) "Hold tag against your phone" else "Tap below to start", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(28.dp))
    Button(onClick = { if (isScanning) viewModel.stopScanning() else viewModel.startScanning() },
        colors = ButtonDefaults.buttonColors(containerColor = if (isScanning) AccentDim else Accent, contentColor = if (isScanning) Accent else Void),
        shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp)
    ) { Text(if (isScanning) "Cancel" else "Scan", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
}

@Composable
private fun TagDetail(tag: TagInfo, viewModel: TagForgeViewModel) {
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current

    // Header
    Surface(color = Surface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Border), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).background(AccentDim, RoundedCornerShape(12.dp))) { NfcIcon(size = 22.dp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(tag.type ?: "Unknown", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tag.uid, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.width(6.dp))
                        Surface(onClick = { clipboard.setText(AnnotatedString(tag.uid)); viewModel.clearMessages() },
                            color = SurfaceLight, shape = RoundedCornerShape(4.dp)) {
                            Text("COPY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextDim, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
            if (tag.maxSize > 0) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaChip("Memory", "${tag.maxSize}B", Modifier.weight(1f)); MetaChip("Used", "${tag.usedSize}B", Modifier.weight(1f)); MetaChip("Records", "${tag.records.size}", Modifier.weight(1f))
                }
            }
        }
    }

    // Extended info
    if (tag.atqa != null || tag.manufacturer != null) {
        Spacer(Modifier.height(12.dp))
        Surface(color = Surface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Border), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("TAG DETAILS", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 8.dp))
                tag.manufacturer?.let { DetailRow("Manufacturer", it) }
                tag.atqa?.let { DetailRow("ATQA", it) }
                tag.sak?.let { DetailRow("SAK", "0x$it") }
                tag.maxTransceiveLength?.let { DetailRow("Max Transceive", "$it bytes") }
            }
        }
    }

    // Records with tappable URLs
    if (tag.records.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Surface(color = Surface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Border), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("NDEF RECORDS", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 12.dp))
                tag.records.forEachIndexed { i, rec ->
                    val isUrl = rec.type == "URL" && (rec.value.startsWith("http://") || rec.value.startsWith("https://"))
                    val tc = when (rec.type) { "URL" -> InfoBlue; "Text" -> Accent; else -> TextMuted }
                    Surface(color = SurfaceLight, shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().then(if (i < tag.records.size - 1) Modifier.padding(bottom = 8.dp) else Modifier)
                            .then(if (isUrl) Modifier.clickable { uriHandler.openUri(rec.value) } else Modifier)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                            Surface(color = tc.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
                                Text(rec.type, color = tc, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(rec.value, style = MaterialTheme.typography.bodyMedium.copy(color = if (isUrl) InfoBlue else TextPrimary), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // QR code for first URL
        val firstUrl = tag.records.firstOrNull { it.type == "URL" && it.value.startsWith("http") }
        if (firstUrl != null) {
            Spacer(Modifier.height(12.dp))
            Surface(color = Surface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Border), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("QR CODE", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 12.dp))
                    val qrBitmap = remember(firstUrl.value) { generateQR(firstUrl.value, 360) }
                    qrBitmap?.let { Image(bitmap = it.asImageBitmap(), contentDescription = "QR Code", modifier = Modifier.size(160.dp).background(Color.White, RoundedCornerShape(12.dp)).padding(8.dp)) }
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { viewModel.startScanning() }, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Void),
            shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(vertical = 14.dp), modifier = Modifier.weight(1f)
        ) { Text("Scan Again", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        val ctx = androidx.compose.ui.platform.LocalContext.current
        Button(onClick = {
            val data = viewModel.exportTagData()
            if (data != null) {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(android.content.Intent.EXTRA_TEXT, data) }
                ctx.startActivity(android.content.Intent.createChooser(intent, "Share tag data"))
            }
        }, colors = ButtonDefaults.buttonColors(containerColor = SurfaceLight, contentColor = TextPrimary),
            shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(vertical = 14.dp), modifier = Modifier.weight(1f)
        ) { Text("Share", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, color = TextDim, fontSize = 12.sp, modifier = Modifier.width(110.dp))
        Text(value, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun MetaChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(color = SurfaceLight, shape = RoundedCornerShape(10.dp), modifier = modifier) {
        Column(Modifier.padding(10.dp)) { Text(label.uppercase(), style = MaterialTheme.typography.labelSmall); Spacer(Modifier.height(2.dp)); Text(value, style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp)) }
    }
}

private fun generateQR(text: String, size: Int): Bitmap? = try {
    val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) for (y in 0 until size) bmp.setPixel(x, y, if (matrix.get(x, y)) 0xFF0A0A0F.toInt() else 0xFFFFFFFF.toInt())
    bmp
} catch (_: Exception) { null }
