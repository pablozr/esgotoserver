package org.esg.utils;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.weapons.WeaponFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Utility class for handling NBT data on items associated with weapons.
 */
public final class NBTUtils {

    private static final Logger LOGGER = Logger.getLogger(NBTUtils.class.getName());
    private static final long CACHE_EXPIRATION_MS = 5000;

    private static final Map<String, CachedFiringState> cachedIsFiring = new HashMap<>();

    private static class CachedFiringState {
        boolean isFiring;
        long timestamp;

        CachedFiringState(boolean isFiring, long timestamp) {
            this.isFiring = isFiring;
            this.timestamp = timestamp;
        }
    }

    private NBTUtils() {
        // Construtor privado para evitar instanciação.
    }

    public static ItemStack applyWeaponNBT(ItemStack item, Weapon weapon, Player player) {
        if (item == null || player == null) return null;

        NBTItem nbtItem = new NBTItem(item);
        if (!nbtItem.hasTag("weapon_id")) {
            nbtItem.setString("weapon_id", UUID.randomUUID().toString());
        }

        String weaponId = nbtItem.getString("weapon_id");
        setWeaponAttributes(nbtItem, weapon);
        setFiringState(nbtItem, weaponId, player);

        LOGGER.info("Applied NBT to item for player " + player.getName() + ": weaponId=" + weaponId +
                ", currentAmmo=" + weapon.getCurrentAmmo() + ", isReloading=" + weapon.isReloading());
        return nbtItem.getItem();
    }

    private static void setWeaponAttributes(NBTItem nbtItem, Weapon weapon) {
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
    }

    private static void setFiringState(NBTItem nbtItem, String weaponId, Player player) {
        UUID playerUUID = player.getUniqueId();
        boolean isFiringForPlayer = Weapon.getIsFiring().getOrDefault(playerUUID, false);
        nbtItem.setBoolean("is_firing", isFiringForPlayer);
        cachedIsFiring.put(weaponId + "_" + playerUUID, new CachedFiringState(isFiringForPlayer, System.currentTimeMillis()));
    }

    public static Weapon getWeaponFromNBT(ItemStack item, Player player) {
        if (item == null || !item.hasItemMeta() || player == null) return null;

        NBTItem nbtItem = new NBTItem(item);
        if (!nbtItem.hasTag("weapon_id")) return null;

        String weaponId = nbtItem.getString("weapon_id");
        String weaponName = nbtItem.getString("weapon_name");
        int currentAmmo = nbtItem.getInteger("current_ammo");
        boolean isReloading = nbtItem.getBoolean("is_reloading");
        boolean isFiring = nbtItem.getBoolean("is_firing");

        try {
            Weapon weapon = WeaponFactory.createWeapon(weaponName);
            weapon.setCurrentAmmo(currentAmmo);
            weapon.setReloading(isReloading);
            updateFiringState(player, weaponId, isFiring);
            LOGGER.info("Retrieved weapon from NBT for player " + player.getName() + ": weaponId=" + weaponId +
                    ", weaponName=" + weaponName + ", currentAmmo=" + currentAmmo + ", isReloading=" + isReloading);
            return weapon;
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Failed to create weapon from NBT for player " + player.getName() + ": " + e.getMessage());
            return null;
        }
    }

    private static void updateFiringState(Player player, String weaponId, boolean isFiring) {
        UUID playerUUID = player.getUniqueId();
        Weapon.getIsFiring().put(playerUUID, isFiring);
        cachedIsFiring.put(weaponId + "_" + playerUUID, new CachedFiringState(isFiring, System.currentTimeMillis()));
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
        String cacheKey = weaponId + "_" + playerUUID;

        CachedFiringState cached = cachedIsFiring.get(cacheKey);
        if (cached != null && (System.currentTimeMillis() - cached.timestamp) < CACHE_EXPIRATION_MS) {
            return cached.isFiring;
        }

        boolean isFiring = nbtItem.getBoolean("is_firing");
        cachedIsFiring.put(cacheKey, new CachedFiringState(isFiring, System.currentTimeMillis()));
        LOGGER.info("Retrieved isFiring from NBT for player " + player.getName() + ": " + isFiring);
        return isFiring;
    }
}