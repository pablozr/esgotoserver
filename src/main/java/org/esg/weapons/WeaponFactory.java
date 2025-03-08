package org.esg.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.esg.models.Weapon;

public class WeaponFactory {

    public static Weapon createWeapon(String weaponName) {
        weaponName = weaponName.toLowerCase();
        switch (weaponName) {
            case "ak47":
                return new AK47();
            default:
                throw new IllegalArgumentException("Arma desconhecida: " + weaponName);
        }
    }

    public static ItemStack toItemStack(Weapon weapon) {
        ItemStack item = new ItemStack(getMaterialForWeapon(weapon));
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(weapon.getName());
            item.setItemMeta(meta);
        }

        return item;
    }

    private static Material getMaterialForWeapon(Weapon weapon) {
        switch (weapon.getType()) {
            case PISTOL:
                return Material.IRON_HOE;
            case SHOTGUN:
                return Material.WOOD_HOE;
            case RIFLE:
                return Material.STONE_HOE;
            case SNIPER:
                return Material.DIAMOND_HOE;
            default:
                return Material.IRON_HOE;
        }
    }
}