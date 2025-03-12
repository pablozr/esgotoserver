package org.esg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.utils.WeaponUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.esg.Main;

import java.util.Map;
import java.util.UUID;

public class ShootListener implements Listener {
    private static final Map<UUID, Boolean> isFiring = Weapon.getIsFiring();
    private static final Map<UUID, Long> lastClickTimes = Weapon.getLastClickTimes();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        ItemStack itemInHand = player.getInventory().getItemInHand();
        Weapon weapon = WeaponUtils.getWeaponFromItem(itemInHand, player);
        if (weapon == null) return;

        // Chama o método shoot diretamente para garantir feedback em cada clique
        weapon.shoot(player);

        // Inicia ou continua o disparo contínuo apenas se o jogador puder atirar
        if (weapon.canShoot(player)) {
            isFiring.put(playerUUID, true);
            lastClickTimes.put(playerUUID, System.currentTimeMillis());
            weapon.startFiring(player);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isFiring.getOrDefault(playerUUID, false)) {
                        lastClickTimes.remove(playerUUID);
                        WeaponUtils.updateWeaponInHand(player, weapon);
                        cancel();
                        return;
                    }

                    if (weapon.isReloading(player)) {
                        System.out.println("[ShootListener] Canceling firing due to reloading for player: " + player.getName());
                        isFiring.put(playerUUID, false);
                        lastClickTimes.remove(playerUUID);
                        WeaponUtils.updateWeaponInHand(player, weapon);
                        cancel();
                        return;
                    }

                    long lastClick = lastClickTimes.getOrDefault(playerUUID, 0L);
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastClick > 100) {
                        isFiring.put(playerUUID, false);
                        lastClickTimes.remove(playerUUID);
                        WeaponUtils.updateWeaponInHand(player, weapon);
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getPlugin(), 2L, 2L);
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        isFiring.put(playerUUID, false);
        Weapon weapon = WeaponUtils.getWeaponFromItem(player.getInventory().getItemInHand(), player);
        if (weapon != null) {
            WeaponUtils.updateWeaponInHand(player, weapon);
        }
    }
}