package me.kutuzov.client.wrapper;

import me.kutuzov.client.util.BukkitUtil;

import java.net.InetSocketAddress;

public class BukkitWrapper {
    public final boolean isMC;
    private Class<?> _class = null;
    private Class<?> _serverClass = null;
    private Class<?> _playerClass = null;
    private Object _serverInstance = null;
    public BukkitWrapper() {
        this.isMC = BukkitUtil.isMC();
        if(isMC) {
            try {
                _class = Class.forName("org.bukkit.Bukkit");
                _serverClass = Class.forName("org.bukkit.Server");
                _playerClass = Class.forName("org.bukkit.entity.Player");

                _serverInstance = _serverClass.getDeclaredMethod("getServer").invoke(null);
            } catch (Exception exception) {
                //exception.printStackTrace();
            }
        }
    }

    public String getBukkitVersion() {
        try {
            return (String)_class.getDeclaredMethod("getBukkitVersion").invoke(null);
        } catch (Exception exception) {
            //exception.printStackTrace();
            return "";
        }
    }

    public int getPort() {
        try {
            return (int)_class.getDeclaredMethod("getPort").invoke(null);
        } catch (Exception exception) {
            //exception.printStackTrace();
            return -1;
        }
    }

    public Object getConsoleSender() {
        try {
            return _class.getDeclaredMethod("getConsoleSender").invoke(null);
        } catch (Exception exception) {
            //exception.printStackTrace();
            return null;
        }
    }

    public Object getPlayer(String name) {
        try {
            return _class.getDeclaredMethod("getPlayer", String.class).invoke(null, name);
        } catch (Exception exception) {
            //exception.printStackTrace();
            return null;
        }
    }

    public void player_chat(Object player, String message) {
        try {
            _playerClass.getDeclaredMethod("chat", String.class).invoke(player, message);
        } catch (Exception exception) {
            //exception.printStackTrace();
        }
    }

    public void dispatchCommand(Object sender, String commandLine) {
        try {

            _class.getDeclaredMethod("dispatchCommand", Class.forName("org.bukkit.command.CommandSender"), String.class).invoke(null, sender, commandLine);
        } catch (Exception exception) {
            //exception.printStackTrace();
        }
    }

    public InetSocketAddress player_getAddress(Object player) {
        try {
            return (InetSocketAddress)_playerClass.getDeclaredMethod("getAddress").invoke(player);
        } catch (Exception exception) {
            //exception.printStackTrace();
            return null;
        }
    }
}