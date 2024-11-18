package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.DMEUtils;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author HarmfulMechanic
 * Based on scripts by Trylobot, Uomoz and Cycerin
 */
public class istl_PlasmaMortarFX implements EveryFrameWeaponEffectPlugin {
    
    private static final float OFFSET = 10f; // Offset on weapon sprite; shoud be set to turret offset
    private static final Color FLASH_COLOR = new Color(175,125,255,255); //Color of muzzle flash explosion
//    private static final float FIRE_DURATION = 0.35f; // Firing cycle time (up + down)
//    private static final Color PARTICLE_COLOR = new Color(125,100,255,175); // Particle color
        
    // weapon state (per weapon instance)
    private float last_charge_level = 0.0f;
    private int last_weapon_ammo = 0;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }

        float charge_level = weapon.getChargeLevel();
        int weapon_ammo = weapon.getAmmo();

        if (charge_level > last_charge_level || weapon_ammo < last_weapon_ammo) {
            // shared vars
            Vector2f weapon_location = weapon.getLocation();
            ShipAPI ship = weapon.getShip();
            float ship_facing = ship.getFacing();
            Vector2f ship_velocity = ship.getVelocity();
            Vector2f muzzle_location = MathUtils.getPointOnCircumference(weapon_location, OFFSET, ship_facing);

//            // chargeup (fire button held down, not cooling down after firing)
//            if (charge_level > last_charge_level && weapon.isFiring()) {
//                //Fancy shit goes in here
//            }

            // muzzle flash on fire after charging; ammo decreased indicates shot fired
            if (weapon_ammo < last_weapon_ammo) {
                // do muzzle flash
                Vector2f explosion_offset = DMEUtils.translate_polar(weapon_location, OFFSET + ((0.05f * 100f) - 2f), weapon.getCurrAngle());
                engine.spawnExplosion(explosion_offset, ship.getVelocity(), FLASH_COLOR, 40f, 0.07f);

            }
        }

        last_charge_level = charge_level;
        last_weapon_ammo = weapon_ammo;
    }
}