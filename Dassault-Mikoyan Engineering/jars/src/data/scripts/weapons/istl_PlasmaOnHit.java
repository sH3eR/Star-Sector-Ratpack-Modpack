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

public class istl_PlasmaOnHit implements OnHitEffectPlugin
{
    // minimum amount of extra damage
    private static final int CRIT_DAMAGE_MIN = 0;
    // maximum amount of extra damage dealt
    private static final int CRIT_DAMAGE_MAX = 50;
    // probability (0-1) of dealing a critical hit
    private static final float CRIT_CHANCE = 1.0f;
    // All the good stuff to make the nebula particles behave
    private static final Color EXPLOSION_COLOR = new Color(155,100,255,255);
    private static final float NEBULA_SIZE = 8f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 12f;
    private static final float NEBULA_DUR = 1.0f;
    private static final float NEBULA_RAMPUP = 0f;

    // placeholder, please change this once you have a nice explosion sound :)
    private static final String SFX = "istl_energy_crit";

    private static Random rng = new Random();

    // if we were using LazyLib, we could just use its'
    // `getRandomNumberInRange()` method, but this works fine too.
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
        // get the target's velocity to render the crit FX
        Vector2f v_target = new Vector2f(target.getVelocity());
        // check whether or not we want to apply critical damage
        if (target instanceof ShipAPI && !shieldHit && Math.random() <= CRIT_CHANCE)
        {

            // apply the extra damage to the target
            engine.applyDamage(target, point, // where to apply damage
                    damageAmount(), // amount of damage
                    DamageType.FRAGMENTATION, // damage type
                    100f, // amount of EMP damage (none)
                    false, // does this bypass shields? (no)
                    false, // does this deal soft flux? (no)
                    projectile.getSource());
    }
            // do visual effects                
            engine.addNebulaParticle(
                point,
                v_target,
                NEBULA_SIZE,
                NEBULA_SIZE_MULT,
                NEBULA_RAMPUP,
                0f,
                NEBULA_DUR,
                EXPLOSION_COLOR,
                true
            );
            //play a sound
            Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
        }
}
