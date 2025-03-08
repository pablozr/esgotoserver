package org.esg.models;

import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
    protected int projectileCount; // Quantidade de projéteis disparado por clique

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

    public void shoot(Player player){
        if (!canShoot()){
            player.sendMessage("Sem munição! Recarregue com Shift.");
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
            return;
        }

        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        double inaccuracy = (1.0 - accuracy) * 0.1; // Quanto menor a precisão, maior o desvio
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
                currentAmmo--;
                player.playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1.0f, 1.0f);
                return;
                }
            }
        }
        currentAmmo--;
        player.playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1.0f, 1.0f);
    }

    private boolean isReloading = false;

    public void reload(Player player){
        if (currentAmmo >= maxAmmo) {
            player.sendMessage("A arma já está totalmente carregada!");
            return;
        }
        isReloading = true;

        player.sendMessage("Recarregando " + name + "...");
        player.getServer().getScheduler().runTaskLater(
                player.getServer().getPluginManager().getPlugin("Main"),
                () -> {
                    currentAmmo = maxAmmo;
                    player.sendMessage(name + " recarregada!");
                    player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1.0f, 1.0f);
                },
                reloadTime * 20L // Converte segundos para ticks (20 ticks = 1 segundo)
        );
    }
    public boolean canShoot(){
        return currentAmmo > 0;
    }

    public void setCurrentAmmo(int currentAmmo) {
        this.currentAmmo = Math.max(0, Math.min(currentAmmo, maxAmmo));
    }

}