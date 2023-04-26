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
        if(client.valid)
            clients.put(client.getIp(), client);

        return client;
    }

    public Client findClientByIdentifier(String identifier) {
        for(Client client : clients.values())
            if(client.getIdentifierName().contentEquals(identifier))
                return client;
        return null;
    }

    public Client findClient(String idorip) {
        for(Client client : clients.values())
            if(client.getIp().contentEquals(idorip))
                return client;
        return findClientByIdentifier(idorip);
    }
}