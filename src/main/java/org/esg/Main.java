package org.esg;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.esg.commands.CommandHeal;
import org.esg.listeners.ReloadListener;
import org.esg.listeners.ShootListener;

public final class Main extends JavaPlugin {

    @Getter
    private static Main plugin;

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(new ShootListener(), this);
        getServer().getPluginManager().registerEvents(new ReloadListener(), this);
        this.getCommand("heal").setExecutor(new CommandHeal());
    }

    @Override
    public void onDisable() {
    }
}