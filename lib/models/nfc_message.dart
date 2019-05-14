class NfcMessage {
  NfcMessage({
    this.id,
    this.payload,
    this.techList,
  });

  factory NfcMessage.fromMap(Map<dynamic, dynamic> data) => NfcMessage(
        id: data['id'],
        payload: data['payload']?.cast<String>(),
        techList: data['techList']?.cast<String>(),
      );

  String id;
  List<String> payload;
  List<String> techList;
}
