# Flutter NFC plugin

The Flutter NFC plugin allows you to read NFC state and tags

## Supported Platforms

* Android

## Installing

Add to pubspec.yaml:

```
dependencies:
  flutter_nfc_reader:
    git:
      url: git://github.com/dukhanov/flutter-nfc-plugin.git
      ref: master
```

Add NFC permissions to Android package's manifest file:

```
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" />
```

To be able to start the app within an NFC tag scanned
Add such intent filter to Android package's manifest file inside of the Main activity section:

```
<intent-filter>
    <action android:name="android.nfc.action.TECH_DISCOVERED" />
</intent-filter>
<intent-filter>
    <action android:name="android.nfc.action.NDEF_DISCOVERED" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:scheme="*" />
</intent-filter>
```

And `nfc_tech_filter.xml` to the `android/app/src/main/res/xml` folder (create if not exist). See `example`

<description>

```
<resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
    <tech-list>
        <tech>android.nfc.tech.NfcA</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NfcB</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NfcBarcode</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NfcF</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NfcV</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.IsoDep</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.MifareClassic</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.MifareUltralight</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.Ndef</tech>
    </tech-list>
    <tech-list>
        <tech>android.nfc.tech.NdefFormatable</tech>
    </tech-list>
</resources>
```

</description>

Add such metadata to Android package's manifest file inside of the Main activity section:
```
<meta-data android:name="android.nfc.action.TECH_DISCOVERED" android:resource="@xml/nfc_tech_filter" />
```

## Example of usage

### Check NFC state:

```
NfcPlugin nfcPlugin = NfcPlugin();
try {
  final NfcState _nfcState = await nfcPlugin.nfcState;
  print('NFC state is $_nfcState');
} on PlatformException {
  print('Method "NFC state" exception was thrown');
}

```

### Check NFC message which was used to start the app:

```
NfcPlugin nfcPlugin = NfcPlugin();
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

```

### Read NFC tags subscribe:

```
NfcPlugin nfcPlugin = NfcPlugin();
nfcPlugin.onNfcMessage.listen((NfcEvent event) {
if (event.error.isNotEmpty) {
  print('NFC read error: ${event.error}');
} else {
  print('NFC read successful');
  print(event.message.payload.toString());
  print(event.message.id);
});

```

**See more at**: [example project](example/lib/main.dart)