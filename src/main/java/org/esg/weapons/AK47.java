package org.esg.weapons;

import org.bukkit.ChatColor;
import org.esg.models.AmmoType;
import org.esg.models.Weapon;
import org.esg.models.WeaponType;

public class AK47 extends Weapon {

    public AK47() {
        super("AK47", WeaponType.RIFLE, AmmoType._762MM, 0.1, 80, 0.4, 1, 90, 32, 32, 2, 1);
    }
}
