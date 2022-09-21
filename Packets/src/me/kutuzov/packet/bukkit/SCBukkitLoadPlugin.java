package me.kutuzov.packet.bukkit;

import me.kutuzov.packet.Packet;

public class SCBukkitLoadPlugin extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    public final String file;
    public SCBukkitLoadPlugin(String file) {
        this.file = file;
    }
}