package me.kutuzov.packet;

public class SCKeepAlivePacket extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }
}