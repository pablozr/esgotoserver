package org.esg.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.esg.models.Weapon;
import org.esg.models.WeaponType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Factory class for creating and managing weapons.
 */
public final class WeaponFactory {

    private static final Map<String, Supplier<Weapon>> WEAPON_REGISTRY = new HashMap<>();
    private static final Map<WeaponType, Material> WEAPON_MATERIALS = new HashMap<>();

    static {
        WEAPON_MATERIALS.put(WeaponType.PISTOL, Material.IRON_HOE);
        WEAPON_MATERIALS.put(WeaponType.SHOTGUN, Material.WOOD_HOE);
        WEAPON_MATERIALS.put(WeaponType.RIFLE, Material.STONE_HOE);
        WEAPON_MATERIALS.put(WeaponType.SNIPER, Material.DIAMOND_HOE);

        registerWeapon("ak47", AK47::new);
    }

    private WeaponFactory() {
        // Construtor privado para evitar instanciação.
    }

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

    public static Map<String, Supplier<Weapon>> getWeaponRegistry() {
        return Collections.unmodifiableMap(WEAPON_REGISTRY);
    }

    private static Material getMaterialForWeapon(Weapon weapon) {
        return WEAPON_MATERIALS.getOrDefault(weapon.getType(), Material.IRON_HOE);
    }


}