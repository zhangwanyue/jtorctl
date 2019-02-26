## Intro

A Java library for controlling a Tor instance via its control port.  It is
used in the Android app Orbot as well as others.

This fork includes patches for managing onion services.

## Fork And Modify

This is fork from: https://github.com/akwizgran/jtorctl

We add `TorControlConnection.addOnionV3()` to support hidden service v3, and `serverAndClientExample package` as a hidden service example.

## Fat Jar

### How To Compile Fat Jar

Run`./gradlew jar`

### How To Use Fat Jar (Fat Jar Contains a Hidden Service Test Case)

#### Simply testing fat jar's test case in command line

1. Make sure your Tor process is running on your computer **as a client**: you can use `sudo systemctl status tor` to check whether it is runnning and check the log: `/var/log/tor/notice.log` to see whether it got `Bootstrapped 100%`;

2. Make sure you have opened the `ControlPort` at 9051 and `SocksPort` at 9050 in `torrc` file, and enabled the `CookieAuthentication`:

```
SocksPort 9050
ControlPort 9051
CookieAuthentication 1
```

3. Use `sudo chmod 777 /var/run/tor/control.authcookie` to change the permission of cookie file;

4. Simply run `java -jar jtorctl-0.4.jar`.

```
$ java -jar jtorctl-0.4.jar 

>> ADD_ONION NEW:ED25519-V3 Port=80,127.0.0.1:8080
<< 250-ServiceID=v4qdqxeyi734lo7p25grvpl6gydzlflz2h5ue4tkdvpld5md7kc2h5qd
<< 250-PrivateKey=ED25519-V3:wJNUN7zA5xtdWv86+aSh1RuwWfWqz9UQXhMgnsX+TXzbTgDEG15Xyc4C0cDiiD1DCnOgruNYvGP90QzCL/Z6Iw==
<< 250 OK
hiddenServiceAddress: v4qdqxeyi734lo7p25grvpl6gydzlflz2h5ue4tkdvpld5md7kc2h5qd.onion
hiddenServicePrivateKey: ED25519-V3:wJNUN7zA5xtdWv86+aSh1RuwWfWqz9UQXhMgnsX+TXzbTgDEG15Xyc4C0cDiiD1DCnOgruNYvGP90QzCL/Z6Iw==
>> SETEVENTS EXTENDED HS_DESC
<< 250 OK
<< 650 HS_DESC UPLOAD v4qdqxeyi734lo7p25grvpl6gydzlflz2h5ue4tkdvpld5md7kc2h5qd UNKNOWN $793ED35AA169BB4269CD49A31ABE35E05A960C96~sqrrm xJI681BxyV//ELVQbHuPeWwPv3kK4o1N9Xv9RJiivGw HSDIR_INDEX=09316F2336E747310183861C7C4E511FFE2856F6B20289CD4EEFA096936A6E3C
...

Descriptor uploaded
...

[CLIENT] try to connect to server
...

[SERVER] receive connect from client
[SERVER] send a message to client: Hello client
[CLIENT] receive reply from server: Hello client

```


#### Using fat jar as a dependency

1. Make a directory named `libs` in your project and copy this jar into `libs`

2. Add this in `build.gradle`

```groovy
dependencies {
    ...
    implementation files('libs/jtorctl-0.4.jar')
}
```

## Publish

### How To Publish

1. modify xxx below to a url

```groovy
publishing{
     ...
     repositories{
         maven{
              name = 'jtorctl'
              url = "xxx" //发布到本地
         }
     }
}
```

2. publish

Run `./gradlew publish`

### How To Use Published Archive As A Dependency

```groovy
repositories {
    ...
    maven{
        url uri('xxx')
    }
}

dependencies {
    ...
    implementation group: 'com.buptnsrc', 'name': 'jtorctl', 'version':'0.4'
}
```



