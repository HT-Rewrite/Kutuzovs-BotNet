package me.kutuzov.packet.bukkit;

import me.kutuzov.packet.Packet;

public class CSBukkitInfo extends Packet {
    @Override
    public boolean isServer() {
        return false;
    }

    public final String VERSION;
    public final int PORT;
    public CSBukkitInfo(String VERSION, int PORT) {
        this.VERSION = VERSION;
        this.PORT = PORT;
    }
}