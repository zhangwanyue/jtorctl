package net.freehaven.tor.control.serverAndClientExample;

/**
 * Created by vera on 18-11-5.
 */
public class MainLaunch {
    public static void main(String[] args){
        TorTest torTest = new TorTest();

        torTest.connectToControlPort();

        // test control port
//        ControlPortOperation.getConf(torTest.torControlConnection, "HiddenServiceOptions");
//        ControlPortOperation.resetConf(torTest.torControlConnection, Collections.singletonList("Socks5Proxy"));
//        ControlPortOperation.setEvents(torTest.torControlConnection, torTest, Arrays.asList(torTest.EVENTS));
//        ControlPortOperation.getInfo(torTest.torControlConnection, "version");

        torTest.testServerAndClient();

    }
}





