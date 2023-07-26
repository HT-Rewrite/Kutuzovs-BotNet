package me.kutuzov.packet.python;

import me.kutuzov.packet.Packet;

public class CSPythonCommandPacket extends Packet {
    public final String response;
    public CSPythonCommandPacket(String response) {
        this.response = response;
    }

    @Override
    public boolean isServer() {
        return false;
    }
}