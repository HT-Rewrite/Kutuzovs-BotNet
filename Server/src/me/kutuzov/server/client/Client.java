package me.kutuzov.server.client;

import me.kutuzov.packet.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private Socket socket;
    private String ip, identifierName, os, localIp, version;
    private boolean isMC;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private AtomicBoolean reading;
    public Client(Socket socket) {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        this.localIp = "Unknown";
        this.identifierName = ip;
        this.version = "Unknown";
        this.os = "Unknown";
        this.isMC = false;
        this.reading = new AtomicBoolean(false);

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isReading() { return reading.get(); }
    public void setReading(boolean state) { reading.set(state); }

    public Socket getConnection() { return socket; }
    public String getIp() { return ip; }

    public void   sendPacket(Packet packet) throws IOException { getCOutput().writeObject(packet); }
    public Packet readPacket() throws IOException, ClassNotFoundException {
        while(isReading())
            try { Thread.sleep(1); } catch (Exception exception) {}

        setReading(true);
        Packet packet = (Packet)getCInput().readObject();
        setReading(false);

        return packet;
    }

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

    public ObjectInputStream  getCInput () { return ois; }
    public ObjectOutputStream getCOutput() { return oos; }
}
