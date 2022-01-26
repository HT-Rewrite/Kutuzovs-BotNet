package me.kutuzov.packet;

public class SCBeepPacket extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }
}