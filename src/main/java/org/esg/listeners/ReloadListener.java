package org.esg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.utils.NBTUtils;

public class ReloadListener implements Listener {

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInHand();
        Weapon weapon = NBTUtils.getWeaponFromNBT(itemInHand);

        if (weapon != null) {
            if (weapon.getCurrentAmmo() < weapon.getMaxAmmo()) {
                weapon.reload(player);
            }
        }
    }
}