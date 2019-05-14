import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_nfc_plugin/models/nfc_event.dart';
import 'package:flutter_nfc_plugin/models/nfc_message.dart';
import 'package:flutter_nfc_plugin/models/nfc_state.dart';
import 'package:flutter_nfc_plugin/nfc_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String nfcState = 'Unknown';
  String nfcError = '';
  String nfcMessage = '';
  String nfcTechList = '';
  String nfcId = '';
  NfcMessage nfcMessageStartedWith;

  NfcPlugin nfcPlugin = NfcPlugin();
  StreamSubscription<NfcEvent> _nfcMesageSubscription;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    NfcState _nfcState;

    try {
      _nfcState = await nfcPlugin.nfcState;
      print('NFC state is $_nfcState');
    } on PlatformException {
      print('Method "NFC state" exception was thrown');
    }

    try {
      final NfcEvent _nfcEventStartedWith = await nfcPlugin.nfcStartedWith;
      print('NFC event started with is ${_nfcEventStartedWith.toString()}');
      if (_nfcEventStartedWith != null) {
        setState(() {
          nfcMessageStartedWith = _nfcEventStartedWith.message;
        });
      }
    } on PlatformException {
      print('Method "NFC event started with" exception was thrown');
    }

    if (_nfcState == NfcState.enabled) {
      _nfcMesageSubscription = nfcPlugin.onNfcMessage.listen((NfcEvent event) {
        if (event.error.isNotEmpty) {
          setState(() {
            nfcMessage = 'ERROR: ${event.error}';
            nfcId = '';
          });
        } else {
          setState(() {
            nfcMessage = event.message.payload.toString();
            nfcTechList = event.message.techList.toString();
            nfcId = event.message.id;
          });
        }
      });
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      nfcState = _nfcState.toString();
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Flutter NFC plugin example app'),
        ),
        body: Padding(
            padding: EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text('NFC state: $nfcState\n'),
                Text('NFC message: $nfcMessage\n'),
                Text('NFC tech list: $nfcTechList\n'),
                Text('NFC id: $nfcId\n'),
                Text(
                    'The app was started with an NFC: ${nfcMessageStartedWith?.id}')
              ],
            )),
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
    if (_nfcMesageSubscription != null) {
      _nfcMesageSubscription.cancel();
    }
  }
}
