package org.esg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.utils.NBTUtils;

public class ShootListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = e.getPlayer();
            ItemStack itemInHand = player.getInventory().getItemInHand();
            Weapon weapon = NBTUtils.getWeaponFromNBT(itemInHand);
            if (weapon != null) {
                if (weapon.canShoot()) {
                    weapon.shoot(player);
                }
            }
        }
    }
}