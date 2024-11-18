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
import org.lazywizard.lazylib.MathUtils;

public class istl_PlasmaGunOnHit implements OnHitEffectPlugin
{
    // -- crit damage -------------------------------------------------------
    private static final float CRIT_DAMAGE_MIN_MULT = 0.1f;
    private static final float CRIT_DAMAGE_MAX_MULT = 0.25f;     
    private static final float CRIT_CHANCE = 0.3f;
    //  -- crit fx ----------------------------------------------------------
    private static final Color EXPLOSION_COLOR = new Color(155,100,255,255);
    private static final float NEBULA_SIZE = 8f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 15f;
    private static final float NEBULA_DUR = 1.2f;
    private static final float NEBULA_RAMPUP = 0.1f;
    private static final String SFX = "istl_energy_crit_sm";

    @Override
    public void onHit(DamagingProjectileAPI projectile,
            CombatEntityAPI target,
            Vector2f point,
            boolean shieldHit,
            ApplyDamageResultAPI damageResult, 
            CombatEngineAPI engine)
    {
        Vector2f v_target = new Vector2f(target.getVelocity());
        
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
                    projectile.getSource());
        }
        // do visual effects                
        engine.addNebulaParticle(point,
            target.getVelocity(),
            NEBULA_SIZE,
            NEBULA_SIZE_MULT,
            NEBULA_RAMPUP,
            0.2f,
            NEBULA_DUR,
            EXPLOSION_COLOR,
            true
        );
        //play a sound
        Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
    }
}
