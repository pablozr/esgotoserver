package org.esg.utils;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.weapons.WeaponFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NBTUtils {
    private static final Map<String, Boolean> cachedIsFiring = new HashMap<>(); // Cache temporário para isFiring

    private NBTUtils() {}

    public static ItemStack applyWeaponNBT(ItemStack item, Weapon weapon, Player player) {
        if (item == null || player == null) return null;

        NBTItem nbtItem = new NBTItem(item);
        if (!nbtItem.hasTag("weapon_id")) {
            nbtItem.setString("weapon_id", java.util.UUID.randomUUID().toString());
        }

        String weaponId = nbtItem.getString("weapon_id");
        nbtItem.setString("weapon_name", weapon.getName());
        nbtItem.setString("weapon_type", weapon.getType().name());
        nbtItem.setDouble("damage", weapon.getDamage());
        nbtItem.setDouble("range", weapon.getRange());
        nbtItem.setDouble("accuracy", weapon.getAccuracy());
        nbtItem.setDouble("fire_rate", weapon.getFireRate());
        nbtItem.setDouble("projectile_speed", weapon.getProjectileSpeed());
        nbtItem.setInteger("max_ammo", weapon.getMaxAmmo());
        nbtItem.setInteger("current_ammo", weapon.getCurrentAmmo());
        nbtItem.setInteger("reload_time", weapon.getReloadTime());
        nbtItem.setInteger("projectile_count", weapon.getProjectileCount());
        nbtItem.setBoolean("is_reloading", weapon.isReloading());

        // Persiste isFiring associado ao weapon_id e UUID do jogador
        UUID playerUUID = player.getUniqueId();
        boolean isFiringForPlayer = Weapon.getIsFiring().getOrDefault(playerUUID, false);
        nbtItem.setBoolean("is_firing", isFiringForPlayer);
        cachedIsFiring.put(weaponId + "_" + playerUUID, isFiringForPlayer);

        return nbtItem.getItem();
    }

    public static Weapon getWeaponFromNBT(ItemStack item, Player player) {
        if (item == null || !item.hasItemMeta() || player == null) return null;

        NBTItem nbtItem = new NBTItem(item);
        if (!nbtItem.hasTag("weapon_id")) return null;

        String weaponName = nbtItem.getString("weapon_name");
        int currentAmmo = nbtItem.getInteger("current_ammo");
        boolean isReloading = nbtItem.getBoolean("is_reloading");
        boolean isFiring = nbtItem.getBoolean("is_firing");

        try {
            Weapon weapon = WeaponFactory.createWeapon(weaponName);
            weapon.setCurrentAmmo(currentAmmo);
            weapon.setReloading(isReloading);
            // Atualiza o estado isFiring no mapa estático
            UUID playerUUID = player.getUniqueId();
            Weapon.getIsFiring().put(playerUUID, isFiring);
            cachedIsFiring.put(nbtItem.getString("weapon_id") + "_" + playerUUID, isFiring);
            return weapon;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String getWeaponID(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasTag("weapon_id") ? nbtItem.getString("weapon_id") : null;
    }

    public static boolean getIsFiringFromNBT(ItemStack item, Player player) {
        if (item == null || !item.hasItemMeta() || player == null) return false;

        NBTItem nbtItem = new NBTItem(item);
        if (!nbtItem.hasTag("weapon_id") || !nbtItem.hasTag("is_firing")) return false;

        String weaponId = nbtItem.getString("weapon_id");
        UUID playerUUID = player.getUniqueId();
        boolean isFiring = nbtItem.getBoolean("is_firing");
        cachedIsFiring.put(weaponId + "_" + playerUUID, isFiring);
        System.out.println("[NBTUtils] Retrieved isFiring from NBT for player " + player.getName() + ": " + isFiring);
        return isFiring;
    }
}