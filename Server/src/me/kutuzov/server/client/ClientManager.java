package me.kutuzov.server.client;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {
    public final ConcurrentHashMap<String, Client> clients;
    public ClientManager() {
        clients = new ConcurrentHashMap<>();
    }

    public Client newClient(Socket socket) {
        Client client = new Client(socket);
        clients.put(client.getIp(), client);

        return client;
    }
}