package me.kutuzov.packet.bukkit;

import me.kutuzov.packet.Packet;

public class SCBukkitPlayerCommand extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    public final String name, command;
    public SCBukkitPlayerCommand(String name, String command) {
        this.name = name;
        this.command = command;
    }
}