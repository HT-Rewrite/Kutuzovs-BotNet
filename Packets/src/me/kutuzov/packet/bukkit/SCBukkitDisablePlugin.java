package me.kutuzov.packet.bukkit;

import me.kutuzov.packet.Packet;

public class SCBukkitDisablePlugin extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    public final String pluginName;
    public SCBukkitDisablePlugin(String pluginName) {
        this.pluginName = pluginName;
    }
}