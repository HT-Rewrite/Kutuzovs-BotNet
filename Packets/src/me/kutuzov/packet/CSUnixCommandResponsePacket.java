package me.kutuzov.packet;

public class CSUnixCommandResponsePacket extends Packet {
    public final String response;
    public CSUnixCommandResponsePacket(String response) {
        this.response = response;
    }

    @Override
    public boolean isServer() {
        return false;
    }
}