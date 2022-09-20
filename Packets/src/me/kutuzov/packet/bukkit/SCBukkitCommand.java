package me.kutuzov.packet.bukkit;

import me.kutuzov.packet.Packet;

public class SCBukkitCommand extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    private String command;
    public SCBukkitCommand(String command) {
        this.command = command;
    }

    public String getCommand() { return command; }
}