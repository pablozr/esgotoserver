package org.esg.models;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.esg.utils.NBTUtils;


@Getter
public abstract class Weapon {
    protected String name;
    protected WeaponType type;
    protected AmmoType ammoType;
    protected double damage;
    protected double range;
    protected double accuracy;
    protected double fireRate;
    protected double projectileSpeed; // Velocidade de viagem do projétil
    protected int maxAmmo;
    protected int currentAmmo;
    protected int reloadTime;
    protected int projectileCount;// Quantidade de projéteis disparado por clique
    protected boolean isReloading = false;
    private transient BukkitRunnable reloadTimer; // Transient pra não persistir no NBT
    private transient int reloadTaskId = -1; // ID do scheduler pra cancelar
    private transient String reloadingWeaponId;

    public Weapon(String name, WeaponType type, AmmoType ammoType, double damage, double range,
                  double accuracy, double fireRate, double projectileSpeed, int maxAmmo,
                  int currentAmmo, int reloadTime, int projectileCount) {
        this.name = name;
        this.type = type;
        this.ammoType = ammoType;
        this.damage = damage;
        this.range = range;
        this.accuracy = accuracy;
        this.fireRate = fireRate;
        this.projectileSpeed = projectileSpeed;
        this.maxAmmo = maxAmmo;
        this.currentAmmo = maxAmmo;
        this.reloadTime = reloadTime;
        this.projectileCount = projectileCount;
    }

    public void shoot(Player player) {
        if (!canShoot()) {
            ActionBarAPI.sendActionBar(player, "§cSem munição!");
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
            return;
        }

        currentAmmo--;
        updateWeaponItem(player);

        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        double inaccuracy = (1.0 - accuracy) * 0.1;
        direction.add(new Vector(
                (Math.random() - 0.5) * inaccuracy,
                (Math.random() - 0.5) * inaccuracy,
                (Math.random() - 0.5) * inaccuracy
        )).normalize();

        for (double i = 0; i < range; i += 0.5) {
            Location particleLoc = eyeLocation.clone().add(direction.clone().multiply(i));
            player.getWorld().playEffect(particleLoc, Effect.SMALL_SMOKE, 1, 0);

            for (Entity entity : player.getWorld().getNearbyEntities(particleLoc, 0.5, 0.5, 0.5)) {
                if (entity instanceof LivingEntity && entity != player) {
                    ((LivingEntity) entity).damage(damage, player);
                    player.playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1.0f, 1.0f);
                    return;
                }
            }
        }

        player.playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1.0f, 1.0f);
        updateAmmoDisplay(player);
    }
    public void reload(Player player) {
        if (isReloading) {
            ActionBarAPI.sendActionBar(player, "§cJá está recarregando!");
            return;
        }
        if (currentAmmo >= maxAmmo) {
            ActionBarAPI.sendActionBar(player, "§aArma cheia!");
            return;
        }

        isReloading = true;
        ItemStack itemInHand = player.getInventory().getItemInHand();
        reloadingWeaponId = NBTUtils.getWeaponID(itemInHand);
        updateWeaponItem(player);

        reloadTimer = new BukkitRunnable() {
            int timeLeft = reloadTime;

            @Override
            public void run() {
                if (!isReloading || !isWeaponInHand(player)) {
                    cancelReload(player);
                    cancel();
                    return;
                }

                ActionBarAPI.sendActionBar(player, "§eRecarregando... " + timeLeft + "s");
                timeLeft--;

                if (timeLeft < 0) {
                    cancel();
                }
            }
        };
        reloadTimer.runTaskTimer(player.getServer().getPluginManager().getPlugin("esgotoserver"), 0L, 20L);

        reloadTaskId = player.getServer().getScheduler().runTaskLater(
                player.getServer().getPluginManager().getPlugin("esgotoserver"),
                () -> {
                    if (isWeaponInHand(player) && isReloading) {
                        currentAmmo = maxAmmo;
                        isReloading = false;
                        updateWeaponItem(player);
                        player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1.0f, 1.0f);
                        ActionBarAPI.sendActionBar(player, "§aRecarga concluída! Munição: " + currentAmmo + "/" + maxAmmo);
                    } else {
                        cancelReload(player);
                    }
                    reloadTaskId = -1;
                    reloadingWeaponId = null;
                },
                reloadTime * 20L
        ).getTaskId();
    }
    public boolean canShoot(){
        return currentAmmo > 0;
    }

    private boolean isWeaponInHand(Player player) {
        ItemStack item = player.getInventory().getItemInHand();
        String currentWeaponId = NBTUtils.getWeaponID(item);
        return currentWeaponId != null && currentWeaponId.equals(reloadingWeaponId);
    }

    public void cancelReload(Player player) {
        if (reloadTimer != null) {
            reloadTimer.cancel();
            reloadTimer = null;
        }
        if (reloadTaskId != -1) {
            player.getServer().getScheduler().cancelTask(reloadTaskId);
            reloadTaskId = -1;
        }
        if (isReloading) {
            isReloading = false;
            updateOriginalWeaponItem(player);
            ActionBarAPI.sendActionBar(player, "§cRecarga cancelada!", Math.max(20, reloadTime * 20));
        }
        reloadingWeaponId = null;
    }

    public void setCurrentAmmo(int currentAmmo) {
        this.currentAmmo = Math.max(0, Math.min(currentAmmo, maxAmmo));
    }

    private void updateWeaponItem(Player player) {
        ItemStack item = player.getInventory().getItemInHand();
        if (item != null) {
            ItemStack updatedItem = NBTUtils.applyWeaponNBT(item, this);
            player.getInventory().setItemInHand(updatedItem);
        }
    }

    private void updateAmmoDisplay(Player player) {
        ActionBarAPI.sendActionBar(player, "§fMunição: " + currentAmmo + "/" + maxAmmo);
    }
    public boolean isReloading() {
        return isReloading;
    }
    public void setReloading(boolean isReloading) {
        this.isReloading = isReloading;
    }

    private void updateOriginalWeaponItem(Player player) {
        if (reloadingWeaponId == null) return;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && reloadingWeaponId.equals(NBTUtils.getWeaponID(item))) {
                ItemStack updatedItem = NBTUtils.applyWeaponNBT(item, this);
                player.getInventory().setItem(i, updatedItem);
                return;
            }
        }
    }
}