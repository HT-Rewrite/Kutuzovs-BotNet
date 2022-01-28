package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class SCKFTPListDirectoryPacket extends Packet {
    public final String directory;
    public SCKFTPListDirectoryPacket(String directory) {
        this.directory = directory;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}