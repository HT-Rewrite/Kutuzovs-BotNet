package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class SCKFTPUploadUrlPacket extends Packet {
    public final String url;
    public SCKFTPUploadUrlPacket(String url) {
        this.url = url;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}