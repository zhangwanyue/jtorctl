A Java library for controlling a Tor instance via its control port.  It is
used in the Android app Orbot as well as others.

This fork includes patches for managing onion services.

### modify

**add `TorControlConnection.addOnionV3()` and `serverAndClientExample package` to jtorctl**

### how to compile fat jar

run`./gradlew jar`