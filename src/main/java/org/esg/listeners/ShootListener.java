package org.esg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.esg.models.Weapon;
import org.esg.utils.NBTUtils;
import org.esg.utils.WeaponUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.esg.Main;

import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.v1_8_R3.MinecraftServer.LOGGER;

public class ShootListener implements Listener {
    private static final long CLICK_COOLDOWN_MS = 150;
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

        weapon.shoot(player);

        if (weapon.canShoot(player)) {
            isFiring.put(playerUUID, true);
            lastClickTimes.put(playerUUID, System.currentTimeMillis());
            weapon.startFiring(player);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isFiring.getOrDefault(playerUUID, false)) {
                        lastClickTimes.remove(playerUUID);
                        cancel();
                        return;
                    }

                    if (weapon.isReloading(player)) {
                        System.out.println("[ShootListener] Canceling firing due to reloading for player: " + player.getName());
                        isFiring.put(playerUUID, false);
                        lastClickTimes.remove(playerUUID);
                        cancel();
                        return;
                    }

                    long lastClick = lastClickTimes.getOrDefault(playerUUID, 0L);
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastClick > 100) {
                        isFiring.put(playerUUID, false);
                        lastClickTimes.remove(playerUUID);
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

        ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());
        if (previousItem != null) {
            Weapon previousWeapon = WeaponUtils.getWeaponFromItem(previousItem, player);
            if (previousWeapon != null) {
                if (previousWeapon.isReloading(player)) {
                    previousWeapon.cancelReload(player);
                }
                WeaponUtils.updateWeaponInSlot(player, event.getPreviousSlot(), previousWeapon); // Salva o estado
                LOGGER.info("Estado da arma anterior salvo no slot " + event.getPreviousSlot() + ": currentAmmo=" + previousWeapon.getCurrentAmmo());
            }
        }

        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (newItem != null) {
            String newItemWeaponId = NBTUtils.getWeaponID(newItem);
            if (newItemWeaponId != null) {
                Weapon newWeapon = WeaponUtils.getWeaponFromItem(newItem, player);
                if (newWeapon != null) {
                    LOGGER.info("Jogador " + player.getName() + " trocou para a arma " + newWeapon.getName() +
                            " no slot " + event.getNewSlot() + " com currentAmmo=" + newWeapon.getCurrentAmmo());
                }
            } else {
                LOGGER.info("Jogador " + player.getName() + " trocou para um item que não é arma no slot " + event.getNewSlot());
            }
        }
    }
}