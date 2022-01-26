package me.kutuzov.packet;

public class SCWinCommandPacket extends Packet {
    public final String command;
    public SCWinCommandPacket(String command) {
        this.command = command;
    }

    @Override
    public boolean isServer() {
        return true;
    }
}