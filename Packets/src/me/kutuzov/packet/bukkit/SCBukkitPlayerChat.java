package me.kutuzov.packet.bukkit;

import me.kutuzov.packet.Packet;

public class SCBukkitPlayerChat extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    public final String name, message;
    public SCBukkitPlayerChat(String name, String message) {
        this.name = name;
        this.message = message;
    }
}