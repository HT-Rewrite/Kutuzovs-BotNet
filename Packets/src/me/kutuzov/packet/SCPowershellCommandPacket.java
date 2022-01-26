package me.kutuzov.packet;

public class SCPowershellCommandPacket extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    public final String command;
    public SCPowershellCommandPacket(String command) {
        this.command = command;
    }
}