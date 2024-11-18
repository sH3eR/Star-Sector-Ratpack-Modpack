package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

public class istl_ScramblerEMPOnHit implements OnHitEffectPlugin
{
    // -- explosion graphics -------------------------------------------------
    // color of the explosion
    private static final Color EXPLOSION_COLOR = new Color(125, 100, 255, 155);
    // radius of the explosion
    private static final float EXPLOSION_RADIUS = 15f;
    // how long the explosion lingers for
    private static final float EXPLOSION_DURATION = 0.3f;

    // == EMP arcs ===========================================================
    // EMP arcs are randomly triggered based on the ARC_CHANCE constant
    // chance to create an EMP arc
    // this should range from 0 to 1, where 0 is "never" and 1 is "always"
    private static final float ARC_CHANCE = 0.2f;
    // EMP arc maximum range
    private static final float ARC_RANGE = 100000f;

    // -- multiplier for additional weapon damage dealt by arcs --------------
    // the amount of energy damage dealt by the arc will be the weapon's damage
    // multiplied by this constant.
    private static final float ARC_DAMAGE_MULT = 1.0f;
    // multiplier for weapon EMP damage dealt by arcs
    private static final float ARC_EMP_MULT = 1.0f;

    // -- arc FX -------------------------------------------------------------
    // sound effect to play when an EMP arc happens
    private static final String ARC_SFX = "tachyon_lance_emp_impact";
    // how thick the arc effect should be
    private static final float ARC_WIDTH = 20f;
    // fringe color
    private static final Color ARC_FRINGE_COLOR = new Color(85, 60, 205, 225);
    // core color
    private static final Color ARC_CORE_COLOR = new Color(235, 175, 235, 255);


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
            // get the target's velocity to render the crit FX
        }
            // do visual effects
            engine.spawnExplosion(point, v_target,
                    EXPLOSION_COLOR, // color of the explosion
                    EXPLOSION_RADIUS,
                    EXPLOSION_DURATION
            );
            //play a sound
            //Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
    }
}
