### intro

A Java library for controlling a Tor instance via its control port.  It is
used in the Android app Orbot as well as others.

This fork includes patches for managing onion services.

### fork and modify

This is fork from: https://github.com/akwizgran/jtorctl

We add `TorControlConnection.addOnionV3()` to support hidden service v3, and `serverAndClientExample package` as a hidden service example.

### how to compile fat jar

Run`./gradlew jar`

### how to use

#### simply using jar in command line

1. Make sure your Tor process is running on your computer **as a client**: you can use `sudo systemctl status tor` to check whether it is runnning and check the log: `/var/log/tor/notice.log` to see whether it got `Bootstrapped 100%`;

2. Make sure you have opened the `ControlPort` at 9051 and `SocksPort` at 9050 in `torrc` file, and enabled the `CookieAuthentication`:

```
SocksPort 9050
ControlPort 9051
CookieAuthentication 1
```

3. Use `sudo chmod 777 /var/run/tor/control.authcookie` to change the permission of cookie file;

4. Simply run `java -jar jtorctl-0.4.jar`.

#### Using the jar as a dependency

1. Make a file named `libs` in your project and copy this jar into `libs`

2. Add this in `build.gradle`

```groovy
dependencies {
    ...
    implementation files('libs/jtorctl-0.4.jar')
}
```