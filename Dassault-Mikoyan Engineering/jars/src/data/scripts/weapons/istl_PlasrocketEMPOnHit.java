package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;

public class istl_PlasrocketEMPOnHit implements OnHitEffectPlugin
{
    // -- explosion graphics -------------------------------------------------
    private static final Color EXPLOSION_COLOR = new Color(75,255,175,155);
    private static final float EXPLOSION_RADIUS = 30f;
    private static final float EXPLOSION_DURATION = 0.15f;
    private static final float NEBULA_SIZE = 24f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_DUR = 0.75f;
    private static final float NEBULA_RAMPUP = 0.15f;
    private static final String SFX = "istl_energy_crit";
    // == EMP arcs ===========================================================
    private static final float ARC_CHANCE = 0.6f;
    private static final float ARC_RANGE = 100000f;
    // -- multiplier for additional weapon damage dealt by arcs --------------
    private static final float ARC_DAMAGE_MULT = 1.0f;
    private static final float ARC_EMP_MULT = 2.0f;
    // -- arc FX -------------------------------------------------------------
    private static final String ARC_SFX = "tachyon_lance_emp_impact";
    private static final float ARC_WIDTH = 32f;
    private static final Color ARC_FRINGE_COLOR = new Color(25,155,125,235);
    private static final Color ARC_CORE_COLOR = new Color(75,255,175,255);

    @Override
    public void onHit(DamagingProjectileAPI projectile,
            CombatEntityAPI target,
            Vector2f point,
            boolean shieldHit,
            ApplyDamageResultAPI damageResult, 
            CombatEngineAPI engine)
    {
        // check whether we've hit armour/hull
        if (target instanceof ShipAPI && !shieldHit)
        {
            // -- make an EMP arc ----------------------------------------------
            // check whether or not we want to apply critical damage
            if (Math.random() <= ARC_CHANCE)
            {
                // calculate arc EMP and energy damage
                float emp = projectile.getEmpAmount() * ARC_EMP_MULT;
                float dam = projectile.getDamageAmount() * ARC_DAMAGE_MULT;
                // spawn an EMP arc
                engine.spawnEmpArc(projectile.getSource(),
                        point, target, target,
                        DamageType.HIGH_EXPLOSIVE,
                        dam,
                        emp, // emp
                        ARC_RANGE, // max range
                        ARC_SFX,
                        ARC_WIDTH, // thickness
                        ARC_FRINGE_COLOR,
                        ARC_CORE_COLOR
                );
            }
        }
        // get the target's velocity to render the crit FX
        Vector2f v_target = new Vector2f(target.getVelocity());
        // do visual effects
        float NEBULA_SIZE_MULT = Misc.getHitGlowSize(60f, projectile.getDamage().getBaseDamage(), damageResult) / 100f;    
        engine.addSwirlyNebulaParticle(point,
            target.getVelocity(),
            NEBULA_SIZE,
            5f + 3f * NEBULA_SIZE_MULT,
            NEBULA_RAMPUP,
            0f,
            NEBULA_DUR,
            EXPLOSION_COLOR,
            true
        );
        engine.spawnExplosion(point,
            target.getVelocity(),
            EXPLOSION_COLOR, 
            NEBULA_SIZE * 4, 
            NEBULA_DUR / 4
        );
        //play a sound
        Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
    }
}
