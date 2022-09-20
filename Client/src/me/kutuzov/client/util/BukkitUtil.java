package me.kutuzov.client.util;

public class BukkitUtil {
    public static boolean isMC() {
        try {
            Class.forName("org.bukkit.Bukkit");
        } catch (ClassNotFoundException exception) { return false; }
        return true;
    }
}