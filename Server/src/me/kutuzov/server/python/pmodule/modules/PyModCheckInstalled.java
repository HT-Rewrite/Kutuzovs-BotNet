package me.kutuzov.server.python.pmodule.modules;

import me.kutuzov.packet.python.CSPythonStatusPacket;
import me.kutuzov.packet.python.SCPythonStatusPacket;
import me.kutuzov.server.client.Client;
import me.kutuzov.server.python.pmodule.PythonModule;

import java.util.concurrent.atomic.AtomicReference;

public class PyModCheckInstalled implements PythonModule<CSPythonStatusPacket> {
    @Override
    public CSPythonStatusPacket run(Client client) {
        AtomicReference<CSPythonStatusPacket> status = new AtomicReference<>(null);
        client.addWait(() -> {
            try {
                client.sendPacket(new SCPythonStatusPacket());
                status.set((CSPythonStatusPacket) client.readPacket());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        return status.get();
    }
}