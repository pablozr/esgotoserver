package org.esg.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.esg.models.Weapon;
import org.esg.weapons.WeaponFactory;
import org.esg.weapons.WeaponManager;

public class WeaponCommand implements CommandExecutor {

    private static final String USAGE_MESSAGE = ChatColor.RED + "Uso: /weapon <nome-da-arma>";
    private static final String AVAILABLE_WEAPONS = ChatColor.YELLOW + "Armas disponíveis: ak-47";
    private static final String ONLY_PLAYERS = ChatColor.RED + "Este comando só pode ser usado por jogadores!";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ONLY_PLAYERS);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(USAGE_MESSAGE);
            player.sendMessage(AVAILABLE_WEAPONS);
            return true;
        }

        String weaponName = args[0].toLowerCase();

        try {
            Weapon weapon = WeaponFactory.createWeapon(weaponName);
            WeaponManager.giveWeapon(player, weapon);
            player.sendMessage(ChatColor.GREEN + "Você recebeu uma " + weapon.getName() + "!");
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Arma desconhecida: " + weaponName);
            player.sendMessage(AVAILABLE_WEAPONS);
        }

        return true;
    }
}