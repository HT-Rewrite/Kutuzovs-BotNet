package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class SCKFTPStateFilePacket extends Packet {
    public final String path;
    public SCKFTPStateFilePacket(String path) {
        this.path = path;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}