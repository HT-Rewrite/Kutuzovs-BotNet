package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class SCKFTPDeleteFilePacket extends Packet {
    public final String path;
    public SCKFTPDeleteFilePacket(String path) {
        this.path = path;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}