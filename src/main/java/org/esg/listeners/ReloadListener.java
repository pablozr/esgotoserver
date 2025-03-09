package org.esg.listeners;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.utils.NBTUtils;

public class ReloadListener implements Listener {

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        if (!e.isSneaking()) return;

        ItemStack itemInHand = player.getInventory().getItemInHand();
        Weapon weapon = NBTUtils.getWeaponFromNBT(itemInHand);

        if (weapon == null) return;

        if (weapon.isReloading()) {
            ActionBarAPI.sendActionBar(player, "§cJá está recarregando!");
            return;
        }

        if (weapon.getCurrentAmmo() < weapon.getMaxAmmo()) {
            weapon.reload(player);
        } else {
            ActionBarAPI.sendActionBar(player, "§aArma cheia!");
        }
    }

    @EventHandler
    public void onPlayerSwitchItem(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack previousItem = player.getInventory().getItem(e.getPreviousSlot());
        ItemStack newItem = player.getInventory().getItem(e.getNewSlot());

        String previousWeaponID = NBTUtils.getWeaponID(previousItem);
        String newWeaponID = NBTUtils.getWeaponID(newItem);

        Weapon previousWeapon = NBTUtils.getWeaponFromNBT(previousItem);

        if (previousWeapon != null && previousWeapon.isReloading()) {
            if (newWeaponID == null || !newWeaponID.equals(previousWeaponID)) {
                previousWeapon.cancelReload(player);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        ItemStack droppedItem = e.getItemDrop().getItemStack();
        Weapon weapon = NBTUtils.getWeaponFromNBT(droppedItem);

        if (weapon != null && weapon.isReloading()) {
            weapon.setReloading(false);
            if (droppedItem != null && droppedItem.getAmount() > 0) {
                ItemStack updatedItem = NBTUtils.applyWeaponNBT(droppedItem, weapon);
                e.getItemDrop().setItemStack(updatedItem);
            }
            weapon.cancelReload(player);
        }
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent e) {
        if (!(e.getInventory().getHolder() instanceof Player)) return;
        Player player = (Player) e.getInventory().getHolder();
        ItemStack item = e.getItem().getItemStack();
        Weapon weapon = NBTUtils.getWeaponFromNBT(item);

        if (weapon != null && weapon.isReloading()) {
            weapon.cancelReload(player);
        }
    }
}