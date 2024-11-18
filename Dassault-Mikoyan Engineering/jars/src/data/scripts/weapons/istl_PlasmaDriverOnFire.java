package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.DMEUtils;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class istl_PlasmaDriverOnFire implements OnFireEffectPlugin {
    //private static final Color FLASH_COLOR = new Color(155,100,255,235);
    //private static final float OFFSET = 15f; // Offset on weapon sprite

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        projectile.getVelocity().scale(MathUtils.getRandomNumberInRange(0.95f, 1.05f));
        
//        Vector2f weapon_location = weapon.getLocation();
//        ShipAPI ship = weapon.getShip();
//        Vector2f explosion_offset = DMEUtils.translate_polar(weapon_location, OFFSET +
//                                                          ((0.05f * 100f) - 2f), weapon.getCurrAngle());
//        engine.spawnExplosion(explosion_offset, ship.getVelocity(), FLASH_COLOR, 90f, 0.12f);
    }
}