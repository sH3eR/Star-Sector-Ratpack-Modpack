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

import java.util.Random;
import java.awt.Color;

public class istl_PhaseBlasterOnHit implements OnHitEffectPlugin
{
    // -- crit damage -------------------------------------------------------
    //private static final int CRIT_DAMAGE_MIN = 75;
    //private static final int CRIT_DAMAGE_MAX = 125;
    private static final float CRIT_DAMAGE_MIN_MULT = 0.375f;
    private static final float CRIT_DAMAGE_MAX_MULT = 0.625f;    
    private static final float CRIT_CHANCE = 1.0f;
    //  -- crit fx ----------------------------------------------------------
    private static final Color EXPLOSION_COLOR = new Color(90,255,200,255);
    private static final Color NEBULA_COLOR = new Color(25,255,155,255);
    private static final float NEBULA_SIZE = 9f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 15f;
    private static final float NEBULA_DUR = 0.75f;
    private static final float NEBULA_RAMPUP = 0.1f;
    // color of spawned particles
    private static final Color BRIGHT_COLOR = new Color(155,255,225,255);
    private static final Color DIM_COLOR = new Color(0,100,100,30);
    private static final float PARTICLE_SIZE = 5f;
    private static final float PARTICLE_BRIGHTNESS = 255f;
    private static final float PARTICLE_DURATION = 1.2f;
    private static final int PARTICLE_COUNT = 4;
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
            CombatEngineAPI engine
        )
    {
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
                DamageType.FRAGMENTATION, // damage type
                0f, // amount of EMP damage (none)
                false, // does this bypass shields? (no)
                false, // does this deal soft flux? (no)
                projectile.getSource()
            );
        }
        // do visual effects ---------------------------------------------
        engine.spawnExplosion(point,
            target.getVelocity(),
            EXPLOSION_COLOR, // color of the explosion
            NEBULA_SIZE * 5, // sets the size of the explosion
            NEBULA_DUR * 0.4f // how long the explosion lingers for
        );
        engine.addNebulaParticle(point,
            target.getVelocity(),
            NEBULA_SIZE,
            NEBULA_SIZE_MULT,
            NEBULA_RAMPUP,
            0.25f,
            NEBULA_DUR,
            NEBULA_COLOR,
            true
        );

        float speed = projectile.getVelocity().length();
        float facing = projectile.getFacing();
        for (int i = 0; i <= PARTICLE_COUNT; i++)
        {
            float angle = MathUtils.getRandomNumberInRange(
                facing - A_2,
                facing + A_2
            );
            float vel = MathUtils.getRandomNumberInRange(
                speed * -VEL_MIN,
                speed * -VEL_MAX
            );
            Vector2f vector = MathUtils.getPointOnCircumference(
                null,
                vel,
                angle
            );
            engine.addHitParticle(point,
                vector,
                PARTICLE_SIZE,
                PARTICLE_BRIGHTNESS,
                PARTICLE_DURATION,
                BRIGHT_COLOR
            );
            engine.addHitParticle(point,
                vector,
                PARTICLE_SIZE * 5,
                PARTICLE_BRIGHTNESS,
                PARTICLE_DURATION * 0.75f,
                DIM_COLOR
            );
        }
    }
}
