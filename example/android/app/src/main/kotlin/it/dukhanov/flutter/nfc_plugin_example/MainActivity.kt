package it.dukhanov.flutter.nfc_plugin_example

import android.content.Intent
import android.os.Bundle
import android.util.Log

import io.flutter.app.FlutterActivity
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity: FlutterActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.w("flutterNfc", "onCreate")
    GeneratedPluginRegistrant.registerWith(this)
  }
}
