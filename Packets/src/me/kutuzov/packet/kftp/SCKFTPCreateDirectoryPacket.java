package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class SCKFTPCreateDirectoryPacket extends Packet {
    public final String directory;
    public SCKFTPCreateDirectoryPacket(String directory) {
        this.directory = directory;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}