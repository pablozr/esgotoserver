package org.esg.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.esg.models.Weapon;
import org.esg.weapons.WeaponFactory;
import org.esg.Manager.WeaponManager;

import java.util.stream.Collectors;

public class WeaponCommand implements CommandExecutor {

    private static final String USAGE_MESSAGE = ChatColor.RED + "Uso: /weapon <nome-da-arma>";
    private static final String ONLY_PLAYERS_MESSAGE = ChatColor.RED + "Este comando só pode ser usado por jogadores!";
    private static final String UNKNOWN_WEAPON_MESSAGE = ChatColor.RED + "Arma desconhecida: %s";
    private static final String WEAPON_RECEIVED_MESSAGE = ChatColor.GREEN + "Você recebeu uma %s!";
    private static final String AVAILABLE_WEAPONS_MESSAGE = ChatColor.YELLOW + "Armas disponíveis: %s";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isPlayer(sender)) {
            sender.sendMessage(ONLY_PLAYERS_MESSAGE);
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            showUsage(player);
            return true;
        }

        giveWeaponToPlayer(player, args[0].toLowerCase());
        return true;
    }

    private boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    private void showUsage(Player player) {
        player.sendMessage(USAGE_MESSAGE);
        player.sendMessage(String.format(AVAILABLE_WEAPONS_MESSAGE, getAvailableWeapons()));
    }

    private String getAvailableWeapons() {
        // Usa Stream para coletar as chaves (nomes das armas) do WEAPON_REGISTRY e juntá-las com vírgulas
        return WeaponFactory.getWeaponRegistry().keySet().stream()
                .collect(Collectors.joining(", "));
    }

    private void giveWeaponToPlayer(Player player, String weaponName) {
        try {
            Weapon weapon = WeaponFactory.createWeapon(weaponName);
            WeaponManager.giveWeapon(player, weapon);
            player.sendMessage(String.format(WEAPON_RECEIVED_MESSAGE, weapon.getName()));
        } catch (IllegalArgumentException e) {
            player.sendMessage(String.format(UNKNOWN_WEAPON_MESSAGE, weaponName));
            showUsage(player);
        }
    }
}