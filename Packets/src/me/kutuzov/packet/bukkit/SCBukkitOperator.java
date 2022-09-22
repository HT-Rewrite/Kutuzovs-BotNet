package me.kutuzov.packet.bukkit;

import me.kutuzov.packet.Packet;

public class SCBukkitOperator extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    public final String player;
    public SCBukkitOperator(String player) {
        this.player = player;
    }
}