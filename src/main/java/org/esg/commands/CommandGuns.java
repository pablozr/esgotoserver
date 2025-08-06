package org.esg.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.esg.Main;
import org.esg.weapons.WeaponFactory;

import java.util.Arrays;

public class CommandGuns implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;
        openGunsMenu(player);
        return true;
    }

    private void openGunsMenu(Player player) {
        // Criar inventário com 3 linhas (27 slots)
        Inventory gunsMenu = Bukkit.createInventory(null, 27, "§8§lLoja de Armas");

        // Adicionar AK47
        ItemStack ak47Item = createWeaponItem("AK47", Material.STONE_HOE, 
            "§6§lAK47", 
            Arrays.asList(
                "§7Tipo: Rifle",
                "§7Dano: 7.0",
                "§7Alcance: 20 blocos",
                "§7Precisão: 85%",
                "§7Munição: 30/30",
                "",
                "§eClique para obter!"
            ));
        
        gunsMenu.setItem(13, ak47Item); // Posição central

        // Adicionar item de fechar
        ItemStack closeItem = createItem(Material.BARRIER, "§c§lFechar", 
            Arrays.asList("§7Clique para fechar o menu"));

        gunsMenu.setItem(26, closeItem); // Último slot

        player.openInventory(gunsMenu);
    }

    private ItemStack createWeaponItem(String weaponName, Material material, String displayName, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createItem(Material material, String displayName, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
} 