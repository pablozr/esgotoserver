package org.esg.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.esg.weapons.WeaponFactory;
import org.esg.models.Weapon;

public class GunsMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§8§lLoja de Armas")) {
            return;
        }

        event.setCancelled(true); // Cancela o evento para evitar que o jogador mova itens

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        String itemName = clickedItem.getItemMeta().getDisplayName();

        // Verificar se clicou no botão de fechar
        if (itemName.equals("§c§lFechar")) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.CHEST_CLOSE, 1.0f, 1.0f);
            return;
        }

        // Verificar se clicou em uma arma
        if (itemName.equals("§6§lAK47")) {
            giveWeaponToPlayer(player, "ak47");
            return;
        }
    }

    private void giveWeaponToPlayer(Player player, String weaponName) {
        try {
            // Criar a arma usando o WeaponFactory
            Weapon weapon = WeaponFactory.createWeapon(weaponName);
            ItemStack weaponItem = WeaponFactory.toItemStack(weapon);

            // Adicionar a arma ao inventário do jogador
            player.getInventory().addItem(weaponItem);
            
            player.sendMessage("§aVocê recebeu uma " + weapon.getName() + "!");
            player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1.0f, 1.0f);
            
            // Fechar o inventário
            player.closeInventory();
            
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cErro ao obter a arma: " + e.getMessage());
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
        }
    }
} 