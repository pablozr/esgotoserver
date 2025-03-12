package org.esg.models;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.esg.Effects.ParticleCat;
import org.esg.Main;
import org.esg.utils.MessageHandler;
import org.esg.Effects.SoundEffects;
import org.esg.utils.WeaponUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public abstract class Weapon {
    protected String name;
    protected WeaponType type;
    protected AmmoType ammoType;
    protected double damage;
    protected double range;
    protected double accuracy;
    protected double fireRate;
    protected double projectileSpeed;
    protected int maxAmmo;
    protected int currentAmmo;
    protected int reloadTime;
    protected int projectileCount;
    protected boolean isReloading = false;
    private transient int reloadTaskId = -1;
    private transient String reloadingWeaponId;
    private transient int reloadSlot = -1;

    private static final Map<UUID, Boolean> isFiring = new HashMap<>();
    private static final Map<UUID, Long> lastClickTimes = new HashMap<>();
    // Cache para isReloading
    private static final Map<UUID, CachedReloadingState> reloadingCache = new HashMap<>();

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
        this.currentAmmo = currentAmmo;
        this.reloadTime = reloadTime;
        this.projectileCount = projectileCount;
    }

    // Classe para armazenar o estado de recarga em cache
    private static class CachedReloadingState {
        boolean isReloading;
        long timestamp;

        CachedReloadingState(boolean isReloading, long timestamp) {
            this.isReloading = isReloading;
            this.timestamp = timestamp;
        }
    }

    public void shoot(Player player) {
        if (!canShoot(player)) {
            if (isReloading(player)) {
                MessageHandler.sendReloading(player);
            } else {
                MessageHandler.sendNoAmmo(player);
                SoundEffects.playError(player);
            }
            isFiring.put(player.getUniqueId(), false);
            WeaponUtils.updateWeaponInHand(player, this);
            return;
        }

        currentAmmo--;
        System.out.println("[Weapon] Shot fired by " + player.getName() + ", currentAmmo decremented to: " + currentAmmo);

        if (currentAmmo <= 0) {
            MessageHandler.sendNoAmmo(player);
            SoundEffects.playError(player);
            isFiring.put(player.getUniqueId(), false);
            WeaponUtils.updateWeaponInHand(player, this);
            return;
        }

        performShot(player);
        MessageHandler.sendAmmoStatus(player, currentAmmo, maxAmmo);
        WeaponUtils.updateWeaponInHand(player, this);
    }

    private void performShot(Player player) {
        Location handLocation = getHandLocation(player);
        Vector direction = calculateShotDirection(player.getEyeLocation());
        traceProjectile(player, handLocation, direction);
        SoundEffects.playShotAt(player, handLocation);
    }

    private Location getHandLocation(Player player) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        Location handLocation = eyeLocation.clone();
        handLocation.setY(handLocation.getY() - 0.6);
        handLocation.add(direction.multiply(1.2));

        return handLocation;
    }

    private Vector calculateShotDirection(Location eyeLocation) {
        Vector direction = eyeLocation.getDirection().normalize();
        double inaccuracy = (1.0 - accuracy) * 0.1;
        double offsetX = (Math.random() - 0.5) * 2 * inaccuracy;
        double offsetY = (Math.random() - 0.5) * 2 * inaccuracy;
        double offsetZ = (Math.random() - 0.5) * 2 * inaccuracy;
        direction.add(new Vector(offsetX, offsetY, offsetZ)).normalize();
        return direction;
    }

    private void traceProjectile(Player player, Location start, Vector direction) {
        for (double i = 0; i < range; i += 0.5) {
            Location particleLoc = start.clone().add(direction.clone().multiply(i));
            ParticleCat.sendParticle(EnumParticle.SMOKE_NORMAL, particleLoc, 0, 0, 0, 0, 1);

            for (Entity entity : player.getWorld().getNearbyEntities(particleLoc, 0.5, 0.5, 0.1)) {
                if (entity instanceof LivingEntity && entity != player) {
                    ((LivingEntity) entity).damage(damage, player);
                    SoundEffects.playShotAt(player, particleLoc);
                    return;
                }
            }
        }
    }

    public void reload(Player player) {
        if (isReloading(player)) {
            System.out.println("[Weapon] Reload blocked: already reloading for player " + player.getName());
            MessageHandler.sendAlreadyReloading(player);
            return;
        }
        if (currentAmmo >= maxAmmo) {
            System.out.println("[Weapon] Reload blocked: ammo full for player " + player.getName());
            MessageHandler.sendFullAmmo(player);
            return;
        }
        startReload(player);
    }

    private void startReload(Player player) {
        System.out.println("[Weapon] Starting reload for player: " + player.getName());
        isReloading = true;
        ItemStack itemInHand = player.getInventory().getItemInHand();
        reloadingWeaponId = WeaponUtils.getWeaponId(itemInHand);
        reloadSlot = player.getInventory().getHeldItemSlot();
        if (reloadSlot < 0 || reloadSlot > 8) {
            System.out.println("[Weapon] Invalid reloadSlot detected: " + reloadSlot + ", setting to 0");
            reloadSlot = 0; // Valor padrão seguro
        }

        isFiring.put(player.getUniqueId(), false);
        WeaponUtils.updateWeaponInHand(player, this);
        // Atualizar o cache
        reloadingCache.put(player.getUniqueId(), new CachedReloadingState(true, System.currentTimeMillis()));

        reloadTaskId = new BukkitRunnable() {
            int ticksLeft = reloadTime * 20;

            @Override
            public void run() {
                if (!isReloading(player) || !isWeaponInHand(player)) {
                    cancelReload(player);
                    cancel();
                    return;
                }

                int secondsLeft = ticksLeft / 20;
                MessageHandler.sendReloadProgress(player, secondsLeft);

                if (ticksLeft <= 0) {
                    completeReload(player);
                    cancel();
                }

                ticksLeft--;
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 1L).getTaskId();
    }

    public boolean canShoot(Player player) {
        boolean canShoot = currentAmmo > 0 && !isReloading(player);
        System.out.println("[Weapon] canShoot for player " + player.getName() + ": currentAmmo=" + currentAmmo + ", isReloading=" + isReloading(player) + ", result=" + canShoot);
        return canShoot;
    }

    public boolean isReloading(Player player) {
        UUID playerId = player.getUniqueId();
        // Verificar cache
        CachedReloadingState cached = reloadingCache.get(playerId);
        if (cached != null && (System.currentTimeMillis() - cached.timestamp) < 1000) { // Cache válido por 1 segundo
            return cached.isReloading;
        }

        ItemStack itemInHand = player.getInventory().getItemInHand();
        boolean result;
        if (itemInHand != null && WeaponUtils.isSameWeapon(itemInHand, reloadingWeaponId)) {
            Weapon weaponFromItem = WeaponUtils.getWeaponFromItem(itemInHand, player);
            if (weaponFromItem != null) {
                System.out.println("[Weapon] isReloading for player " + player.getName() + ": weaponFromItem.isReloading=" + weaponFromItem.isReloading + ", currentAmmo=" + weaponFromItem.currentAmmo);
                result = weaponFromItem.isReloading;
            } else {
                System.out.println("[Weapon] isReloading for player " + player.getName() + ": fallback to local isReloading=" + isReloading + ", currentAmmo=" + currentAmmo);
                result = isReloading;
            }
        } else {
            System.out.println("[Weapon] isReloading for player " + player.getName() + ": fallback to local isReloading=" + isReloading + ", currentAmmo=" + currentAmmo);
            result = isReloading;
        }

        // Atualizar o cache
        reloadingCache.put(playerId, new CachedReloadingState(result, System.currentTimeMillis()));
        return result;
    }

    public void cancelReload(Player player) {
        if (!isReloading(player)) return;

        System.out.println("[Weapon] Cancelling reload for player: " + player.getName() + ", reloadSlot=" + reloadSlot);
        isReloading = false;
        isFiring.put(player.getUniqueId(), false);
        if (reloadTaskId != -1) {
            player.getServer().getScheduler().cancelTask(reloadTaskId);
            reloadTaskId = -1;
        }

        if (reloadSlot >= 0 && reloadSlot <= 8) {
            WeaponUtils.updateWeaponInSlot(player, reloadSlot, this);
            System.out.println("[Weapon] Updated NBT in slot " + reloadSlot + " with isReloading=false");
        } else {
            System.out.println("[Weapon] Invalid reloadSlot: " + reloadSlot + ", skipping update");
        }
        MessageHandler.sendReloadCancelled(player);

        // Atualizar o cache
        reloadingCache.put(player.getUniqueId(), new CachedReloadingState(false, System.currentTimeMillis()));

        reloadSlot = -1;
        reloadingWeaponId = null;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isReloading(player)) MessageHandler.clear(player);
            }
        }.runTaskLater(Main.getPlugin(), 40L);
    }

    public void setCurrentAmmo(int currentAmmo) {
        this.currentAmmo = Math.max(0, Math.min(currentAmmo, maxAmmo));
    }

    public void setReloading(boolean reloading) {
        this.isReloading = reloading;
        // Atualizar o cache ao alterar o estado
        if (reloadingWeaponId != null) {
            Player player = Main.getPlugin().getServer().getPlayer(reloadingWeaponId);
            if (player != null) {
                reloadingCache.put(player.getUniqueId(), new CachedReloadingState(reloading, System.currentTimeMillis()));
            }
        }
    }

    public String getReloadingWeaponId() {
        return reloadingWeaponId;
    }

    private void completeReload(Player player) {
        if (!isReloading(player) || !isWeaponInHand(player)) return;

        System.out.println("[Weapon] Completing reload for player: " + player.getName());
        currentAmmo = maxAmmo;
        isReloading = false;
        isFiring.put(player.getUniqueId(), false);
        WeaponUtils.updateWeaponInHand(player, this);
        SoundEffects.playReloadComplete(player);
        MessageHandler.sendReloadComplete(player, currentAmmo, maxAmmo);

        // Atualizar o cache
        reloadingCache.put(player.getUniqueId(), new CachedReloadingState(false, System.currentTimeMillis()));

        reloadTaskId = -1;
        reloadSlot = -1;
        reloadingWeaponId = null;
    }

    private boolean isWeaponInHand(Player player) {
        ItemStack item = player.getInventory().getItemInHand();
        return WeaponUtils.isSameWeapon(item, reloadingWeaponId);
    }

    public static Map<UUID, Boolean> getIsFiring() {
        return isFiring;
    }

    public static Map<UUID, Long> getLastClickTimes() {
        return lastClickTimes;
    }

    public void startFiring(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isFiring.getOrDefault(player.getUniqueId(), false) || !canShoot(player)) {
                    isFiring.put(player.getUniqueId(), false);
                    WeaponUtils.updateWeaponInHand(player, Weapon.this);
                    cancel();
                    return;
                }
                shoot(player);
            }
        }.runTaskTimer(Main.getPlugin(), 0L, (long) (20 / fireRate));
    }
}