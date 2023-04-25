package me.kutuzov.packet.logger;

import me.kutuzov.packet.Packet;

public class SCScreenFrame extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }
}