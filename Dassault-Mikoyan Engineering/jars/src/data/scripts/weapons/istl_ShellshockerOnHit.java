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

import org.lazywizard.lazylib.MathUtils;

import java.awt.Color;

public class istl_ShellshockerOnHit implements OnHitEffectPlugin {

    private static final int MIN_ARCS = 3;
    private static final int MAX_ARCS = 5;
    private static final float ARC_DAMAGE = 0.1667f;
    private static final float ARC_EMP = 0.2f; //SATAN!
    private static final String SFX = "istl_kinetic_crit_med";
    // -- flux raise --------------------------------------------------------
    private static final float FLUXRAISE_MULT = 0.6f;
    // -- explosion graphics -------------------------------------------------
    private static final Color NEBULA_COLOR = new Color(75,90,255,200);
    private static final float NEBULA_SIZE = 10f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 20f;
    private static final float NEBULA_DUR = 0.8f;
    private static final float NEBULA_RAMPUP = 0.15f;
    private static final Color EXPLOSION_COLOR = new Color(125,135,255,255);
    private static final float EXPLOSION_DUR_MULT = 0.75f;
    // -- stuff for tweaking particle characteristics ------------------------
    private static final Color PARTICLE_COLOR = new Color(120,135,255,255);
    private static final float PARTICLE_SIZE = 7f;
    private static final float PARTICLE_BRIGHTNESS = 255f;
    private static final float PARTICLE_DURATION = 2.0f;
    private static final int PARTICLE_COUNT = 5;
    // -- particle geometry --------------------------------------------------
    private static final float CONE_ANGLE = 150f;
    private static final float VEL_MIN = 0.08f;
    private static final float VEL_MAX = 0.2f;
    // one half of the angle. used internally, don't mess with this
    private static final float A_2 = CONE_ANGLE / 2;
    
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
             //particles
            float speed = projectile.getVelocity().length();
            float facing = projectile.getFacing();
            for (int i2 = 0; i2 <= PARTICLE_COUNT; i2++)
            {
                float angle = MathUtils.getRandomNumberInRange(facing - A_2,
                        facing + A_2);
                float vel = MathUtils.getRandomNumberInRange(speed * -VEL_MIN,
                        speed * -VEL_MAX);
                Vector2f vector = MathUtils.getPointOnCircumference(null,
                        vel,
                        angle);
                engine.addHitParticle(point,
                        vector,
                        PARTICLE_SIZE,
                        PARTICLE_BRIGHTNESS,
                        PARTICLE_DURATION,
                        PARTICLE_COLOR);
            }
            // calculate a number to raise target flux by
            float fluxmult = projectile.getDamageAmount() * FLUXRAISE_MULT;
            // get target max flux level
            float maxflux = targetship.getMaxFlux();
            //Check that the target can handle the flux; if so, raise target ship flux on hull hit
            if (maxflux > (fluxmult * 1.5f)) {
                targetship.getFluxTracker().increaseFlux(fluxmult, true);              
            }
        }
    }
}