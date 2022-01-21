package me.kutuzov.packet;

public class SCKeepAlive extends Packet{
    @Override
    public boolean isServer() { return true; }
}