package me.kutuzov.packet;

public class CSWriterPacket extends Packet {
    public String text = "";

    @Override
    public boolean isServer() {
        return false;
    }
}