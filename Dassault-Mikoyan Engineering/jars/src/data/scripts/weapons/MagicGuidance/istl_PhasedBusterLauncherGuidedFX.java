package data.scripts.weapons.MagicGuidance;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.DMEUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author HarmfulMechanic
 * Based on scripts by Trylobot, Uomoz, Cycerin, and Nicke535
 */
public class istl_PhasedBusterLauncherGuidedFX implements EveryFrameWeaponEffectPlugin {
    
    private static final float OFFSET = 5f; // Offset on weapon sprite; shoud be set to turret offset
    private static final Color FLASH_COLOR = new Color(185,175,100,255); //Color of muzzle flash explosion
    private static final float FLASH_SIZE = 135f; //Size of muzzle flash explosion
//    private static final float FIRE_DURATION = 0.35f; // Firing cycle time (up + down)
//    private static final Color PARTICLE_COLOR = new Color(125,100,255,175); // Particle color
        
    // weapon state (per weapon instance)
    private float last_charge_level = 0.0f;
    private int last_weapon_ammo = 0;
    
    private List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<DamagingProjectileAPI>();
    
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
                engine.spawnExplosion(explosion_offset, ship.getVelocity(), FLASH_COLOR, FLASH_SIZE, 0.1f);

            }
        }

        last_charge_level = charge_level;
        last_weapon_ammo = weapon_ammo;

        ShipAPI source = weapon.getShip();
        ShipAPI target = null;

        if(source.getWeaponGroupFor(weapon)!=null ){
            //Autofire management.
            if(source.getWeaponGroupFor(weapon).isAutofiring()  //if autofire is on for this weapon group.
                    && source.getSelectedGroupAPI()!=source.getWeaponGroupFor(weapon)){ //if the autofire group isn't selected.
                target = source.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTargetShip();
            }
            else {
                target = source.getShipTarget();
            }
        }

        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200f)) {
            if (proj.getWeapon() == weapon && !alreadyRegisteredProjectiles.contains(proj)
            		&& engine.isEntityInPlay(proj) && !proj.didDamage()) {
                engine.addPlugin(new istl_BusterGuidanceProjScript(proj, target));
                alreadyRegisteredProjectiles.add(proj);
            }
        }

        //Tidy up the list of registered projectiles.
        List<DamagingProjectileAPI> cloneList = new ArrayList<>(alreadyRegisteredProjectiles);
        for (DamagingProjectileAPI proj : cloneList) {
            if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
                alreadyRegisteredProjectiles.remove(proj);
            }
        }
    }
}