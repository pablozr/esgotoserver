package org.esg.utils;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.entity.Player;

/**
 * Utility class for sending action bar messages to players.
 */
public final class MessageHandler {

    private enum Messages {
        RELOADING("§cRecarregando, aguarde!"),
        ALREADY_RELOADING("§cJá está recarregando!"),
        NO_AMMO("§cSem munição!"),
        FULL_AMMO("§aArma cheia!"),
        RELOAD_CANCELLED("§cRecarga cancelada!"),
        RELOAD_PROGRESS("§eRecarregando... %ds"),
        RELOAD_COMPLETE("§aRecarga concluída! Munição: %d/%d"),
        AMMO_STATUS("§fMunição: %d/%d"),
        CLEAR("");

        private final String template;

        Messages(String template) {
            this.template = template;
        }

        public String format(Object... args) {
            return String.format(template, args);
        }
    }

    private static final int RELOAD_CANCELLED_DURATION = 40;
    private static final int CLEAR_DURATION = 20;

    private MessageHandler() {
        // Construtor privado para evitar instanciação.
    }

    public static void send(Player player, String message) {
        ActionBarAPI.sendActionBar(player, message);
    }

    public static void sendReloading(Player player) {
        send(player, Messages.RELOADING.format());
    }

    public static void sendAlreadyReloading(Player player) {
        send(player, Messages.ALREADY_RELOADING.format());
    }

    public static void sendNoAmmo(Player player) {
        send(player, Messages.NO_AMMO.format());
    }

    public static void sendFullAmmo(Player player) {
        send(player, Messages.FULL_AMMO.format());
    }

    public static void sendReloadCancelled(Player player) {
        send(player, Messages.RELOAD_CANCELLED.format(), RELOAD_CANCELLED_DURATION);
    }

    public static void sendReloadProgress(Player player, int secondsLeft) {
        send(player, Messages.RELOAD_PROGRESS.format(secondsLeft));
    }

    public static void sendReloadComplete(Player player, int currentAmmo, int maxAmmo) {
        send(player, Messages.RELOAD_COMPLETE.format(currentAmmo, maxAmmo));
    }

    public static void sendAmmoStatus(Player player, int currentAmmo, int maxAmmo) {
        send(player, Messages.AMMO_STATUS.format(currentAmmo, maxAmmo));
    }

    public static void clear(Player player) {
        send(player, Messages.CLEAR.format(), CLEAR_DURATION);
    }

    public static void send(Player player, String message, int duration) {
        ActionBarAPI.sendActionBar(player, message, duration);
    }
}