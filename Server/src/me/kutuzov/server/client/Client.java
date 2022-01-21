package me.kutuzov.server.client;

import java.net.Socket;

public class Client {
    private Socket socket;
    private String ip;
    public Client(Socket socket) {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    public Socket getSocket() { return socket; }
    public String getIp() { return ip; }
}