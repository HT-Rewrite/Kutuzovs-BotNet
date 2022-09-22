package me.kutuzov.packet.bukkit;

import me.kutuzov.packet.Packet;

public class SCBukkitGamemode extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    public final String player;
    public final int gamemode;
    public SCBukkitGamemode(String player, int gamemode) {
        this.player = player;
        this.gamemode = gamemode;
    }
}