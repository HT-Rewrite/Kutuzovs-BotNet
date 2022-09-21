package me.pk2.bukkitplugintest;

import me.kutuzov.client.KutuzovEntry;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.reflect.Method;

public class KutuzovsPlugin extends JavaPlugin {
    private boolean bool1=false;

    @Override
    public void onEnable() {
        if(!bool1) {
            KutuzovEntry.main(new String[]{});
            bool1=true;
        }

        getLogger().info("Loaded kutuzovs test plugin.");
    }

    @Override
    public void onDisable() {
    }
}