package me.kutuzov.server.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private Socket socket;
    private String ip, identifierName, os, localIp, version;
    private boolean isMC;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    public Client(Socket socket) {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        this.localIp = "Unknown";
        this.identifierName = ip;
        this.version = "Unknown";
        this.os = "Unknown";
        this.isMC = false;

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() { return socket; }
    public String getIp() { return ip; }

    public String getFormattedIdentifierName() { return identifierName + " (" + ip + ")"; }

    public String getIdentifierName() { return identifierName; }
    public void   setIdentifierName(String identifierName) { this.identifierName = identifierName; }

    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }

    public String getLocalIp() { return localIp; }
    public void setLocalIp(String localIp) { this.localIp = localIp; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public boolean _isMC() { return isMC; }
    public void setMC(boolean isMC) { this.isMC = isMC; }

    public ObjectInputStream  getInput () { return ois; }
    public ObjectOutputStream getOutput() { return oos; }
    
    // TODO: Don't receive packets if already receiving one.
}
