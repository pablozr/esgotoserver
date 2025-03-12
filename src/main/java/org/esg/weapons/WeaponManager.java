package org.esg.weapons;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.utils.WeaponUtils;

public final class WeaponManager {

    private WeaponManager() {}

    public static void giveWeapon(Player player, Weapon weapon) {
        ItemStack item = WeaponUtils.applyWeaponToItem(WeaponFactory.toItemStack(weapon), weapon, player);
        player.getInventory().addItem(item);
    }
}