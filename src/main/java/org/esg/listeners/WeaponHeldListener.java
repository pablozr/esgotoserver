package org.esg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.utils.MessageHandler;
import org.esg.utils.WeaponUtils;

public class WeaponHeldListener implements Listener {

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        int previousSlot = event.getPreviousSlot();
        ItemStack oldItem = player.getInventory().getItem(previousSlot);
        Weapon oldWeapon = WeaponUtils.getWeaponFromItem(oldItem,player);

        if (oldWeapon != null && oldWeapon.isReloading()) {
            oldWeapon.cancelReload(player);
            player.getInventory().setItem(previousSlot, WeaponUtils.applyWeaponToItem(oldItem, oldWeapon, player));
        }

        Weapon newWeapon = WeaponUtils.getWeaponFromItem(player.getInventory().getItem(event.getNewSlot()), player);
        if (newWeapon != null) {
            MessageHandler.sendAmmoStatus(player, newWeapon.getCurrentAmmo(), newWeapon.getMaxAmmo());
        }
    }
}