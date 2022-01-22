package me.kutuzov.packet;

public class SCDAPacket extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    public final String data;
    public SCDAPacket(String data) {
        this.data = data;
    }
}