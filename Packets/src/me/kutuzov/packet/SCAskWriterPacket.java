package me.kutuzov.packet;

public class SCAskWriterPacket extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }
}