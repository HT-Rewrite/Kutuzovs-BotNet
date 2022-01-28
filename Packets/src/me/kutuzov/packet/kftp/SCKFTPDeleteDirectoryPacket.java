package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class SCKFTPDeleteDirectoryPacket extends Packet {
    public final String path;
    public SCKFTPDeleteDirectoryPacket(String path) {
        this.path = path;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}