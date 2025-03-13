package org.esg.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;

import java.util.logging.Logger;

/**
 * Utility class for managing weapon-related item updates and retrieval.
 */
public final class WeaponUtils {

    private static final Logger LOGGER = Logger.getLogger(WeaponUtils.class.getName());

    private WeaponUtils() {
        // Construtor privado para evitar instanciação.
    }

    public static void updateWeaponInHand(Player player, Weapon weapon) {
        ItemStack item = player.getInventory().getItemInHand();
        if (item != null) {
            String itemWeaponId = NBTUtils.getWeaponID(item); // Pega o weapon_id do item
            if (itemWeaponId != null) { // Só atualiza se for uma arma
                ItemStack updatedItem = NBTUtils.applyWeaponNBT(item, weapon, player);
                player.getInventory().setItemInHand(updatedItem);
                LOGGER.info("Arma atualizada para " + player.getName() +
                        ": recarregando=" + weapon.isReloading() +
                        ", munição=" + weapon.getCurrentAmmo());
            } else {
                LOGGER.info("Atualização pulada para " + player.getName() +
                        ": item na mão não é uma arma (sem weapon_id)");
            }
        }
    }

    public static void updateWeaponInSlot(Player player, int slot, Weapon weapon) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item != null) {
            ItemStack updatedItem = NBTUtils.applyWeaponNBT(item, weapon, player);
            player.getInventory().setItem(slot, updatedItem);
            LOGGER.info("Updated weapon in slot " + slot + " for player " + player.getName() +
                    ": isReloading=" + weapon.isReloading() +
                    ", currentAmmo=" + weapon.getCurrentAmmo());
        }
    }

    public static Weapon getWeaponFromItem(ItemStack item, Player player) {
        Weapon weapon = NBTUtils.getWeaponFromNBT(item, player);
        if (weapon != null) {
            LOGGER.info("Got weapon from item for player " + player.getName() + ": " +
                    "name=" + weapon.getName() +
                    ", currentAmmo=" + weapon.getCurrentAmmo() +
                    ", isReloading=" + weapon.isReloading() +
                    ", isFiring from NBT=" + NBTUtils.getIsFiringFromNBT(item, player));
        }
        return weapon;
    }

    public static String getWeaponId(ItemStack item) {
        return NBTUtils.getWeaponID(item);
    }

    public static ItemStack applyWeaponToItem(ItemStack item, Weapon weapon, Player player) {
        return NBTUtils.applyWeaponNBT(item, weapon, player);
    }

    public static boolean isSameWeapon(ItemStack item, String weaponId) {
        if (item == null || weaponId == null) return false;
        String itemWeaponId = getWeaponId(item);
        return weaponId.equals(itemWeaponId);
    }
}