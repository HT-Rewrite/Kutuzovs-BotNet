package me.kutuzov.packet;

public class CSPowershellResponsePacket extends Packet {
    @Override
    public boolean isServer() {
        return false;
    }

    public final String response;
    public CSPowershellResponsePacket(String response) {
        this.response = response;
    }
}