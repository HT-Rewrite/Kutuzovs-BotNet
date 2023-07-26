package me.kutuzov.packet.python;

import me.kutuzov.packet.Packet;

public class CSPythonInstallPacket extends Packet {
    public final boolean done;
    public CSPythonInstallPacket(boolean done) {
        this.done = done;
    }

    @Override
    public boolean isServer() {
        return false;
    }
}