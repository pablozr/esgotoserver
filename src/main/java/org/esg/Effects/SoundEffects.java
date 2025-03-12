package org.esg.Effects;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundEffects {

    private SoundEffects() {}

    public static void playError(Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
    }

    public static void playShotAt(Player player, Location location) {
        player.getWorld().playSound(location, Sound.FIREWORK_BLAST, 1.0f, 1.0f);
    }

    public static void playReloadComplete(Player player) {
        player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1.0f, 1.0f);
    }
}