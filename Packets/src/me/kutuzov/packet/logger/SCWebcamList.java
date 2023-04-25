package me.kutuzov.packet.logger;

import me.kutuzov.packet.Packet;

public class SCWebcamList extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }
}