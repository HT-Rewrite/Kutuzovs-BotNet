package me.kutuzov.packet;

public class SCEpilepsyPacket extends Packet{
    @Override
    public boolean isServer() {
        return true;
    }

    public final long time;
    public SCEpilepsyPacket(long time){
        this.time = time;
    }
}