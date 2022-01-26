package me.kutuzov.packet;

public class CSWinCommandResponsePacket extends Packet {
    @Override
    public boolean isServer() {
        return false;
    }

    public final String response;
    public CSWinCommandResponsePacket(String response) {
        this.response = response;
    }
}