package me.kutuzov.packet.bukkit;

import me.kutuzov.packet.Packet;

public class CSBukkitPluginList extends Packet {
    @Override
    public boolean isServer() {
        return false;
    }

    public final String[] plugins;
    public CSBukkitPluginList(String... plugins) {
        this.plugins = plugins;
    }
}