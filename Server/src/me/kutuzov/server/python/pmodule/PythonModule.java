package me.kutuzov.server.python.pmodule;

import me.kutuzov.packet.python.CSPythonStatusPacket;
import me.kutuzov.server.client.Client;

public interface PythonModule<R> {
    R run(Client client);
}