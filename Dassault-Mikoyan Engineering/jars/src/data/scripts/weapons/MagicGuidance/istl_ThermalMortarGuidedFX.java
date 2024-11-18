package data.scripts.weapons.MagicGuidance;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
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
public class istl_ThermalMortarGuidedFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    
    private static final float OFFSET = 7f; // Offset on weapon sprite; shoud be set to turret offset
    private static final Color BOOM_COLOR = new Color(175,100,255,255);
    private static final Color NEBULA_COLOR = new Color(155,75,255,255);
    private static final float NEBULA_SIZE = 5f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 12f;
    private static final float NEBULA_DUR = 0.6f;
    private static final float NEBULA_RAMPUP = 0.1f;
    
    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine)
    {
        Vector2f weapon_location = weapon.getLocation();
        ShipAPI ship = weapon.getShip();
        float ship_facing = ship.getFacing();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f muzzle_location = MathUtils.getPointOnCircumference(weapon_location, OFFSET, ship_facing);

        Vector2f explosion_offset = DMEUtils.translate_polar(weapon_location, OFFSET + ((0.05f * 100f) - 2f), weapon.getCurrAngle());
        engine.addSwirlyNebulaParticle(explosion_offset,
            ship_velocity,
            NEBULA_SIZE,
            NEBULA_SIZE_MULT,
            NEBULA_RAMPUP,
            0.2f,
            NEBULA_DUR,
            NEBULA_COLOR,
            true
        );
        engine.addSmoothParticle(explosion_offset, ship_velocity, NEBULA_SIZE * 4, 0.75f, NEBULA_RAMPUP, NEBULA_DUR / 2, BOOM_COLOR);
        engine.spawnExplosion(explosion_offset, ship_velocity,  BOOM_COLOR, NEBULA_SIZE * 6, NEBULA_DUR * 0.75f);
    }
    
    private List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<DamagingProjectileAPI>();
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

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
                engine.addPlugin(new istl_MortarGuidanceProjScript(proj, target));
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