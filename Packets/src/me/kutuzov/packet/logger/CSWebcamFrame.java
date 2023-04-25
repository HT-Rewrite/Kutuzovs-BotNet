package me.kutuzov.packet.logger;

import me.kutuzov.packet.Packet;

public class CSWebcamFrame extends Packet {
    public byte[] bytes;

    @Override
    public boolean isServer() {
        return false;
    }

    public CSWebcamFrame(byte[] bytes) {
        this.bytes = bytes;
    }
}