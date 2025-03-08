package org.esg.weapons;

import org.esg.models.AmmoType;
import org.esg.models.Weapon;
import org.esg.models.WeaponType;

public class AK47 extends Weapon {

    public AK47() {
        super("AK47", WeaponType.RIFLE, AmmoType._762MM, 7.0, 20, 0.85, 2.0, 60, 30, 30, 2, 1);
    }
}
