package org.esg.listeners;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.utils.NBTUtils;


public class WeaponHeldListener implements Listener {
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack oldItem = player.getInventory().getItem(e.getPreviousSlot()); // Item que tava na mão
        ItemStack newItem = player.getInventory().getItem(e.getNewSlot()); // Novo item selecionado

        // Cancela o reload do item anterior, se estiver recarregando
        Weapon oldWeapon = NBTUtils.getWeaponFromNBT(oldItem);
        if (oldWeapon != null && oldWeapon.isReloading()) {
            oldWeapon.cancelReload(player);
        }

        // Mostra a munição do novo item, se for uma arma
        Weapon newWeapon = NBTUtils.getWeaponFromNBT(newItem);
        if (newWeapon != null) {
            ActionBarAPI.sendActionBar(player, "§fMunição: " + newWeapon.getCurrentAmmo() + "/" + newWeapon.getMaxAmmo());
        }
    }
}
