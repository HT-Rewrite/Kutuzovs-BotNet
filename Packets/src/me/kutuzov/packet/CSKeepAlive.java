package me.kutuzov.packet;

public class CSKeepAlive extends Packet {
    @Override
    public boolean isServer() { return false; }
}