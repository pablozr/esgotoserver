package org.esg;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.esg.Manager.InvulnerabilityManager;
import org.esg.commands.WeaponCommand;
import org.esg.listeners.ReloadListener;
import org.esg.listeners.ShootListener;
import org.esg.listeners.WeaponHeldListener;

public final class Main extends JavaPlugin {

    @Getter
    private static Main plugin;

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(new ShootListener(), this);
        getServer().getPluginManager().registerEvents(new ReloadListener(), this);
        getServer().getPluginManager().registerEvents(new WeaponHeldListener(), this);
        this.getCommand("weapon").setExecutor(new WeaponCommand());

        new BukkitRunnable() {
            @Override
            public void run() {
                InvulnerabilityManager.decrementInvulTicks();
            }
        }.runTaskTimer(this, 0L, 1L); // Roda a cada tick
    }



    @Override
    public void onDisable() {
    }
}