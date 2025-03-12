package org.esg.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;

public final class WeaponUtils {

    private WeaponUtils() {}

    public static void updateWeaponInHand(Player player, Weapon weapon) {
        ItemStack item = player.getInventory().getItemInHand();
        if (item != null) {
            ItemStack updatedItem = NBTUtils.applyWeaponNBT(item, weapon, player);
            player.getInventory().setItemInHand(updatedItem);
            System.out.println("[WeaponUtils] Updated weapon in hand for player " + player.getName() + ": isReloading=" + weapon.isReloading() + ", isFiring=" + Weapon.getIsFiring().getOrDefault(player.getUniqueId(), false));
        }
    }

    public static void updateWeaponInSlot(Player player, int slot, Weapon weapon) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item != null) {
            ItemStack updatedItem = NBTUtils.applyWeaponNBT(item, weapon, player);
            player.getInventory().setItem(slot, updatedItem);
        }
    }

    public static Weapon getWeaponFromItem(ItemStack item, Player player) {
        Weapon weapon = NBTUtils.getWeaponFromNBT(item, player);
        System.out.println("[WeaponUtils] Got weapon from item for player " + player.getName() + ": " + (weapon != null ? "name=" + weapon.getName() + ", isReloading=" + weapon.isReloading() + ", isFiring from NBT=" + NBTUtils.getIsFiringFromNBT(item, player) : "null"));
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