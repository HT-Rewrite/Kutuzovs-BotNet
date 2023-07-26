package me.kutuzov.packet.python;

import me.kutuzov.packet.Packet;

public class SCPythonInstallPacket extends Packet {
    public final String mirror;
    public SCPythonInstallPacket(String mirror) {
        this.mirror = mirror;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}