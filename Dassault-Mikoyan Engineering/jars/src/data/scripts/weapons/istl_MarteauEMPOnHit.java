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

public class istl_MarteauEMPOnHit implements OnHitEffectPlugin
{
    private static final Color EXPLOSION_COLOR = new Color(165,165,150,225);
    private static final Color NEBULA_COLOR = new Color(100,100,100,200);
    private static final float NEBULA_SIZE = 7f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 35f;
    private static final float NEBULA_DUR = 1.5f;
    private static final float NEBULA_RAMPUP = 0.3f;
    
    @Override
    public void onHit(DamagingProjectileAPI projectile,
        CombatEntityAPI target,
        Vector2f point,
        boolean shieldHit,
        ApplyDamageResultAPI damageResult, 
        CombatEngineAPI engine)
    {
        if ((float) Math.random() > 0.3f && !shieldHit && target instanceof ShipAPI)
        {
            float emp = projectile.getEmpAmount() * 2.0f;
            float dam = projectile.getDamageAmount() * 0.25f;

            engine.spawnEmpArc(projectile.getSource(), point, target, target,
                DamageType.ENERGY,
                dam,
                emp, // emp 
                1000f, // max range 
                "tachyon_lance_emp_impact",
                20f, // thickness
                EXPLOSION_COLOR,
                new Color(255,255,255,255)
            );
            engine.addNebulaParticle(point,
                target.getVelocity(),
                NEBULA_SIZE,
                NEBULA_SIZE_MULT,
                NEBULA_RAMPUP,
                0.3f,
                NEBULA_DUR,
                NEBULA_COLOR
            );
            engine.spawnExplosion(point,
                target.getVelocity(),
                EXPLOSION_COLOR, // color of the explosion
                NEBULA_SIZE * 3, // sets the size of the explosion
                NEBULA_DUR * 6 // how long the explosion lingers for
            );
        }
    }
}
