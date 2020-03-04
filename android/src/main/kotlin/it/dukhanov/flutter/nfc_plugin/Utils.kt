package it.dukhanov.flutter.nfc_plugin

import android.content.Intent
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import kotlin.experimental.and

private const val KEY_ERROR = "error"
private const val KEY_MESSAGE = "message"
private const val KEY_ID = "id"
private const val KEY_PAYLOAD = "payload"
private const val KEY_TECH_LIST = "techList"
const val NFC_STATE_ENABLED = "enabled"
const val NFC_STATE_DISABLED = "disabled"
const val NFC_STATE_NOT_SUPPORTED = "notSupported"

fun bytesToString(bytes: ByteArray?): String {
	if (bytes == null) return ""
	return bytes.joinToString("") {
		String.format("%02x", it)
	}
}

fun ndefToMap(tag: Tag?): Map<String, Any?> {
	return try {
		val id = bytesToString(tag?.id)
		val techList = tag?.techList?.toList()
		var payload = getPayloadMessages(tag)
		val message = mapOf(
				KEY_ID to id,
				KEY_PAYLOAD to payload,
				KEY_TECH_LIST to techList
		)

		mapOf(KEY_ERROR to "", KEY_MESSAGE to message)
	} catch (e: Exception) {
		mapOf(KEY_ERROR to "Cannot parse NDEF message: $e", KEY_MESSAGE to null)
	}
}

fun getPayloadMessages(tag: Tag?): List<String>? {
	if (tag == null) {
		return null
	}
	val ndef = Ndef.get(tag)
	if (ndef == null || ndef.cachedNdefMessage == null) {
		return null
	}
	return ndef.cachedNdefMessage.records.map { r -> recordToString(r) }
}

fun recordToString(record: NdefRecord): String {
	if (record.tnf == NdefRecord.TNF_EMPTY) {
		return ""
	}

	if (record.toUri() != null) {
		return record.toUri().toString()
	}

	try {
		val payload = record.payload
		val charset = if ((payload[0] and 0x080.toByte()) === 0.toByte())  Charsets.UTF_8 else Charsets.UTF_16

		if (record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type contentEquals NdefRecord.RTD_TEXT) {
			val languageLength: Int = (payload[0] and 0x3f).toInt() + 1
			return String(payload, languageLength, payload.size - languageLength, charset)
		}

		return String(payload, charset)
	} catch (e: java.lang.Exception) {
		Log.e(PLUGIN_TAG, "Cannot parse payload message $e")
	}
	return ""
}

fun getNfcState(adapter: NfcAdapter?): String {
	return when {
		adapter == null -> NFC_STATE_NOT_SUPPORTED
		adapter.isEnabled -> NFC_STATE_ENABLED
		else -> NFC_STATE_DISABLED
	}
}

fun getNfcStartedWith(intent: Intent): Map<String, Any?>? {
	val action = intent.action ?: return null

	val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
	when (action) {
		NfcAdapter.ACTION_NDEF_DISCOVERED -> {
			val message = ndefToMap(tag)
			Log.d(PLUGIN_TAG, "action ACTION_NDEF_DISCOVERED $message")
			return message
		}
		NfcAdapter.ACTION_TECH_DISCOVERED -> {
			for (tagTech in tag.techList) {
				Log.d(PLUGIN_TAG, "action ACTION_TECH_DISCOVERED")
				if (tagTech == NdefFormatable::class.java.name) {
					val message = ndefToMap(tag)
					Log.d(PLUGIN_TAG, "ACTION_TECH_DISCOVERED NdefFormatable: $message")
					return message
				} else if (tagTech == Ndef::class.java.name) { //
					val message = ndefToMap(tag)
					Log.d(PLUGIN_TAG, "ACTION_TECH_DISCOVERED Ndef: $message")
					return message
				}
			}
		}
		NfcAdapter.ACTION_TAG_DISCOVERED -> {
			val message = ndefToMap(tag)
			Log.d(PLUGIN_TAG, "action ACTION_TAG_DISCOVERED $message")
			return message
		}
	}

	return null
}