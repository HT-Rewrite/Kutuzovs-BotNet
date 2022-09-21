package me.kutuzov.packet.bukkit;

import me.kutuzov.packet.Packet;

public class SCBukkitPluginList extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }
}