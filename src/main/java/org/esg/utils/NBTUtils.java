package org.esg.utils;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;
import org.esg.models.AmmoType;
import org.esg.models.Weapon;
import org.esg.models.WeaponType;
import org.esg.weapons.WeaponFactory;

import java.util.UUID;

public class NBTUtils {
    public static ItemStack applyWeaponNBT(ItemStack item, Weapon weapon){
        NBTItem nbtItem = new NBTItem(item);

        nbtItem.setString("weapon_id", UUID.randomUUID().toString());
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

        return nbtItem.getItem();
    }

    public static Weapon getWeaponFromNBT(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        NBTItem nbtItem = new NBTItem(item);
        if (!nbtItem.hasTag("weapon_id")) return null;

        String weaponName = nbtItem.getString("weapon_name");
        int currentAmmo = nbtItem.getInteger("current_ammo");
        boolean isReloading = nbtItem.getBoolean("is_reloading");

        try {
            Weapon weapon = WeaponFactory.createWeapon(weaponName);
            weapon.setCurrentAmmo(currentAmmo);
            weapon.setReloading(isReloading);
            return weapon;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
}