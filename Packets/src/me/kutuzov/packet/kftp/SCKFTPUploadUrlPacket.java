package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class SCKFTPUploadUrlPacket extends Packet {
    public final String url;
    public final String path;
    public SCKFTPUploadUrlPacket(String url, String path) {
        this.url = url;
        this.path = path;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}