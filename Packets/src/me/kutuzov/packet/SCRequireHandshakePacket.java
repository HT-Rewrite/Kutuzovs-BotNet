package me.kutuzov.packet;

public class SCRequireHandshakePacket extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }
}