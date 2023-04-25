package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class CSKFTPStateFilePacket extends Packet {
    public final String path;
    public final boolean state;
    public CSKFTPStateFilePacket(String path, boolean state) {
        this.path = path;
        this.state = state;
    }

    @Override
    public boolean isServer() {
        return false;
    }
}