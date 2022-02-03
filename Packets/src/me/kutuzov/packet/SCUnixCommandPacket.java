package me.kutuzov.packet;

public class SCUnixCommandPacket extends Packet {
    public final String command;
    public SCUnixCommandPacket(String command) {
        this.command = command;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}