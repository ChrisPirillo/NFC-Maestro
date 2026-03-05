package com.pirillo.tagforge

import android.content.Intent
import android.nfc.NdefRecord
import android.nfc.Tag
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pirillo.tagforge.nfc.NfcManager
import com.pirillo.tagforge.nfc.TagInfo

enum class AppMode { IDLE, SCANNING, WRITE_READY, CLONE_READ_SOURCE, CLONE_WRITING, TOOL_ERASE, TOOL_LOCK, SCAN_LAUNCH }
enum class WriteType(val label: String) { URL("URL"), TEXT("Text"), WIFI("Wi-Fi"), PHONE("Phone"), EMAIL("Email"), SMS("SMS"), LOCATION("Loc"), CONTACT("Contact"), APP("App"), POSTER("Poster") }
data class MultiRecord(val type: WriteType, val label: String, val buildFn: (Tag) -> NdefRecord)
data class HistoryEntry(val id: Long = System.nanoTime(), val action: String, val tagType: String, val detail: String, val timestamp: Long = System.currentTimeMillis())

class TagForgeViewModel : ViewModel() {
    var mode by mutableStateOf(AppMode.IDLE); private set
    var lastError by mutableStateOf<String?>(null); private set
    var lastSuccess by mutableStateOf<String?>(null); private set

    // Scan
    var scannedTag by mutableStateOf<TagInfo?>(null); private set

    // Write - each type has persistent fields
    var writeType by mutableStateOf(WriteType.URL)
    var writeUrl by mutableStateOf("https://")
    var writeText by mutableStateOf("")
    var writeWifiSsid by mutableStateOf(""); var writeWifiPassword by mutableStateOf("")
    var writePhone by mutableStateOf(""); var writeEmail by mutableStateOf("")
    var writeSms by mutableStateOf(""); var writeLat by mutableStateOf(""); var writeLon by mutableStateOf("")
    var writeContactName by mutableStateOf(""); var writeContactPhone by mutableStateOf(""); var writeContactEmail by mutableStateOf("")
    var writeAppPkg by mutableStateOf(""); var writePosterTitle by mutableStateOf(""); var writePosterUrl by mutableStateOf("")
    var lockAfterWrite by mutableStateOf(false); var batchMode by mutableStateOf(false)
    var batchCount by mutableIntStateOf(0); private set
    val multiRecords = mutableStateListOf<MultiRecord>()

    // Clone
    var cloneSource by mutableStateOf<TagInfo?>(null); private set
    var cloneCount by mutableIntStateOf(0); private set

    // Confirmations
    var showLockConfirm by mutableStateOf(false)
    var showSizeWarning by mutableStateOf(false)

    // History
    var history by mutableStateOf(listOf<HistoryEntry>()); private set

    // === Validation ===
    fun isValidUrl(u: String): Boolean { if (u.isBlank() || u == "https://") return false; val n = if ("://" !in u) "https://$u" else u; return try { java.net.URL(n); true } catch (_: Exception) { false } }
    fun isValidEmail(e: String) = e.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
    fun isValidPhone(p: String) = p.replace(Regex("[^\\d+]"), "").length >= 7

    fun hasValidInput(): Boolean = when (writeType) {
        WriteType.URL -> isValidUrl(writeUrl); WriteType.TEXT -> writeText.isNotBlank()
        WriteType.WIFI -> writeWifiSsid.isNotBlank(); WriteType.PHONE -> isValidPhone(writePhone)
        WriteType.EMAIL -> isValidEmail(writeEmail); WriteType.SMS -> isValidPhone(writeSms)
        WriteType.LOCATION -> writeLat.isNotBlank() && writeLon.isNotBlank() && writeLat.toDoubleOrNull() != null && writeLon.toDoubleOrNull() != null
        WriteType.CONTACT -> writeContactName.isNotBlank()
        WriteType.APP -> writeAppPkg.isNotBlank() && '.' in writeAppPkg
        WriteType.POSTER -> isValidUrl(writePosterUrl)
    }
    fun canWrite() = multiRecords.isNotEmpty() || hasValidInput()
    fun normalizeUrl(u: String): String { val t = u.trim(); if (t.isBlank() || t == "https://") return t; val deduped = t.replace(Regex("^https?://https?://", RegexOption.IGNORE_CASE), "https://"); return if ("://" !in deduped) "https://$deduped" else deduped }

    fun estimateBytes(): Int {
        if (multiRecords.isNotEmpty()) return multiRecords.size * 20 // rough estimate
        return when (writeType) {
            WriteType.URL -> normalizeUrl(writeUrl).length + 5; WriteType.TEXT -> writeText.length + 7
            WriteType.WIFI -> writeWifiSsid.length + writeWifiPassword.length + 20; WriteType.PHONE -> writePhone.length + 10
            WriteType.EMAIL -> writeEmail.length + 12; WriteType.SMS -> writeSms.length + 10
            WriteType.LOCATION -> writeLat.length + writeLon.length + 10; WriteType.CONTACT -> writeContactName.length + writeContactPhone.length + writeContactEmail.length + 50
            WriteType.APP -> writeAppPkg.length + 5; WriteType.POSTER -> writePosterTitle.length + writePosterUrl.length + 15
        }
    }

    // === State actions ===
    fun startScanning(launch: Boolean = false) { mode = if (launch) AppMode.SCAN_LAUNCH else AppMode.SCANNING; scannedTag = null; clearMessages() }
    fun stopScanning() { mode = AppMode.IDLE }
    fun attemptWrite() {
        // Gate 1: Lock confirmation
        if (lockAfterWrite && !showLockConfirm) { showLockConfirm = true; return }
        showLockConfirm = false
        // Gate 2: Size warning
        if (estimateBytes() > 130 && !showSizeWarning) { showSizeWarning = true; return }
        showSizeWarning = false
        prepareWrite()
    }
    fun confirmLockAndWrite() { showLockConfirm = false; showSizeWarning = false; prepareWrite() }
    fun confirmSizeAndWrite() { showSizeWarning = false; prepareWrite() }
    fun prepareWrite() { mode = AppMode.WRITE_READY; clearMessages() }
    fun cancelWrite() { mode = AppMode.IDLE }
    fun startErase() { mode = AppMode.TOOL_ERASE; clearMessages() }
    fun startLock() { mode = AppMode.TOOL_LOCK; clearMessages() }
    fun cancelTool() { mode = AppMode.IDLE }
    fun startCloneRead() { mode = AppMode.CLONE_READ_SOURCE; cloneSource = null; cloneCount = 0; clearMessages() }
    fun finishClone() { if (cloneCount > 0) addHistory("Clone", cloneSource?.type ?: "?", "Batch: $cloneCount tag${if (cloneCount != 1) "s" else ""}"); mode = AppMode.IDLE }
    fun verifyWrite() { mode = AppMode.SCANNING; scannedTag = null; clearMessages(); lastSuccess = "Tap the tag to verify its contents" }
    fun addMultiRecord() { if (!hasValidInput()) return; multiRecords.add(buildCurrentMultiRecord()); }
    fun removeMultiRecord(index: Int) { if (index in multiRecords.indices) multiRecords.removeAt(index) }

    fun removeHistoryItem(id: Long) { history = history.filter { it.id != id } }
    fun clearHistory() { history = emptyList() }

    fun exportTagData(): String? {
        val t = scannedTag ?: return null
        return buildString {
            appendLine("NFC Maestro \u2014 Tag Export"); appendLine("Serial: ${t.uid}")
            if (t.records.isNotEmpty()) { appendLine(); appendLine("Records:")
                t.records.forEachIndexed { i, r -> appendLine("  ${i + 1}. [${r.type}] ${r.value}") } }
        }
    }

    // === Tag handling ===
    fun onTagDiscovered(tag: Tag) = when (mode) {
        AppMode.SCANNING -> handleScan(tag)
        AppMode.SCAN_LAUNCH -> handleScanLaunch(tag)
        AppMode.WRITE_READY -> handleWrite(tag)
        AppMode.CLONE_READ_SOURCE -> handleCloneSource(tag)
        AppMode.CLONE_WRITING -> handleCloneTarget(tag)
        AppMode.TOOL_ERASE -> handleErase(tag)
        AppMode.TOOL_LOCK -> handleLock(tag)
        AppMode.IDLE -> handleScan(tag)
    }

    // Store intent for scan-launch URL opening
    var pendingLaunchUrl by mutableStateOf<String?>(null); private set
    fun clearPendingLaunch() { pendingLaunchUrl = null }

    private fun handleScan(tag: Tag) {
        try { val info = NfcManager.parseTag(tag); scannedTag = info; mode = AppMode.IDLE
            lastSuccess = "Tag read"; addHistory("Read", info.type ?: "?", info.records.firstOrNull()?.value ?: "Empty")
        } catch (e: Exception) { lastError = "Read: ${e.message}"; mode = AppMode.IDLE } }

    private fun handleScanLaunch(tag: Tag) {
        try { val info = NfcManager.parseTag(tag)
            val url = info.records.firstOrNull { it.type == "URL" && (it.value.startsWith("http://") || it.value.startsWith("https://")) }
            if (url != null) { pendingLaunchUrl = url.value; lastSuccess = "Launching: ${url.value}"; addHistory("Launch", info.type ?: "?", url.value) }
            else { lastError = "No URL found on tag" }
            // Stay in SCAN_LAUNCH mode
        } catch (e: Exception) { lastError = "Read: ${e.message}" } }

    private fun handleWrite(tag: Tag) {
        val result = if (multiRecords.isNotEmpty()) {
            val recs = multiRecords.map { it.buildFn(tag) }
            NfcManager.writeMultipleRecords(tag, recs)
        } else writeSingleRecord(tag)

        result.fold(
            onSuccess = {
                val lbl = if (multiRecords.isNotEmpty()) "${multiRecords.size} records" else writeType.label
                if (lockAfterWrite) { NfcManager.lockTag(tag).fold(
                    onSuccess = { lastSuccess = "Written & locked"; addHistory("Write+Lock", lbl, currentWriteValue().take(40)) },
                    onFailure = { lastSuccess = "Written, lock failed"; addHistory("Write", lbl, currentWriteValue().take(40)) })
                } else { lastSuccess = "Tag written"; addHistory("Write", lbl, currentWriteValue().take(40)) }
                if (batchMode) { batchCount++; return } // Stay in write mode
                mode = AppMode.IDLE
            },
            onFailure = { lastError = "Write: ${it.message}"; mode = AppMode.IDLE })
    }

    private fun writeSingleRecord(tag: Tag): Result<Unit> = when (writeType) {
        WriteType.URL -> NfcManager.writeUrl(tag, normalizeUrl(writeUrl))
        WriteType.TEXT -> NfcManager.writeText(tag, writeText)
        WriteType.WIFI -> NfcManager.writeWifi(tag, writeWifiSsid, writeWifiPassword)
        WriteType.PHONE -> NfcManager.writeUrl(tag, "tel:${writePhone.filter { it.isDigit() || it == '+' }}")
        WriteType.EMAIL -> NfcManager.writeUrl(tag, "mailto:$writeEmail")
        WriteType.SMS -> NfcManager.writeUrl(tag, "sms:${writeSms.filter { it.isDigit() || it == '+' }}")
        WriteType.LOCATION -> NfcManager.writeLocation(tag, writeLat, writeLon)
        WriteType.CONTACT -> NfcManager.writeVCard(tag, writeContactName, writeContactPhone, writeContactEmail)
        WriteType.APP -> NfcManager.writeAppLaunch(tag, writeAppPkg)
        WriteType.POSTER -> NfcManager.writeSmartPoster(tag, writePosterTitle, writePosterUrl)
    }

    private fun buildCurrentMultiRecord(): MultiRecord {
        val label = currentWriteValue().take(30)
        val type = writeType
        // Capture current values
        return when (type) {
            WriteType.URL -> { val v = normalizeUrl(writeUrl); MultiRecord(type, v) { NdefRecord.createUri(v) } }
            WriteType.TEXT -> { val v = writeText; MultiRecord(type, v) { val l = "en".toByteArray(); val t2 = v.toByteArray(); val p = ByteArray(1+l.size+t2.size); p[0]=l.size.toByte(); System.arraycopy(l,0,p,1,l.size); System.arraycopy(t2,0,p,1+l.size,t2.size); NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), p) } }
            WriteType.WIFI -> { val s=writeWifiSsid; val p=writeWifiPassword; MultiRecord(type, "WiFi: $s") { NdefRecord.createMime("application/vnd.wfa.wsc", "WIFI:S:$s;T:WPA;P:$p;;".toByteArray()) } }
            WriteType.PHONE -> { val v=writePhone; MultiRecord(type, v) { NdefRecord.createUri("tel:${v.filter { it.isDigit()||it=='+' }}") } }
            WriteType.EMAIL -> { val v=writeEmail; MultiRecord(type, v) { NdefRecord.createUri("mailto:$v") } }
            WriteType.SMS -> { val v=writeSms; MultiRecord(type, v) { NdefRecord.createUri("sms:${v.filter { it.isDigit()||it=='+' }}") } }
            WriteType.LOCATION -> { val la=writeLat; val lo=writeLon; MultiRecord(type, "geo:$la,$lo") { NdefRecord.createUri("geo:$la,$lo") } }
            WriteType.CONTACT -> { val n=writeContactName; MultiRecord(type, n) { NdefRecord.createMime("text/vcard", "BEGIN:VCARD\nVERSION:3.0\nFN:$n\nEND:VCARD".toByteArray()) } }
            WriteType.APP -> { val v=writeAppPkg; MultiRecord(type, v) { NdefRecord.createApplicationRecord(v) } }
            WriteType.POSTER -> { val t=writePosterTitle; val u=writePosterUrl; MultiRecord(type, u) { NdefRecord.createUri(u) } } // Simplified for multi-record
        }
    }

    private fun currentWriteValue() = when (writeType) {
        WriteType.URL -> writeUrl; WriteType.TEXT -> writeText; WriteType.WIFI -> writeWifiSsid
        WriteType.PHONE -> writePhone; WriteType.EMAIL -> writeEmail; WriteType.SMS -> writeSms
        WriteType.LOCATION -> writeLat; WriteType.CONTACT -> writeContactName
        WriteType.APP -> writeAppPkg; WriteType.POSTER -> writePosterUrl
    }

    private fun handleCloneSource(tag: Tag) {
        try { val info = NfcManager.parseTag(tag); if (info.rawNdefMessage == null) { lastError = "No NDEF data"; return }
            cloneSource = info; cloneCount = 0; mode = AppMode.CLONE_WRITING; lastSuccess = "Source captured"
        } catch (e: Exception) { lastError = "Read: ${e.message}" } }

    private fun handleCloneTarget(tag: Tag) {
        val msg = cloneSource?.rawNdefMessage ?: return
        NfcManager.writeMessage(tag, msg).fold(
            onSuccess = { cloneCount++; lastSuccess = "Cloned! ($cloneCount)"; addHistory("Clone", cloneSource?.type ?: "?", "Tag #$cloneCount") },
            onFailure = { lastError = "Clone: ${it.message}" }) }

    private fun handleErase(tag: Tag) {
        NfcManager.eraseTag(tag).fold(
            onSuccess = { lastSuccess = "Tag erased"; addHistory("Erase", NfcManager.parseTag(tag).type ?: "?", "Formatted"); mode = AppMode.IDLE },
            onFailure = { lastError = "Erase: ${it.message}"; mode = AppMode.IDLE }) }

    private fun handleLock(tag: Tag) {
        NfcManager.lockTag(tag).fold(
            onSuccess = { lastSuccess = "Tag locked permanently"; addHistory("Lock", "NDEF", "Read-only"); mode = AppMode.IDLE },
            onFailure = { lastError = "Lock: ${it.message}"; mode = AppMode.IDLE }) }

    private fun addHistory(action: String, tagType: String, detail: String) { history = listOf(HistoryEntry(action = action, tagType = tagType, detail = detail)) + history }
    fun clearMessages() { lastError = null; lastSuccess = null }
}
