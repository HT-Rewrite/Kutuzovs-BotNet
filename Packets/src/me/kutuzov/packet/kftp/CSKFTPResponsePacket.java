package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class CSKFTPResponsePacket extends Packet {
    public static final int RESPONSE_OK    = 1;
    public static final int RESPONSE_ERROR = 0;

    public final int response;
    public CSKFTPResponsePacket(int response) {
        this.response = response;
    }

    @Override
    public boolean isServer() {
        return false;
    }
}