package me.kutuzov.packet.kftp;

import me.kutuzov.packet.Packet;

public class SCKFTPHandshakePacket extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }
}