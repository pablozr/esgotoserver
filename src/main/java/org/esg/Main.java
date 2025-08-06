package org.esg;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.esg.commands.CommandHeal;
import org.esg.commands.CommandGuns;
import org.esg.listeners.ReloadListener;
import org.esg.listeners.ShootListener;
import org.esg.listeners.GunsMenuListener;

public final class Main extends JavaPlugin {

    @Getter
    private static Main plugin;

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(new ShootListener(), this);
        getServer().getPluginManager().registerEvents(new ReloadListener(), this);
        getServer().getPluginManager().registerEvents(new GunsMenuListener(), this);
        this.getCommand("heal").setExecutor(new CommandHeal());
        this.getCommand("guns").setExecutor(new CommandGuns());
    }

    @Override
    public void onDisable() {
    }
}