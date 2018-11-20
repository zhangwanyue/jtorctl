package net.freehaven.tor.control.serverAndClientExample;

import net.freehaven.tor.control.EventHandler;
import net.freehaven.tor.control.TorControlConnection;
import net.freehaven.tor.control.serverAndClientExample.utils.IoUtils;

//【方法2】使用自己写的socks5Socket
import net.freehaven.tor.control.serverAndClientExample.socks.SocksSocket;

//【方法３】使用 jsocks.jar 中的 Socks5Proxy 和 SocksSocket
//import socks.Socks5Proxy;
//import socks.SocksSocket;

import java.io.*;
import java.net.*;
import java.util.*;


/**
 * Created by vera on 18-11-20.
 */
public class TorTest implements EventHandler {
    // the default control_port is 9051
    private static int CONTROL_PORT = 9051;

    // the default tor proxy port is 9050
    private static int SOCKS_PORT = 9050;

    // hidden service's virtual tcp port(remote client call this port to connect to server's hidden service)
    private static int HIDDENSERVICE_VIRTUAL_PORT = 80;

    // the target port for the given virtual port(server's hidden service open this port to listen for client's connect)
    private static int HIDDENSERVICE_TARGET_PORT = 8080;

    // the default tor control authcookie default file location is: /var/run/tor/control.authcookie
    // remember to make this authcookie file readable
    private static final String AUTHCOOKIE_FILE = "/var/run/tor/control.authcookie";

    private static final String HS_ADDRESS_STRING = "onionAddress";
    private static final String HS_PRIVKEY_STRING = "onionPrivKey";

    private static final int CONNECT_TO_PROXY_TIMEOUT = 50000; // Milliseconds
    private static final int EXTRA_SOCKET_TIMEOUT = 300000; // Milliseconds

    //    Request that the server inform the client about interesting events
    public static final String[] EVENTS = {
            "CIRC", "ORCONN", "HS_DESC", "NOTICE", "WARN", "ERR", "STATUS_SERVER", "STATUS_GENERAL", "STATUS_CLIENT"
    };

    protected TorControlConnection torControlConnection = null;
    //    private String hiddenServicePrivateKey = "ED25519-V3:qO25ZIERKpVqyCZ3dlsNAlqJ/sQ2LNU2ZtuM0dRepEERYBeYFDaxAKWYIEDmv9AZvNPWaZd3gU3kQQH9bUqNJA==";
    private String hiddenServicePrivateKey = null;
    private String hiddenServiceAddress = null;

    private ServerSocket serverSocket;

    private volatile boolean isDesUploaded = false;


    public void testServerAndClient(){
        bindToLocalPort();
        publishHiddenService();
        new Thread(new Runnable() {
            public void run() {
                acceptClientConnect();
            }
        }).start();

        // setEvents之后才能获取HS_DESC的通知
        ControlPortOperation.setEvents(torControlConnection, this, Arrays.asList("HS_DESC"));
        // 这里需要等待hidden service descriptor uploaded
        while(!isDesUploaded) {
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        new Thread(new Runnable() {
            public void run() {
                connectToRemote();
            }
        }).start();
    }


    public void connectToControlPort(){
        if(torControlConnection == null) {
            Socket s = null;
            try {
                s = new Socket("127.0.0.1", CONTROL_PORT);
                torControlConnection = new TorControlConnection(s);
                torControlConnection.authenticate(IoUtils.read(new File(AUTHCOOKIE_FILE)));
                torControlConnection.setDebugging(new PrintWriter(System.out, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void bindToLocalPort(){
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.bind(new InetSocketAddress("127.0.0.1", HIDDENSERVICE_TARGET_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocket = ss;
    }

    public void acceptClientConnect(){
        Socket clientSocket = null;
        PrintWriter out = null;
        try {
            clientSocket = serverSocket.accept();
            System.out.println("[SERVER] receive connect from client");
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            String message = "Hello client";
            out.println(message);
            System.out.println("[SERVER] send a message to client: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IoUtils.tryToClose(clientSocket);
            IoUtils.tryToClose(out);
        }
    }

    public void connectToRemote(){
        // 【方法１】
        // 使用java.net.Socket和java.net.InetSocketAddress。需要解析.onion域名，要使用InetSocketAddress.createUnresolved方法，远程解析域名
        // 但是该方法在android中会遇到 java.net.UnknownHostException: Host is unresolved 的异常
        // 关于该问题：https://stackoverflow.com/questions/39308705/android-how-to-let-tor-service-to-perform-the-dns-resolution-using-socket
        // 原因是：Android will probably perform DNS resolution via DNS server specified in network configuration and the resolution of onion address will not work.
//        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", SOCKS_PORT));
//        Socket socks5Socket  = new Socket(proxy);
//        BufferedReader in = null;
//        try {
//            System.out.println("[CLIENT] try to connect to server");
//            socks5Socket.connect(InetSocketAddress.createUnresolved(hiddenServiceAddress, HIDDENSERVICE_VIRTUAL_PORT));
//            in = new BufferedReader(new InputStreamReader(socks5Socket.getInputStream()));
//            System.out.println("[CLIENT] receive reply from server: " + in.readLine());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            IoUtils.tryToClose(in);
//            IoUtils.tryToClose(socks5Socket);
//        }

        //【方法2】
        // 使用自己写的socks5Socket
        InetSocketAddress proxy = new InetSocketAddress("127.0.0.1", SOCKS_PORT);
        SocksSocket socks5Socket =  new SocksSocket(proxy, CONNECT_TO_PROXY_TIMEOUT, EXTRA_SOCKET_TIMEOUT);
        BufferedReader in = null;
        try {
            System.out.println("[CLIENT] try to connect to server");
            socks5Socket.connect(InetSocketAddress.createUnresolved(hiddenServiceAddress, HIDDENSERVICE_VIRTUAL_PORT));
            in = new BufferedReader(new InputStreamReader(socks5Socket.getInputStream()));
            System.out.println("[CLIENT] receive reply from server: " + in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IoUtils.tryToClose(in);
            IoUtils.tryToClose(socks5Socket);
        }

        //【方法３】
        // 使用 jsocks.jar 中的 Socks5Proxy 和 SocksSocket
        // https://stackoverflow.com/questions/19810179/android-socket-host-is-unresolved-onion
//        SocksSocket socks5Socket = null;
//        BufferedReader in = null;
//        try {
//            System.out.println("[CLIENT] try to connect to server");
//            Socks5Proxy socks5Proxy = new Socks5Proxy("127.0.0.1", SOCKS_PORT);
//            socks5Proxy.resolveAddrLocally(false);
//            socks5Socket = new SocksSocket(socks5Proxy, hiddenServiceAddress, HIDDENSERVICE_VIRTUAL_PORT);
//            in = new BufferedReader(new InputStreamReader(socks5Socket.getInputStream()));
//            System.out.println("[CLIENT] receive reply from server: " + in.readLine());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            IoUtils.tryToClose(in);
//            IoUtils.tryToClose(socks5Socket);
//        }
    }

    public void publishHiddenService(){
        if(torControlConnection == null)
            connectToControlPort();
        Map<Integer, String> portLines =
                Collections.singletonMap(HIDDENSERVICE_VIRTUAL_PORT, "127.0.0.1:" + HIDDENSERVICE_TARGET_PORT);
        Map<String, String> response;
        try {
            if(hiddenServicePrivateKey == null) {
                response = torControlConnection.addOnionV3(portLines);
            } else {
                response = torControlConnection.addOnion(hiddenServicePrivateKey, portLines);
            }
            if(!response.containsKey(HS_ADDRESS_STRING)){
                System.out.println("Tor did not return a hidden service address");
                return;
            }
            if (hiddenServicePrivateKey == null && !response.containsKey(HS_PRIVKEY_STRING)) {
                System.out.println("Tor did not return a private key");
                return;
            }
            hiddenServiceAddress = response.get(HS_ADDRESS_STRING) + ".onion";
            if(hiddenServicePrivateKey == null)
                hiddenServicePrivateKey = response.get(HS_PRIVKEY_STRING);
            try {
                Thread.sleep(2*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("hiddenServiceAddress: " + hiddenServiceAddress);
            System.out.println("hiddenServicePrivateKey: " + hiddenServicePrivateKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void streamStatus(String status, String id, String target) {
    }

    public void newDescriptors(List<String> orList) {
    }

    public void bandwidthUsed(long read, long written) {
    }

    public void circuitStatus(String status, String circID, String path) {
        System.out.println("Circuit "+circID+" is now "+status+" (path="+path+")");
    }

    public void message(String severity, String msg) {
        System.out.println(severity + " " + msg);
    }

    public void orConnStatus(String status, String orName) {
        System.out.println("OR connection " + status + " " + orName);
        if (status.equals("CLOSED") || status.equals("FAILED")) {
            // Check whether we've lost connectivity
            System.out.println("we lost connectivity");
            // this should have an updateConn
        }
    }

    public void unrecognized(String type, String msg) {
        if (type.equals("HS_DESC") && msg.startsWith("UPLOADED")) {
            System.out.println("Descriptor uploaded");
            isDesUploaded = true;
        }
    }
}
