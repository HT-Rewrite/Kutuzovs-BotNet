package me.kutuzov.packet;

public class SCMessageBoxPacket extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    public final String title, content;
    public final int amount;
    public SCMessageBoxPacket(String title, String content, int amount) {
        this.title = title;
        this.content = content;
        this.amount = amount;
    }
}