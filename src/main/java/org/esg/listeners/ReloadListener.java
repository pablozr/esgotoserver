package org.esg.listeners;

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

public class ReloadListener implements Listener {
    private final Map<UUID, Long> lastReloadAttempt = new HashMap<>();
    private static final long RELOAD_COOLDOWN = 500; // 500ms cooldown entre tentativas de recarga

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (lastReloadAttempt.containsKey(playerId) && (currentTime - lastReloadAttempt.get(playerId)) < RELOAD_COOLDOWN) {
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInHand();
        Weapon weapon = WeaponUtils.getWeaponFromItem(itemInHand, player);
        if (weapon != null) {
            if (Weapon.getIsFiring().getOrDefault(player.getUniqueId(), false)) {
                return;
            }
            if (weapon.isReloading(player)) {
                MessageHandler.sendAlreadyReloading(player);
                return;
            }
            lastReloadAttempt.put(playerId, currentTime);
            weapon.reload(player);
        }
    }

    @EventHandler
    public void onPlayerSwitchItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        int previousSlot = event.getPreviousSlot();

        if (previousSlot < 0 || previousSlot > 8) {
            return;
        }

        ItemStack previousItem = player.getInventory().getItem(previousSlot);
        Weapon weapon = WeaponUtils.getWeaponFromItem(previousItem, player);

        if (weapon != null && weapon.isReloading(player)) {
            weapon.cancelReload(player);
            try {
                ItemStack updatedItem = WeaponUtils.applyWeaponToItem(previousItem, weapon, player);
                player.getInventory().setItem(previousSlot, updatedItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        Weapon weapon = WeaponUtils.getWeaponFromItem(droppedItem, player);

        if (weapon != null && weapon.isReloading(player)) {
            try {
                weapon.cancelReload(player);
                // Forçar atualização do estado no NBT
                weapon.setReloading(false);
                ItemStack updatedItem = WeaponUtils.applyWeaponToItem(droppedItem, weapon, player);
                event.getItemDrop().setItemStack(updatedItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (!(event.getInventory().getHolder() instanceof Player)) return;

        Player player = (Player) event.getInventory().getHolder();
        ItemStack item = event.getItem().getItemStack();
        Weapon weapon = WeaponUtils.getWeaponFromItem(item, player);

        if (weapon != null) {
            // Garantir que o estado seja falso ao pegar
            if (weapon.isReloading(player)) {
                weapon.setReloading(false);
            }
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

        weapon.cancelReload(player);
        weapon.setReloading(false); // Garantir estado falso
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
        if (rawSlot >= 0 && rawSlot <= 8) {
            for (int i = 9; i < inventory.length; i++) {
                if (inventory[i] == null) return i;
            }
        } else if (rawSlot >= 9 && rawSlot < inventory.length) {
            for (int i = 0; i <= 8; i++) {
                if (inventory[i] == null) return i;
            }
        }
        return -1;
    }
}