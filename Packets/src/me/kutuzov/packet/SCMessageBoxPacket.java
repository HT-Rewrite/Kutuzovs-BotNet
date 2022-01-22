package me.kutuzov.packet;

public class SCMessageBoxPacket extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    public final String title, content;
    public SCMessageBoxPacket(String title, String content) {
        this.title = title;
        this.content = content;
    }
}