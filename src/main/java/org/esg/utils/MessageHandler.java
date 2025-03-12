package org.esg.utils;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.entity.Player;

public final class MessageHandler {

    private MessageHandler() {}

    public static void send(Player player, String message) {
        ActionBarAPI.sendActionBar(player, message);
    }

    public static void sendReloading(Player player) {
        send(player, "§cRecarregando, aguarde!");
    }

    public static void sendAlreadyReloading(Player player) {
        send(player, "§cJá está recarregando!");
    }

    public static void sendNoAmmo(Player player) {
        send(player, "§cSem munição!");
    }

    public static void sendFullAmmo(Player player) {
        send(player, "§aArma cheia!");
    }

    public static void sendReloadCancelled(Player player) {
        send(player, "§cRecarga cancelada!", 40);
    }

    public static void sendReloadProgress(Player player, int secondsLeft) {
        send(player, "§eRecarregando... " + secondsLeft + "s");
    }

    public static void sendReloadComplete(Player player, int currentAmmo, int maxAmmo) {
        send(player, "§aRecarga concluída! Munição: " + currentAmmo + "/" + maxAmmo);
    }

    public static void sendAmmoStatus(Player player, int currentAmmo, int maxAmmo) {
        send(player, "§fMunição: " + currentAmmo + "/" + maxAmmo);
    }

    public static void clear(Player player) {
        send(player, "", 20);
    }

    public static void send(Player player, String message, int duration) {
        ActionBarAPI.sendActionBar(player, message, duration);
    }
}