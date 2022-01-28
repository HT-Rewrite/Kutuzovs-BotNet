package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class SCKFTPDownloadFilePacket extends Packet {
    public final String fileName;
    public SCKFTPDownloadFilePacket(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}