package org.esg.weapons;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.utils.NBTUtils;

public class WeaponManager {
    public static void giveWeapon(Player player, Weapon weapon){
        ItemStack item = NBTUtils.applyWeaponNBT(WeaponFactory.toItemStack(weapon), weapon);
        player.getInventory().addItem(item);
    }
}
