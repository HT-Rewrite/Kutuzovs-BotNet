package me.kutuzov.packet.raw;

import me.kutuzov.packet.Packet;

public class SCRawPacket extends Packet {
    public byte[] data;
    public SCRawPacket(byte... data) {
        this.data = data;
    }
    public SCRawPacket(int... data) {
        this.data = new byte[data.length];
        for(int i = 0; i < data.length; i++)
            this.data[i] = (byte) data[i];
    }

    @Override
    public boolean isServer() {
        return true;
    }
}