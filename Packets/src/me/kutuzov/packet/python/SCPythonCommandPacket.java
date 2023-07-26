package me.kutuzov.packet.python;

import me.kutuzov.packet.Packet;

public class SCPythonCommandPacket extends Packet {
    public final String path, command;
    public SCPythonCommandPacket(String path, String command) {
        this.path = path;
        this.command = command;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}