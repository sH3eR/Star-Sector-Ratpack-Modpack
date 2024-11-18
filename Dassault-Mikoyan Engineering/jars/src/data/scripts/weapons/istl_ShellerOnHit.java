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

public class istl_ShellerOnHit implements OnHitEffectPlugin
{
    // -- crit damage -------------------------------------------------------
    //private static final int CRIT_DAMAGE_MIN = 50;
    //private static final int CRIT_DAMAGE_MAX = 100;
    private static final float CRIT_DAMAGE_MULT = 1.25f;
    private static final float CRIT_CHANCE = 0.15f;
    //  -- crit fx ----------------------------------------------------------
    private static final Color NEBULA_COLOR = new Color(75, 90, 255, 200);
    private static final float NEBULA_SIZE = 6f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 21f;
    private static final float NEBULA_DUR = 0.8f;
    private static final float NEBULA_RAMPUP = 0.2f;
    private static final Color EXPLOSION_COLOR = new Color(125, 135, 255, 255);
    private static final float EXPLOSION_DUR_MULT = 0.6f;    
    private static final String SFX = "istl_kinetic_crit_micro";
    // -- stuff for tweaking particle characteristics ------------------------
    private static final Color PARTICLE_COLOR = new Color(100, 110, 255, 255);
    private static final float PARTICLE_SIZE = 3f;
    private static final float PARTICLE_BRIGHTNESS = 255f;
    private static final float PARTICLE_DURATION = 0.6f;
    private static final int PARTICLE_COUNT = 3;
    // -- particle geometry --------------------------------------------------
    private static final float CONE_ANGLE = 150f;
    private static final float VEL_MIN = 0.2f;
    private static final float VEL_MAX = 0.3f;
    // one half of the angle. used internally, don't mess with thos
    private static final float A_2 = CONE_ANGLE / 2;

    @Override
    public void onHit(DamagingProjectileAPI projectile,
            CombatEntityAPI target,
            Vector2f point,
            boolean shieldHit,
            ApplyDamageResultAPI damageResult, 
            CombatEngineAPI engine)
    {
        // check whether or not we want to apply critical damage
        if (target instanceof ShipAPI && !shieldHit && Math.random() <= CRIT_CHANCE)
        {
            //calculate a number to raise target flux by
            float critmult = projectile.getDamageAmount() * CRIT_DAMAGE_MULT;
            // apply the extra damage to the target
            engine.applyDamage(target, point, // where to apply damage
                    critmult,
                    DamageType.ENERGY, // damage type
                    100f, // amount of EMP damage (none)
                    false, // does this bypass shields? (no)
                    false, // does this deal soft flux? (no)
                    projectile.getSource());

            // do visual effects ---------------------------------------------
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
            //play a sound
            Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());

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
        }
        // do visual effects ---------------------------------------------
        engine.spawnExplosion(point,
                target.getVelocity(),
                EXPLOSION_COLOR, // color of the explosion
                NEBULA_SIZE * 2, // sets the size of the explosion
                NEBULA_DUR * EXPLOSION_DUR_MULT // how long the explosion lingers for
        );
    }
}
