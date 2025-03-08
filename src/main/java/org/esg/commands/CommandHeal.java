package org.esg.commands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHeal implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            final int MAXCAP = 20;

            if (player.getHealth() < MAXCAP || player.getFoodLevel() < MAXCAP) {
                player.setHealth(MAXCAP);
                player.setFoodLevel(MAXCAP);
                player.setSaturation(MAXCAP);
                player.playSound(player.getLocation(), Sound.SHEEP_SHEAR, 1.0f, 1.0f);
            } else {
                Bukkit.getLogger().info("Only players can use this command.");
                return true;
            }
            return true;
        }
        return false;
    }
}