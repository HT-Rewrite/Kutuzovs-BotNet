package me.kutuzov.server.python;

import me.kutuzov.packet.python.CSPythonStatusPacket;
import me.kutuzov.packet.python.SCPythonStatusPacket;
import me.kutuzov.packet.python.status.InstalledStatus;
import me.kutuzov.server.client.Client;

import java.util.concurrent.atomic.AtomicReference;

public class PythonClient {
    public final Client client;
    public InstalledStatus installed = InstalledStatus.UNKNOWN;
    public String path = "";
    public PythonClient(Client client) {
        this.client = client;
    }

    public void update() {
        client.addWait(this::updateNoQueue);
    }
    public void updateNoQueue() {
        CSPythonStatusPacket status = statusNoQueue();
        if(status != null) {
            this.installed = status.installed;
            this.path = status.path;
        }
    }

    public CSPythonStatusPacket statusNoQueue() {
        try {
            client.sendPacket(new SCPythonStatusPacket());
            return (CSPythonStatusPacket)client.readPacket();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }
}