### intro

A Java library for controlling a Tor instance via its control port.  It is
used in the Android app Orbot as well as others.

This fork includes patches for managing onion services.

### fork and modify

This is fork from: https://github.com/akwizgran/jtorctl

We add `TorControlConnection.addOnionV3()` to support hidden service v3, and `serverAndClientExample package` as a hidden service example.

### how to compile fat jar

run`./gradlew jar`