import 'dart:async';

import 'package:flutter/services.dart';
import 'package:nfc_plugin/models/nfc_event.dart';
import 'package:nfc_plugin/models/nfc_state.dart';

class NfcPlugin {
  factory NfcPlugin() {
    if (_instance == null) {
      final MethodChannel methodChannel =
          MethodChannel('nfc_plugin_method_channel');
      final EventChannel eventChannel =
          EventChannel('nfc_plugin_event_channel');
      _instance = NfcPlugin.private(methodChannel, eventChannel);
    }
    return _instance;
  }

  NfcPlugin.private(this._methodChannel, this._eventChannel);

  static NfcPlugin _instance;

  final MethodChannel _methodChannel;
  final EventChannel _eventChannel;
  Stream<NfcEvent> _onNfcMessage;

  /// Check if NFC is enabled
  Future<NfcState> get nfcState => _methodChannel
      .invokeMethod<String>('getNfcState')
      .then<NfcState>((String result) => parseNfcState(result));

  /// Check if the app was started with NFC
  /// and get NFC message started with
  Future<NfcEvent> get nfcStartedWith =>
      _methodChannel.invokeMethod<dynamic>('getNfcStartedWith').then<NfcEvent>(
          (dynamic event) => event != null ? NfcEvent.fromMap(event) : null);

  /// Fires whenever the nfc message received.
  Stream<NfcEvent> get onNfcMessage {
    _onNfcMessage ??= _eventChannel
        .receiveBroadcastStream()
        .map((dynamic event) => NfcEvent.fromMap(event));
    return _onNfcMessage;
  }
}
