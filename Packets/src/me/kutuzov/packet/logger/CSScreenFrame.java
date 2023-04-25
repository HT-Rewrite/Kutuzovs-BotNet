package me.kutuzov.packet.logger;

import me.kutuzov.packet.Packet;

public class CSScreenFrame extends Packet {
    public final int screens;
    public final byte[] data;

    @Override
    public boolean isServer() {
        return false;
    }

    public CSScreenFrame(int screens, byte[] data) {
        this.screens = screens;
        this.data = data;
    }
}