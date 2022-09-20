package me.kutuzov.packet.bukkit;

import me.kutuzov.packet.Packet;

public class CSBukkitPlayerAddress extends Packet {
    @Override
    public boolean isServer() {
        return false;
    }

    public final String address;
    public CSBukkitPlayerAddress(String address) {
        this.address = address;
    }
}