package me.kutuzov.packet.python;

import me.kutuzov.packet.Packet;

public class SCPythonStatusPacket extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }
}