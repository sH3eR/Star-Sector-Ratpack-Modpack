package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import org.lwjgl.util.vector.Vector2f;

import java.util.Random;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

public class istl_ElectroEMPOnHit  implements OnHitEffectPlugin {

    private static final int MIN_ARCS = 2;
    private static final int MAX_ARCS = 4;
    private static final float ARC_DAMAGE = 0.25f;
    private static final float ARC_EMP = 0.6f; //SATAN!
    private static final String SFX = "istl_energy_crit";
    //private static final float FLUXRAISE = 400f; //depreciated
    private static final float FLUXRAISE_MULT = 1f;
    // -- explosion graphics -------------------------------------------------
    private static final Color NEBULA_COLOR = new Color(75,90,255,200);
    private static final float NEBULA_SIZE = 7f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 20f;
    private static final float NEBULA_DUR = 0.6f;
    private static final float NEBULA_RAMPUP = 0.12f;
    private static final Color EXPLOSION_COLOR = new Color(125,135,255,255);
    private static final float EXPLOSION_DUR_MULT = 0.75f;
    
    @Override
    public void onHit(DamagingProjectileAPI projectile,
        CombatEntityAPI target,
        Vector2f point,
        boolean shieldHit,
        ApplyDamageResultAPI damageResult, 
        CombatEngineAPI engine)
    {       
        // get the target's velocity to render the crit FX
        Vector2f v_target = new Vector2f(target.getVelocity());    
        // do visual effects
        engine.addNebulaParticle(point,
            v_target,
            NEBULA_SIZE,
            NEBULA_SIZE_MULT,
            NEBULA_RAMPUP,
            0.3f,
            NEBULA_DUR,
            NEBULA_COLOR,
            true
        );
        engine.spawnExplosion(point,
            v_target,
            EXPLOSION_COLOR, // color of the explosion
            NEBULA_SIZE * 3,
            NEBULA_DUR * EXPLOSION_DUR_MULT
        );  
        if (!shieldHit && target instanceof ShipAPI) {
            ShipAPI targetship = (ShipAPI) target;

            float dam = projectile.getDamageAmount() * ARC_DAMAGE;
            float emp = projectile.getEmpAmount() * ARC_EMP;

            int arcs = MathUtils.getRandomNumberInRange(MIN_ARCS, MAX_ARCS);
            
            for (int i = 0; i < arcs; i++) {
                engine.spawnEmpArc(projectile.getSource(), point, target, target,
                    DamageType.ENERGY, // damage type
                    dam, // damage
                    emp, // emp 
                    100000f, // max range of arcs (on target)
                    "tachyon_lance_emp_impact", // sound
                    25f, // thickness
                    new Color(50, 55, 155, 255), // fringe color
                    new Color(200, 220, 255, 255) // core color
                    );
            }
            Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
            // calculate a number to raise target flux by
            float fluxmult = projectile.getDamageAmount() * FLUXRAISE_MULT;
            // get target max flux level
            float maxflux = targetship.getMaxFlux();
            //Check that the target can handle the flux; if so, raise target ship flux on hull hit
            if (maxflux > (fluxmult * 1.5)) {
                targetship.getFluxTracker().increaseFlux(fluxmult, true);              
            }
        }
    }
}