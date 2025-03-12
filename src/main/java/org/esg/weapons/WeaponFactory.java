package org.esg.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.esg.models.Weapon;
import org.esg.models.WeaponType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class WeaponFactory {

    private static final Map<String, Supplier<Weapon>> WEAPON_REGISTRY = new HashMap<>();

    static {
        registerWeapon("ak47", AK47::new);
    }

    private WeaponFactory() {}

    public static void registerWeapon(String name, Supplier<Weapon> weaponSupplier) {
        WEAPON_REGISTRY.put(name.toLowerCase(), weaponSupplier);
    }

    public static Weapon createWeapon(String weaponName) {
        Supplier<Weapon> supplier = WEAPON_REGISTRY.get(weaponName.toLowerCase());
        if (supplier == null) {
            throw new IllegalArgumentException("Arma desconhecida: " + weaponName);
        }
        return supplier.get();
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
            case PISTOL: return Material.IRON_HOE;
            case SHOTGUN: return Material.WOOD_HOE;
            case RIFLE: return Material.STONE_HOE;
            case SNIPER: return Material.DIAMOND_HOE;
            default: return Material.IRON_HOE;
        }
    }
}