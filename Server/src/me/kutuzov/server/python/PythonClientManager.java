package me.kutuzov.server.python;

import me.kutuzov.server.client.Client;

import java.util.concurrent.ConcurrentHashMap;

public class PythonClientManager {
    public final ConcurrentHashMap<String, PythonClient> clients;
    public PythonClientManager() {
        clients = new ConcurrentHashMap<>();
    }

    public PythonClient cache(Client client) {
        if(clients.containsKey(client.getIp()))
            return clients.get(client.getIp());

        PythonClient pythonClient = new PythonClient(client);
        clients.put(client.getIp(), pythonClient);
        return pythonClient;
    }
}