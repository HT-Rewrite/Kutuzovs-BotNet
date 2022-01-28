package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class SCKFTPStartUploadPacket extends Packet {
    public final long size;
    public SCKFTPStartUploadPacket(long size) {
        this.size = size;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}