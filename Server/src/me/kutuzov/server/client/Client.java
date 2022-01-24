package me.kutuzov.server.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private Socket socket;
    private String ip, identifierName, os;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    public Client(Socket socket) {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        this.identifierName = ip;
        this.os = "Unknown";

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

    public ObjectInputStream  getInput () { return ois; }
    public ObjectOutputStream getOutput() { return oos; }
}