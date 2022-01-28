package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class CSKFTPFilePacket extends Packet {
    public final String path;
    public final byte[] data;
    public CSKFTPFilePacket(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }

    @Override
    public boolean isServer() {
        return false;
    }
}