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

public class istl_EBolterEMPOnHit implements OnHitEffectPlugin
{
    // -- explosion graphics -------------------------------------------------
    private static final Color EXPLOSION_COLOR = new Color(200, 220, 255, 255);
    private static final float EXPLOSION_RADIUS = 4f;
    private static final float EXPLOSION_DURATION = 0.1f;
    private static final String SFX = "istl_energy_crit_sm";
    // == EMP arcs ===========================================================
    private static final float ARC_CHANCE = 0.2f;
    private static final float ARC_RANGE = 20000f;
    // -- multiplier for additional weapon damage dealt by arcs --------------
    private static final float ARC_DAMAGE_MULT = 1f;
    private static final float ARC_EMP_MULT = 2.0f;
    // -- arc FX -------------------------------------------------------------
    private static final String ARC_SFX = "tachyon_lance_emp_impact";
    private static final float ARC_WIDTH = 20f;
    private static final Color ARC_FRINGE_COLOR = new Color(50, 55, 155, 255);
    private static final Color ARC_CORE_COLOR = new Color(200, 220, 255, 255);

    @Override
    public void onHit(DamagingProjectileAPI projectile,
            CombatEntityAPI target,
            Vector2f point,
            boolean shieldHit,
            ApplyDamageResultAPI damageResult, 
            CombatEngineAPI engine)
    {
        Vector2f v_target = new Vector2f(target.getVelocity());

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
                        DamageType.ENERGY,
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
        // do visual effects
        engine.spawnExplosion(point, v_target,
                EXPLOSION_COLOR, // color of the explosion
                EXPLOSION_RADIUS,
                EXPLOSION_DURATION
        );
        //play a sound
        Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
    }
}
