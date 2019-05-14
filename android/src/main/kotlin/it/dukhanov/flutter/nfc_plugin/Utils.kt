package it.dukhanov.flutter.nfc_plugin

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import java.lang.Exception
import java.nio.charset.Charset
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
		val ndef = Ndef.get(tag)
		val id = bytesToString(tag?.id)
		val techList = tag?.techList?.toList()
		val payload = getPayloadMessages(ndef.cachedNdefMessage)
		val message = mapOf(KEY_ID to id, KEY_PAYLOAD to payload, KEY_TECH_LIST to techList)

		mapOf(KEY_ERROR to "", KEY_MESSAGE to message)
	} catch (e: Exception) {
		mapOf(KEY_ERROR to "Cannot parse NDEF message: $e", KEY_MESSAGE to null)
	}
}

fun getPayloadMessages(message: NdefMessage?): List<String> {
	var payload = listOf(String())
	if (message == null) return payload
	payload = message.records.map { r -> recordToString(r) }
	return payload
}

fun recordToString(record: NdefRecord): String {
	if (record.toUri() != null) {
		return record.toUri().toString()
	}

	val payload = record.payload
	val languageCodeLength = (payload[0] and 51) + 1
	return String(payload, languageCodeLength, payload.size - languageCodeLength, Charset.forName("UTF-8"))
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