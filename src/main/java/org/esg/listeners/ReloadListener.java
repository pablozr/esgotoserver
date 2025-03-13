package org.esg.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.utils.MessageHandler;
import org.esg.utils.WeaponUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Listener for handling weapon reload events triggered by player actions.
 */
public class ReloadListener implements Listener {

    private static final Logger LOGGER = Logger.getLogger(ReloadListener.class.getName());
    private static final long RELOAD_COOLDOWN_MS = 500;
    private static final int INVENTORY_HOTBAR_SIZE = 9;
    private static final int INVENTORY_TOTAL_SIZE = 36;

    private final Map<UUID, Long> lastReloadAttempt = new HashMap<>();

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        if (!canAttemptReload(player)) return;

        ItemStack itemInHand = player.getInventory().getItemInHand();
        Weapon weapon = WeaponUtils.getWeaponFromItem(itemInHand, player);
        if (weapon == null || !canReload(player, weapon)) return;

        lastReloadAttempt.put(player.getUniqueId(), System.currentTimeMillis());
        weapon.reload(player);
    }

    @EventHandler
    public void onPlayerSwitchItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        int previousSlot = event.getPreviousSlot();
        int newSlot = event.getNewSlot();

        if (isValidSlot(previousSlot)) {
            ItemStack previousItem = player.getInventory().getItem(previousSlot);
            if (previousItem != null) {
                Weapon previousWeapon = WeaponUtils.getWeaponFromItem(previousItem, player);
                if (previousWeapon != null && previousWeapon.isReloading(player)) {
                    LOGGER.info("Cancelling reload for weapon in slot " + previousSlot + " with weaponId=" + WeaponUtils.getWeaponId(previousItem));
                    previousWeapon.cancelReload(player);
                }
            }
        }

        if (isValidSlot(newSlot)) {
            ItemStack newItem = player.getInventory().getItem(newSlot);
            if (newItem != null && newItem.getType() != Material.AIR) {
                Weapon newWeapon = WeaponUtils.getWeaponFromItem(newItem, player);
                if (newWeapon != null) {
                    LOGGER.info("Switched to weapon in slot " + newSlot + " with weaponId=" + WeaponUtils.getWeaponId(newItem) +
                            ", currentAmmo=" + newWeapon.getCurrentAmmo() + ", isReloading=" + newWeapon.isReloading(player));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        Weapon weapon = WeaponUtils.getWeaponFromItem(droppedItem, player);

        if (weapon != null && weapon.isReloading(player)) {
            cancelReloadAndUpdateDroppedItem(player, weapon, event);
        }
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (!(event.getInventory().getHolder() instanceof Player)) return;

        Player player = (Player) event.getInventory().getHolder();
        ItemStack item = event.getItem().getItemStack();
        Weapon weapon = WeaponUtils.getWeaponFromItem(item, player);

        if (weapon != null && weapon.isReloading(player)) {
            weapon.setReloading(false);
            ItemStack updatedItem = WeaponUtils.applyWeaponToItem(item, weapon, player);
            event.getItem().setItemStack(updatedItem);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem();
        Weapon weapon = WeaponUtils.getWeaponFromItem(currentItem, player);

        if (weapon == null || !weapon.isReloading(player)) return;

        handleInventoryClick(player, weapon, currentItem, event);
    }

    private boolean canAttemptReload(Player player) {
        UUID playerId = player.getUniqueId();
        Long lastAttempt = lastReloadAttempt.get(playerId);
        if (lastAttempt == null) return true;

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastAttempt) >= RELOAD_COOLDOWN_MS;
    }

    private boolean canReload(Player player, Weapon weapon) {
        if (Weapon.getIsFiring().getOrDefault(player.getUniqueId(), false)) return false;
        if (weapon.isReloading(player)) {
            MessageHandler.sendAlreadyReloading(player);
            return false;
        }
        return true;
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < INVENTORY_HOTBAR_SIZE;
    }

    private void cancelReloadAndUpdate(Player player, Weapon weapon, ItemStack item, int slot) {
        weapon.cancelReload(player);
        try {
            ItemStack updatedItem = WeaponUtils.applyWeaponToItem(item, weapon, player);
            player.getInventory().setItem(slot, updatedItem);
        } catch (Exception e) {
            LOGGER.severe("Failed to update weapon in slot " + slot + " for player " + player.getName() + ": " + e.getMessage());
        }
    }

    private void cancelReloadAndUpdateDroppedItem(Player player, Weapon weapon, PlayerDropItemEvent event) {
        try {
            weapon.cancelReload(player);
            weapon.setReloading(false);
            ItemStack updatedItem = WeaponUtils.applyWeaponToItem(event.getItemDrop().getItemStack(), weapon, player);
            event.getItemDrop().setItemStack(updatedItem);
        } catch (Exception e) {
            LOGGER.severe("Failed to update dropped weapon for player " + player.getName() + ": " + e.getMessage());
        }
    }

    private void handleInventoryClick(Player player, Weapon weapon, ItemStack currentItem, InventoryClickEvent event) {
        weapon.cancelReload(player);
        weapon.setReloading(false);
        ItemStack updatedItem = WeaponUtils.applyWeaponToItem(currentItem, weapon, player);

        if (event.isShiftClick()) {
            int destinationSlot = findDestinationSlot(player, event.getRawSlot());
            if (destinationSlot != -1) {
                player.getInventory().setItem(destinationSlot, updatedItem);
                event.setCurrentItem(null);
            } else {
                event.setCurrentItem(updatedItem);
            }
        } else {
            event.setCurrentItem(updatedItem);
        }

        event.setCancelled(true);
    }

    private int findDestinationSlot(Player player, int rawSlot) {
        ItemStack[] inventory = player.getInventory().getContents();
        if (rawSlot >= 0 && rawSlot < INVENTORY_HOTBAR_SIZE) {
            for (int i = INVENTORY_HOTBAR_SIZE; i < INVENTORY_TOTAL_SIZE; i++) {
                if (inventory[i] == null) return i;
            }
        } else if (rawSlot >= INVENTORY_HOTBAR_SIZE && rawSlot < INVENTORY_TOTAL_SIZE) {
            for (int i = 0; i < INVENTORY_HOTBAR_SIZE; i++) {
                if (inventory[i] == null) return i;
            }
        }
        return -1;
    }
}