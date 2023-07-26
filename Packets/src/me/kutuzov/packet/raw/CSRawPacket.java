package me.kutuzov.packet.raw;

import me.kutuzov.packet.Packet;

public class CSRawPacket extends Packet {
    public byte[] data;
    public CSRawPacket(byte... data) {
        this.data = data;
    }
    public CSRawPacket(int... data) {
        this.data = new byte[data.length];
        for(int i = 0; i < data.length; i++)
            this.data[i] = (byte) data[i];
    }

    @Override
    public boolean isServer() {
        return false;
    }
}