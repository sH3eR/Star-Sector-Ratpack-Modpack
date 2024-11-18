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

import java.awt.Color;

public class istl_VoltigeurOnHit implements OnHitEffectPlugin
{
    private static final int EXTRA_DAMAGE = 50;
    private static final Color EXPLOSION_COLOR = new Color(125,175,255,255);

    private static final String SFX = "istl_energy_crit";

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
        if (target instanceof ShipAPI && !shieldHit)
        {

            // apply the extra damage to the target
            engine.applyDamage(target, point, // where to apply damage
                    EXTRA_DAMAGE, // amount of damage
                    DamageType.ENERGY, // damage type
                    0f, // amount of EMP damage (none)
                    false, // does this bypass shields? (no)
                    false, // does this deal soft flux? (no)
                    projectile.getSource());
        }
            // do visual effects
            engine.spawnExplosion(point, v_target,
                    EXPLOSION_COLOR, // color of the explosion
                    75f, // sets the size of the explosion
                    0.2f // how long the explosion lingers for
            );
            //play a sound
            Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
        }
}
