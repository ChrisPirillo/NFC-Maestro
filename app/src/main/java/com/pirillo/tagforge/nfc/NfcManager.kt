package com.pirillo.tagforge.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import java.io.IOException

data class ParsedRecord(val type: String, val value: String, val rawPayload: ByteArray? = null)

data class TagInfo(
    val uid: String, val techList: List<String>, val type: String?,
    val maxSize: Int, val usedSize: Int, val isWritable: Boolean,
    val records: List<ParsedRecord>, val rawNdefMessage: NdefMessage? = null,
    val atqa: String? = null, val sak: String? = null,
    val manufacturer: String? = null, val maxTransceiveLength: Int? = null,
)

object NfcManager {

    fun parseTag(tag: Tag): TagInfo {
        val uid = tag.id?.joinToString(":") { "%02X".format(it) } ?: "Unknown"
        val techList = tag.techList.map { it.substringAfterLast(".") }
        var type: String? = null; var maxSize = 0; var usedSize = 0; var isWritable = false
        var records = listOf<ParsedRecord>(); var rawMsg: NdefMessage? = null
        var atqa: String? = null; var sak: String? = null; var maxTransceive: Int? = null
        NfcA.get(tag)?.let { atqa = it.atqa?.joinToString(" ") { b -> "%02X".format(b) }; sak = "%02X".format(it.sak.toByte()); maxTransceive = it.maxTransceiveLength }
        Ndef.get(tag)?.let { ndef -> type = ndef.type?.substringAfterLast("."); maxSize = ndef.maxSize; isWritable = ndef.isWritable
            ndef.cachedNdefMessage?.let { msg -> rawMsg = msg; usedSize = msg.toByteArray().size; records = msg.records.map { parseNdefRecord(it) } } }
        val friendlyType = type ?: when { techList.contains("MifareClassic") -> "MIFARE Classic"; techList.contains("MifareUltralight") -> "MIFARE Ultralight"; techList.contains("IsoDep") -> "ISO-DEP"; else -> techList.firstOrNull() ?: "Unknown" }
        val mfr = tag.id?.firstOrNull()?.let { guessManufacturer(it) }
        return TagInfo(uid, techList, friendlyType, maxSize, usedSize, isWritable, records, rawMsg, atqa, sak, mfr, maxTransceive)
    }

    private fun guessManufacturer(b: Byte) = when (b.toInt() and 0xFF) { 0x04 -> "NXP"; 0x02 -> "STMicro"; 0x05 -> "Infineon"; 0x07 -> "TI"; else -> "Unknown (0x%02X)".format(b) }

    private fun parseNdefRecord(record: NdefRecord): ParsedRecord = when (record.tnf) {
        NdefRecord.TNF_WELL_KNOWN -> when {
            record.type.contentEquals(NdefRecord.RTD_URI) -> { val p = record.payload; val prefix = URI_PREFIXES.getOrElse(p[0].toInt()) { "" }; ParsedRecord("URL", prefix + String(p, 1, p.size - 1, Charsets.UTF_8), p) }
            record.type.contentEquals(NdefRecord.RTD_TEXT) -> { val p = record.payload; val ll = p[0].toInt() and 0x3F; ParsedRecord("Text", String(p, 1 + ll, p.size - 1 - ll, Charsets.UTF_8), p) }
            record.type.contentEquals(NdefRecord.RTD_SMART_POSTER) -> ParsedRecord("Poster", "[Smart Poster]", record.payload)
            else -> ParsedRecord("WellKnown", record.payload?.let { String(it, Charsets.UTF_8) } ?: "", record.payload)
        }
        NdefRecord.TNF_MIME_MEDIA -> ParsedRecord("MIME (${String(record.type, Charsets.UTF_8)})", String(record.payload, Charsets.UTF_8), record.payload)
        NdefRecord.TNF_ABSOLUTE_URI -> ParsedRecord("URI", String(record.type, Charsets.UTF_8), record.payload)
        NdefRecord.TNF_EXTERNAL_TYPE -> ParsedRecord("External", String(record.payload ?: ByteArray(0), Charsets.UTF_8), record.payload)
        else -> ParsedRecord("Unknown", record.payload?.let { String(it, Charsets.UTF_8) } ?: "", record.payload)
    }

    fun writeUrl(tag: Tag, url: String) = writeRecords(tag, arrayOf(NdefRecord.createUri(url)))
    fun writeText(tag: Tag, text: String): Result<Unit> { val l = "en".toByteArray(Charsets.US_ASCII); val t = text.toByteArray(Charsets.UTF_8); val p = ByteArray(1 + l.size + t.size); p[0] = l.size.toByte(); System.arraycopy(l, 0, p, 1, l.size); System.arraycopy(t, 0, p, 1 + l.size, t.size); return writeRecords(tag, arrayOf(NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), p))) }
    fun writeWifi(tag: Tag, ssid: String, pass: String) = writeRecords(tag, arrayOf(NdefRecord.createMime("application/vnd.wfa.wsc", "WIFI:S:$ssid;T:WPA;P:$pass;;".toByteArray(Charsets.UTF_8))))
    fun writeLocation(tag: Tag, lat: String, lon: String) = writeRecords(tag, arrayOf(NdefRecord.createUri("geo:$lat,$lon")))
    fun writeVCard(tag: Tag, name: String, phone: String, email: String) = writeRecords(tag, arrayOf(NdefRecord.createMime("text/vcard", "BEGIN:VCARD\nVERSION:3.0\nFN:$name\nTEL:$phone\nEMAIL:$email\nEND:VCARD".toByteArray(Charsets.UTF_8))))
    fun writeAppLaunch(tag: Tag, packageName: String) = writeRecords(tag, arrayOf(NdefRecord.createApplicationRecord(packageName)))
    fun writeSmartPoster(tag: Tag, title: String, url: String): Result<Unit> {
        val urlRecord = NdefRecord.createUri(url)
        val titleBytes = title.toByteArray(Charsets.UTF_8); val lang = "en".toByteArray(Charsets.US_ASCII)
        val tp = ByteArray(1 + lang.size + titleBytes.size); tp[0] = lang.size.toByte(); System.arraycopy(lang, 0, tp, 1, lang.size); System.arraycopy(titleBytes, 0, tp, 1 + lang.size, titleBytes.size)
        val titleRecord = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), tp)
        val innerMsg = NdefMessage(arrayOf(urlRecord, titleRecord))
        val spRecord = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_SMART_POSTER, ByteArray(0), innerMsg.toByteArray())
        return writeRecords(tag, arrayOf(spRecord))
    }

    fun writeMultipleRecords(tag: Tag, records: List<NdefRecord>) = writeMessage(tag, NdefMessage(records.toTypedArray()))

    fun writeMessage(tag: Tag, message: NdefMessage): Result<Unit> {
        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) { ndef.close(); return Result.failure(IOException("Read-only")) }
                if (message.toByteArray().size > ndef.maxSize) { ndef.close(); return Result.failure(IOException("Exceeds ${ndef.maxSize}B capacity")) }
                ndef.writeNdefMessage(message); ndef.close(); Result.success(Unit)
            } else {
                val fmt = NdefFormatable.get(tag)
                if (fmt != null) { fmt.connect(); fmt.format(message); fmt.close(); Result.success(Unit) }
                else Result.failure(IOException("No NDEF support"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    fun lockTag(tag: Tag): Result<Unit> {
        return try {
            val ndef = Ndef.get(tag) ?: return Result.failure(IOException("No NDEF"))
            ndef.connect()
            if (!ndef.isWritable) { ndef.close(); return Result.failure(IOException("Already locked")) }
            val ok = ndef.makeReadOnly(); ndef.close()
            if (ok) Result.success(Unit) else Result.failure(IOException("Lock failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    fun eraseTag(tag: Tag) = writeMessage(tag, NdefMessage(arrayOf(NdefRecord(NdefRecord.TNF_EMPTY, ByteArray(0), ByteArray(0), ByteArray(0)))))

    private fun writeRecords(tag: Tag, records: Array<NdefRecord>) = writeMessage(tag, NdefMessage(records))

    private val URI_PREFIXES = mapOf(0x00 to "", 0x01 to "http://www.", 0x02 to "https://www.", 0x03 to "http://", 0x04 to "https://", 0x05 to "tel:", 0x06 to "mailto:")
}
