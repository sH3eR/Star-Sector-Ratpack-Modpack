package data.scripts.weapons;

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

public class istl_GravmissileOnHit implements OnHitEffectPlugin
{
    private static final int CRIT_DAMAGE_MIN = 50;
    private static final int CRIT_DAMAGE_MAX = 150;
    private static final Color EXPLOSION_COLOR = new Color(185,175,100,255);

    private static Random rng = new Random();
    private static float damageAmount()
    {
        return (float) (rng.nextInt((CRIT_DAMAGE_MAX - CRIT_DAMAGE_MIN) + 1) + CRIT_DAMAGE_MIN);
    }
    @Override
    public void onHit(DamagingProjectileAPI projectile,
            CombatEntityAPI target,
            Vector2f point,
            boolean shieldHit,
            ApplyDamageResultAPI damageResult, 
            CombatEngineAPI engine)
    {
        // check whether or not we want to apply critical damage
        if (target instanceof ShipAPI && !shieldHit)
        {
            // apply the extra damage to the target
            engine.applyDamage(target, point, // where to apply damage
                    damageAmount(), // amount of damage
                    DamageType.HIGH_EXPLOSIVE, // damage type
                    0f, // amount of EMP damage (none)
                    false, // does this bypass shields? (no)
                    false, // does this deal soft flux? (no)
                    projectile.getSource());
            // get the target's velocity to render the crit FX
            Vector2f v_target = new Vector2f(target.getVelocity());
            // do visual effects
            engine.spawnExplosion(point,
                    v_target,
                    EXPLOSION_COLOR, // color of the explosion
                    90f, // sets the size of the explosion
                    1.0f // how long the explosion lingers for
            );
        }
    }
}
