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

public class istl_VoleurOnHit implements OnHitEffectPlugin
{
    // -- crit damage -------------------------------------------------------
    private static final int CRIT_DAMAGE_MIN = 50;
    private static final int CRIT_DAMAGE_MAX = 150;
    private static final float CRIT_CHANCE = 1.0f;
    //  -- crit fx ----------------------------------------------------------
    private static final Color EXPLOSION_COLOR = new Color(85,115,225,200);
    private static final String SFX = "istl_shockarty_crit";
    // -- stuff for tweaking particle characteristics ------------------------
    private static final Color PARTICLE_COLOR = new Color(115,145,255,255);
    private static final float PARTICLE_SIZE = 2f;
    private static final float PARTICLE_BRIGHTNESS = 255f;
    private static final float PARTICLE_DURATION = 1.0f;
    private static final int PARTICLE_COUNT = 7;
    // -- particle geometry --------------------------------------------------
    private static final float CONE_ANGLE = 150f;
    private static final float VEL_MIN = 0.1f;
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
        // check whether or not we want to apply critical damage
        if (target instanceof ShipAPI && !shieldHit && Math.random() <= CRIT_CHANCE)
        {
            // apply the extra damage to the target
            engine.applyDamage(target, point, // where to apply damage
                MathUtils.getRandomNumberInRange(
                        CRIT_DAMAGE_MIN, CRIT_DAMAGE_MAX),
                DamageType.HIGH_EXPLOSIVE, // damage type
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
            125f, // sets the size of the explosion
            0.15f // how long the explosion lingers for
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
