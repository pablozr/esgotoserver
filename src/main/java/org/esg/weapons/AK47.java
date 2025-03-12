package org.esg.weapons;

import org.bukkit.ChatColor;
import org.esg.models.AmmoType;
import org.esg.models.Weapon;
import org.esg.models.WeaponType;

public class AK47 extends Weapon {

    public AK47() {
        super("AK47", WeaponType.RIFLE, AmmoType._762MM, 500, 80, 1.0, 30, 60, 80, 80, 2, 1);
    }
}
