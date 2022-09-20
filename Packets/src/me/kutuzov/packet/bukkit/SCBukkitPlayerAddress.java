package me.kutuzov.packet.bukkit;

import me.kutuzov.packet.Packet;

public class SCBukkitPlayerAddress extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    public final String name;
    public SCBukkitPlayerAddress(String name) { this.name = name; }
}