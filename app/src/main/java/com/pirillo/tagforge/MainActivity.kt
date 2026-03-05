package com.pirillo.tagforge

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pirillo.tagforge.ui.components.*
import com.pirillo.tagforge.ui.screens.*
import com.pirillo.tagforge.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFilters: Array<IntentFilter>
    private var vm: TagForgeViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)
        intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED), IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED), IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
        setContent { TagForgeTheme { val viewModel: TagForgeViewModel = viewModel(); vm = viewModel; MaestroApp(viewModel) } }
        handleNfcIntent(intent)
    }

    override fun onResume() { super.onResume(); nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFilters, null) }
    override fun onPause() { super.onPause(); nfcAdapter?.disableForegroundDispatch(this) }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); handleNfcIntent(intent) }

    private fun handleNfcIntent(intent: Intent) {
        val action = intent.action ?: return
        if (action in listOf(NfcAdapter.ACTION_NDEF_DISCOVERED, NfcAdapter.ACTION_TAG_DISCOVERED, NfcAdapter.ACTION_TECH_DISCOVERED)) {
            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            else @Suppress("DEPRECATION") intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let { vibrate(); vm?.onTagDiscovered(it) }
        }
    }

    private fun vibrate() { try { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) getSystemService(VibratorManager::class.java).defaultVibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        else @Suppress("DEPRECATION") (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)) } catch (_: Exception) {} }
}

@Composable
fun NotificationOverlay(viewModel: TagForgeViewModel) {
    val msg = viewModel.lastError ?: viewModel.lastSuccess; val isErr = viewModel.lastError != null
    LaunchedEffect(msg) { if (msg != null) { delay(2800); viewModel.clearMessages() } }
    AnimatedVisibility(visible = msg != null, enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(), exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(), modifier = Modifier.zIndex(10f)) {
        Surface(color = if (isErr) WarningDim else AccentDim, shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, if (isErr) Warning.copy(alpha = 0.4f) else Accent.copy(alpha = 0.4f)),
            shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(28.dp).background(if (isErr) Warning.copy(alpha = 0.2f) else Accent.copy(alpha = 0.2f), CircleShape)) {
                    NfcIcon(size = 14.dp, color = if (isErr) Warning else Accent) }
                Spacer(Modifier.width(12.dp)); Text(msg ?: "", color = if (isErr) Warning else Accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Surface, shape = RoundedCornerShape(24.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp).background(AccentDim, CircleShape)) { NfcIcon(size = 32.dp) }
                Spacer(Modifier.height(16.dp)); Text("NFC Maestro", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                AboutSection("What are NFC tags?", "Small passive chips that transfer data wirelessly when tapped. Powered by your phone\u2019s NFC radio.")
                AboutSection("What can you do?", "Write URLs, Wi-Fi, contacts, locations, smart posters, and more. Clone in batch. Lock permanently.")
                AboutSection("Getting started", "Chrome 89+ or an Android phone with NFC, plus blank NFC tags (NTAG213/215/216).")

                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                    Text("LINKS", style = MaterialTheme.typography.labelSmall.copy(color = Accent, fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 8.dp))
                    LinkRow("More of My Apps", "https://arcade.pirillo.com/", uriHandler)
                    LinkRow("Follow Chris Pirillo", "https://chris.pirillo.com/", uriHandler)
                    LinkRow("Learn How to Make Apps", "https://ctrlaltcreate.live/", uriHandler)
                    LinkRow("Donate to Me Here", "https://www.paypal.com/donate/?hosted_button_id=UMWCDWGVXVHZU", uriHandler)
                    LinkRow("Support My Patreon", "https://patreon.com/ChrisPirillo", uriHandler)
                }

                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AccentDim, contentColor = Accent),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) { Text("Got it", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun LinkRow(label: String, url: String, uriHandler: androidx.compose.ui.platform.UriHandler) {
    Surface(onClick = { uriHandler.openUri(url) }, color = SurfaceLight, shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Text(label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
    }
}

@Composable
private fun AboutSection(title: String, body: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = Accent, fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 4.dp))
        Text(body, color = TextMuted, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
fun MaestroApp(viewModel: TagForgeViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAbout by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Handle scan-launch URLs
    LaunchedEffect(viewModel.pendingLaunchUrl) {
        viewModel.pendingLaunchUrl?.let { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            viewModel.clearPendingLaunch()
        }
    }

    if (showAbout) AboutDialog(onDismiss = { showAbout = false })

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(containerColor = Void,
            topBar = { Surface(color = Void) { Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
                NfcIcon(size = 28.dp); Spacer(Modifier.width(10.dp)); Text("Maestro", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                Spacer(Modifier.weight(1f)); Surface(color = SurfaceLight, shape = CircleShape, modifier = Modifier.size(32.dp).clickable { showAbout = true }) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text("?", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold) } } } } },
            bottomBar = { Surface(color = Surface, shadowElevation = 8.dp) {
                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = 8.dp)) {
                    TabItem("Scan", 0, selectedTab, onClick = { selectedTab = 0 }) { ScanIcon(size = 20.dp, color = it) }
                    TabItem("Write", 1, selectedTab, onClick = { selectedTab = 1 }) { WriteIcon(size = 20.dp, color = it) }
                    TabItem("Tools", 2, selectedTab, onClick = { selectedTab = 2 }) { ToolsIcon(size = 20.dp, color = it) }
                    TabItem("History", 3, selectedTab, onClick = { selectedTab = 3 }) { HistoryIcon(size = 20.dp, color = it) }
                } } }
        ) { padding -> Box(Modifier.padding(padding)) { when (selectedTab) { 0 -> ScanScreen(viewModel); 1 -> WriteScreen(viewModel); 2 -> ToolsScreen(viewModel); 3 -> HistoryScreen(viewModel) } } }

        Column(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 56.dp)) { NotificationOverlay(viewModel) }
    }
}

@Composable
private fun TabItem(label: String, index: Int, selected: Int, onClick: () -> Unit, icon: @Composable (color: Color) -> Unit) {
    val isActive = index == selected; val color = if (isActive) Accent else TextDim
    Surface(onClick = onClick, color = Color.Transparent, shape = RoundedCornerShape(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            icon(color); Spacer(Modifier.height(4.dp)); Text(label.uppercase(), color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        }
    }
}
