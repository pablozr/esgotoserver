package org.esg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.utils.MessageHandler;
import org.esg.utils.WeaponUtils;

/**
 * Listener for handling weapon state updates when a player changes the held item.
 */
public class WeaponHeldListener implements Listener {

    private static final int INVENTORY_HOTBAR_SIZE = 9;

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        handlePreviousItem(player, event.getPreviousSlot());
        updateNewItem(player, event.getNewSlot());
    }

    private void handlePreviousItem(Player player, int previousSlot) {
        if (!isValidSlot(previousSlot)) return;

        ItemStack oldItem = player.getInventory().getItem(previousSlot);
        Weapon oldWeapon = WeaponUtils.getWeaponFromItem(oldItem, player);
        if (oldWeapon != null && oldWeapon.isReloading()) {
            oldWeapon.cancelReload(player);
            ItemStack updatedItem = WeaponUtils.applyWeaponToItem(oldItem, oldWeapon, player);
            player.getInventory().setItem(previousSlot, updatedItem);
        }
    }

    private void updateNewItem(Player player, int newSlot) {
        if (!isValidSlot(newSlot)) return;

        ItemStack newItem = player.getInventory().getItem(newSlot);
        Weapon newWeapon = WeaponUtils.getWeaponFromItem(newItem, player);
        if (newWeapon != null) {
            MessageHandler.sendAmmoStatus(player, newWeapon.getCurrentAmmo(), newWeapon.getMaxAmmo());
        }
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < INVENTORY_HOTBAR_SIZE;
    }
}