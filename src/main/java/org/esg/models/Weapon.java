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
import org.esg.Manager.InvulnerabilityManager;
import org.esg.utils.MessageHandler;
import org.esg.Effects.SoundEffects;
import org.esg.utils.NBTUtils;
import org.esg.utils.WeaponUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Abstract base class for all weapons, handling shooting, reloading, and projectile mechanics.
 */
@Getter
public abstract class Weapon {

    private static final Logger LOGGER = Logger.getLogger(Weapon.class.getName());
    private static final double HAND_Y_OFFSET = -0.6;
    private static final double HAND_FORWARD_OFFSET = 1.2;
    private static final double TRACE_STEP = 0.5;
    private static final double HITBOX_WIDTH = 0.5;
    private static final double HITBOX_HEIGHT = 0.1;
    private static final long CACHE_VALIDITY_MS = 1000;
    private static final long CLEAR_MESSAGE_DELAY_TICKS = 40;

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
    protected int customInvulTicks;

    private transient int reloadTaskId = -1;
    private transient String reloadingWeaponId;
    private transient int reloadSlot = -1;

    private static final Map<UUID, Boolean> isFiring = new HashMap<>();
    private static final Map<UUID, Long> lastClickTimes = new HashMap<>();
    private static final Map<UUID, CachedReloadingState> reloadingCache = new HashMap<>();

    protected Weapon(String name, WeaponType type, AmmoType ammoType, double damage, double range,
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
        this.customInvulTicks = calculateInvulTicks(fireRate);
    }

    private static class CachedReloadingState {
        boolean isReloading;
        long timestamp;

        CachedReloadingState(boolean isReloading, long timestamp) {
            this.isReloading = isReloading;
            this.timestamp = timestamp;
        }
    }

    private int calculateInvulTicks(double fireRate) {
        if (fireRate <= 0) return 10;
        double ticksPerShot = 20.0 / fireRate;
        return Math.max(1, (int) Math.floor(ticksPerShot));
    }

    public void shoot(Player player) {
        LOGGER.info("Attempting to shoot with weapon: " + name + ", currentAmmo: " + currentAmmo + ", isReloading: " + isReloading);
        if (!canShoot(player)) {
            LOGGER.info("Cannot shoot: ammo=" + currentAmmo + ", reloading=" + isReloading(player));
            handleCannotShoot(player);
            return;
        }

        currentAmmo--;
        LOGGER.info("Shot fired by " + player.getName() + ", currentAmmo decremented to: " + currentAmmo);

        if (currentAmmo <= 0) {
            LOGGER.info("No ammo left after shot");
            handleNoAmmo(player);
            return;
        }

        LOGGER.info("Starting projectile with projectileSpeed: " + projectileSpeed + ", range: " + range);
        startProjectile(player);
        MessageHandler.sendAmmoStatus(player, currentAmmo, maxAmmo);
        WeaponUtils.updateWeaponInHand(player, this);
    }

    private void startProjectile(Player player) {
        Location handLocation = getHandLocation(player);
        Vector direction = calculateShotDirection(player.getEyeLocation());
        double distancePerTick = projectileSpeed / 20.0;
        int totalTicks = (int) Math.ceil(range / distancePerTick);
        SoundEffects.playShotAt(player, player.getLocation());

        final int numSubSteps = 15;

        new BukkitRunnable() {
            double distanceTraveled = 0.0;
            @Override
            public void run() {
                double stepDistance = distancePerTick / numSubSteps;
                boolean collisionDetected = false;

                for (int i = 0; i < numSubSteps; i++) {
                    distanceTraveled += stepDistance;
                    Location currentLoc = handLocation.clone().add(direction.clone().multiply(distanceTraveled));

                    if (currentLoc.getBlock().getType().isSolid()) {
                        ParticleCat.sendParticle(EnumParticle.SMOKE_NORMAL, currentLoc, 0, 0, 0, 0, 1);
                        SoundEffects.playShotAt(player, currentLoc);
                        collisionDetected = true;
                        break;
                    }


                    for (Entity entity : player.getWorld().getNearbyEntities(currentLoc, HITBOX_WIDTH, HITBOX_WIDTH, HITBOX_HEIGHT)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;
                            target.setNoDamageTicks(0);
                            InvulnerabilityManager.resetInvulTicks(target, 0);
                            target.damage(damage, player);
                            collisionDetected = true;
                            break;
                        }
                    }


                    ParticleCat.sendParticle(EnumParticle.SMOKE_NORMAL, currentLoc, 0, 0, 0, 0, 1);
                    if (collisionDetected) {
                        break;
                    }
                }


                if (collisionDetected || distanceTraveled >= range) {
                    cancel();
                }
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 1L);
    }

    private void handleCannotShoot(Player player) {
        if (isReloading(player)) {
            MessageHandler.sendReloading(player);
        } else {
            MessageHandler.sendNoAmmo(player);
            SoundEffects.playError(player);
        }
        isFiring.put(player.getUniqueId(), false);
        WeaponUtils.updateWeaponInHand(player, this);
    }

    private void handleNoAmmo(Player player) {
        MessageHandler.sendNoAmmo(player);
        SoundEffects.playError(player);
        isFiring.put(player.getUniqueId(), false);
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
        handLocation.setY(handLocation.getY() + HAND_Y_OFFSET);
        handLocation.add(direction.multiply(HAND_FORWARD_OFFSET));
        return handLocation;
    }

    private Vector calculateShotDirection(Location eyeLocation) {
        Vector direction = eyeLocation.getDirection().normalize();
        double inaccuracy = (1.0 - accuracy) * 0.1;
        double offsetX = (Math.random() - 0.5) * 2 * inaccuracy;
        double offsetY = (Math.random() - 0.5) * 2 * inaccuracy;
        double offsetZ = (Math.random() - 0.5) * 2 * inaccuracy;
        return direction.add(new Vector(offsetX, offsetY, offsetZ)).normalize();
    }

    private void traceProjectile(Player player, Location start, Vector direction) {
        double step = 0.2; //
        double hitboxWidth = 0.75; //
        double hitboxHeight = 1.5;

        for (double i = 0; i < range; i += step) {
            Location particleLoc = start.clone().add(direction.clone().multiply(i));
            if (particleLoc.getBlock().getType().isSolid()) {
                ParticleCat.sendParticle(EnumParticle.SMOKE_NORMAL, particleLoc, 0, 0, 0, 0, 1);
                SoundEffects.playShotAt(player, particleLoc);
                LOGGER.info("Projectile hit block at " + particleLoc.getX() + ", " + particleLoc.getY() + ", " + particleLoc.getZ());
                return;
            }
            ParticleCat.sendParticle(EnumParticle.SMOKE_NORMAL, particleLoc, 0, 0, 0, 0, 1);

            for (Entity entity : player.getWorld().getNearbyEntities(particleLoc, hitboxWidth, hitboxWidth, hitboxHeight)) {
                if (entity instanceof LivingEntity && entity != player) {
                    LivingEntity target = (LivingEntity) entity;
                    UUID targetUUID = target.getUniqueId();



                        target.setNoDamageTicks(0);
                        InvulnerabilityManager.resetInvulTicks(target, 0);

                        target.damage(damage, player);



                        SoundEffects.playShotAt(player, particleLoc);

                }
            }
        }
    }
    public void reload(Player player) {
        if (isReloading(player)) {
            LOGGER.info("Reload blocked: already reloading for player " + player.getName());
            MessageHandler.sendAlreadyReloading(player);
            return;
        }
        if (currentAmmo >= maxAmmo) {
            LOGGER.info("Reload blocked: ammo full for player " + player.getName());
            MessageHandler.sendFullAmmo(player);
            return;
        }
        startReload(player);
    }

    private void startReload(Player player) {
        LOGGER.info("Starting reload for player: " + player.getName());
        isReloading = true;
        ItemStack itemInHand = player.getInventory().getItemInHand();
        reloadingWeaponId = WeaponUtils.getWeaponId(itemInHand);
        reloadSlot = player.getInventory().getHeldItemSlot();

        isFiring.put(player.getUniqueId(), false);
        WeaponUtils.updateWeaponInHand(player, this);
        reloadingCache.put(player.getUniqueId(), new CachedReloadingState(true, System.currentTimeMillis()));

        scheduleReloadTask(player);
    }

    private int validateReloadSlot(int slot) {
        if (slot < 0 || slot > 8) {
            LOGGER.warning("Invalid reloadSlot detected: " + slot + ", setting to 0");
            return 0;
        }
        return slot;
    }

    private void scheduleReloadTask(Player player) {
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
        LOGGER.info("canShoot for player " + player.getName() + ": currentAmmo=" + currentAmmo +
                ", isReloading=" + isReloading(player) + ", result=" + canShoot);
        return canShoot;
    }

    public boolean isReloading(Player player) {
        UUID playerId = player.getUniqueId();
        CachedReloadingState cached = reloadingCache.get(playerId);
        if (cached != null && (System.currentTimeMillis() - cached.timestamp) < CACHE_VALIDITY_MS) {
            return cached.isReloading;
        }

        ItemStack itemInHand = player.getInventory().getItemInHand();
        boolean result;
        if (itemInHand != null && WeaponUtils.isSameWeapon(itemInHand, reloadingWeaponId)) {
            Weapon weaponFromItem = WeaponUtils.getWeaponFromItem(itemInHand, player);
            if (weaponFromItem != null) {
                LOGGER.info("isReloading for player " + player.getName() +
                        ": weaponFromItem.isReloading=" + weaponFromItem.isReloading +
                        ", currentAmmo=" + weaponFromItem.currentAmmo);
                result = weaponFromItem.isReloading;
            } else {
                LOGGER.info("isReloading for player " + player.getName() +
                        ": fallback to local isReloading=" + isReloading +
                        ", currentAmmo=" + currentAmmo);
                result = isReloading;
            }
        } else {
            LOGGER.info("isReloading for player " + player.getName() +
                    ": weapon not in hand, resetting isReloading to false");
            isReloading = false;
            result = false;
        }

        reloadingCache.put(playerId, new CachedReloadingState(result, System.currentTimeMillis()));
        return result;
    }

    private boolean checkReloadingState(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInHand();
        if (itemInHand == null || !WeaponUtils.isSameWeapon(itemInHand, reloadingWeaponId)) {
            LOGGER.info("isReloading for player " + player.getName() +
                    ": fallback to local isReloading=" + isReloading + ", currentAmmo=" + currentAmmo);
            return isReloading;
        }

        Weapon weaponFromItem = WeaponUtils.getWeaponFromItem(itemInHand, player);
        if (weaponFromItem != null) {
            LOGGER.info("isReloading for player " + player.getName() +
                    ": weaponFromItem.isReloading=" + weaponFromItem.isReloading +
                    ", currentAmmo=" + weaponFromItem.currentAmmo);
            return weaponFromItem.isReloading;
        }

        LOGGER.info("isReloading for player " + player.getName() +
                ": fallback to local isReloading=" + isReloading + ", currentAmmo=" + currentAmmo);
        return isReloading;
    }

    public void cancelReload(Player player) {
        if (!isReloading(player)) return;

        LOGGER.info("Cancelling reload for player: " + player.getName() + ", reloadSlot=" + reloadSlot);
        isReloading = false;
        isFiring.put(player.getUniqueId(), false);
        if (reloadTaskId != -1) {
            player.getServer().getScheduler().cancelTask(reloadTaskId);
            reloadTaskId = -1;
        }

        // Atualiza o item no slot de recarregamento
        if (reloadSlot >= 0 && reloadSlot <= 8) {
            ItemStack itemInSlot = player.getInventory().getItem(reloadSlot);
            if (itemInSlot != null && WeaponUtils.isSameWeapon(itemInSlot, reloadingWeaponId)) {
                WeaponUtils.updateWeaponInSlot(player, reloadSlot, this);
                LOGGER.info("Updated NBT in slot " + reloadSlot + " with isReloading=false");
            } else {
                LOGGER.warning("Weapon no longer in slot " + reloadSlot + " during cancelReload for player: " + player.getName());
            }
        } else {
            LOGGER.warning("Invalid reloadSlot: " + reloadSlot + ", skipping update");
        }

        ItemStack itemInHand = player.getInventory().getItemInHand();
        if (itemInHand != null && WeaponUtils.isSameWeapon(itemInHand, reloadingWeaponId)) {
            WeaponUtils.updateWeaponInHand(player, this);
        }

        MessageHandler.sendReloadCancelled(player);
        reloadingCache.put(player.getUniqueId(), new CachedReloadingState(false, System.currentTimeMillis()));

        reloadSlot = -1;
        reloadingWeaponId = null;

        scheduleClearMessage(player);
    }

    private void scheduleClearMessage(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isReloading(player)) {
                    MessageHandler.clear(player);
                }
            }
        }.runTaskLater(Main.getPlugin(), CLEAR_MESSAGE_DELAY_TICKS);
    }

    public void setCurrentAmmo(int currentAmmo) {
        this.currentAmmo = Math.max(0, Math.min(currentAmmo, maxAmmo));
    }

    public void setReloading(boolean reloading) {
        this.isReloading = reloading;
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

        LOGGER.info("Completing reload for player: " + player.getName());
        currentAmmo = maxAmmo;
        isReloading = false;
        isFiring.put(player.getUniqueId(), false);
        WeaponUtils.updateWeaponInHand(player, this);
        SoundEffects.playReloadComplete(player);
        MessageHandler.sendReloadComplete(player, currentAmmo, maxAmmo);

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
            private final double shotsPerTick = fireRate / 20.0;
            private double shotAccumulator = 0.0;

            @Override
            public void run() {
                if (!isFiring.getOrDefault(player.getUniqueId(), false) || !canShoot(player)) {
                    isFiring.put(player.getUniqueId(), false);
                    ItemStack itemInHand = player.getInventory().getItemInHand();
                    if (itemInHand != null && NBTUtils.getWeaponID(itemInHand) != null && WeaponUtils.isSameWeapon(itemInHand, reloadingWeaponId)) {
                        WeaponUtils.updateWeaponInHand(player, Weapon.this);
                    }
                    cancel();
                    return;
                }

                shotAccumulator += shotsPerTick;
                int shotsThisTick = (int) shotAccumulator;
                shotAccumulator -= shotsThisTick;

                for (int i = 0; i < shotsThisTick && canShoot(player); i++) {
                    shoot(player);
                }

                if (!canShoot(player)) {
                    isFiring.put(player.getUniqueId(), false);
                    ItemStack itemInHand = player.getInventory().getItemInHand();
                    if (itemInHand != null && NBTUtils.getWeaponID(itemInHand) != null && WeaponUtils.isSameWeapon(itemInHand, reloadingWeaponId)) {
                        WeaponUtils.updateWeaponInHand(player, Weapon.this);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 1L);
    }
}