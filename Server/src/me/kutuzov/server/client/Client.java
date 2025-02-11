package me.kutuzov.server.client;

import me.kutuzov.packet.Packet;
import me.kutuzov.server.KutuzovEntry;
import me.kutuzov.server.util.ActionQueue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Client {
    public boolean valid;
    public AtomicLong lastMS;
    private Socket socket;
    private String ip, identifierName, os, localIp, version;
    private boolean isMC;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private AtomicBoolean reading;
    public ActionQueue actionQueue;
    public Client(Socket socket) {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        this.localIp = "Unknown";
        this.identifierName = ip;
        this.version = "Unknown";
        this.os = "Unknown";
        this.isMC = false;
        this.lastMS = new AtomicLong(System.currentTimeMillis());
        this.reading = new AtomicBoolean(false);
        this.actionQueue = new ActionQueue();

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            valid = true;
        } catch (Exception e) {
            //e.printStackTrace();

            valid = false;
        }
    }

    public void add(Runnable runnable) {
        actionQueue.add(runnable);
    }

    public void addWait(Runnable action) {
        actionQueue.addWait(action);
    }

    public boolean isReading() { return false; }
    public void setReading(boolean state) { reading.set(state); }

    public Socket getConnection() { return socket; }
    public String getIp() { return ip; }

    public void   sendPacket(Packet packet) throws IOException {
        getCOutput().writeObject(packet);
    }
    public Packet readPacket() {
        Packet packet = null;
        try {
            setReading(true);
            packet = (Packet) getCInput().readObject();
        } catch (Exception exception) {
            exception.printStackTrace();
            if(!getConnection().isClosed())
                try { getConnection().close(); } catch (Exception e) {}
            KutuzovEntry.SERVER.clientManager.clients.remove(ip);
            KutuzovEntry.SERVER.pythonClientManager.clients.remove(ip);
        }

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
