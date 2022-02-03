package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class SCKFTPChangeDirectoryPacket extends Packet {
    public final String directory;
    public SCKFTPChangeDirectoryPacket(String directory) {
        this.directory = directory;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}