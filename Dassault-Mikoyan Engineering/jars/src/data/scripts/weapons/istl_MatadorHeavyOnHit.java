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

public class istl_MatadorHeavyOnHit implements OnHitEffectPlugin
{
    // -- crit damage -------------------------------------------------------
    private static final float CRIT_DAMAGE_MIN_MULT = 1.5f;
    private static final float CRIT_DAMAGE_MAX_MULT = 2.0f; 
    private static final float CRIT_CHANCE = 0.2f;
    //  -- flux raise ----------------------------------------------------------
    private static final float FLUXRAISE_MULT = 0.25f;
    //  -- crit fx ----------------------------------------------------------
    private static final Color NEBULA_COLOR = new Color(75,125,200,215);
    private static final float NEBULA_SIZE = 6f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 15f;
    private static final float NEBULA_DUR = 0.8f;
    private static final float NEBULA_RAMPUP = 0.1f;
    private static final String SFX = "istl_energy_crit_sm";
    // -- stuff for tweaking particle characteristics ------------------------
    private static final Color PARTICLE_COLOR = new Color(235,235,255,255);
    private static final float PARTICLE_SIZE = 5f;
    private static final float PARTICLE_BRIGHTNESS = 255f;
    private static final float PARTICLE_DURATION = 1.0f;
    private static final int PARTICLE_COUNT = 3;
    // -- particle geometry --------------------------------------------------
    private static final float CONE_ANGLE = 150f;
    private static final float VEL_MIN = 0.08f;
    private static final float VEL_MAX = 0.12f;
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
        if (shieldHit && target instanceof ShipAPI) {
        ShipAPI targetship = (ShipAPI) target;
        // calculate a number to raise target flux by
        float fluxmult = projectile.getDamageAmount() * FLUXRAISE_MULT;
        //Raise target ship flux on hull hit
        targetship.getFluxTracker().increaseFlux(fluxmult, false);
        }         
        // check whether or not we want to apply critical damage
        if (target instanceof ShipAPI && !shieldHit && Math.random() <= CRIT_CHANCE)
        {
            //calculate the crit min/max damage based on projectile damage.
            float critminmult = projectile.getDamageAmount() * CRIT_DAMAGE_MIN_MULT;
            float critmaxmult = projectile.getDamageAmount() * CRIT_DAMAGE_MAX_MULT;
            // apply the extra damage to the target
            engine.applyDamage(target, point, // where to apply damage
                    MathUtils.getRandomNumberInRange(
                            critminmult, critmaxmult),
                    DamageType.ENERGY, // damage type
                    0f, // amount of EMP damage (none)
                    false, // does this bypass shields? (no)
                    false, // does this deal soft flux? (no)
                    projectile.getSource());
                //do visual effects
                engine.addNebulaParticle(point,
                    target.getVelocity(),
                    NEBULA_SIZE,
                    NEBULA_SIZE_MULT,
                    NEBULA_RAMPUP,
                    0.2f,
                    NEBULA_DUR,
                    NEBULA_COLOR,
                    true
                );
                    engine.spawnExplosion(point, 
                    target.getVelocity(),
                    NEBULA_COLOR, // color of the explosion
                    NEBULA_SIZE * 4,
                    NEBULA_DUR / 2
                );

            float speed = projectile.getVelocity().length();
            float facing = projectile.getFacing();
            for (int i = 0; i <= PARTICLE_COUNT; i++)
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
            //play a sound
            Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
        }
    }
}
