package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

public class istl_ShockArtyOnHit implements OnHitEffectPlugin
{
    private static final Color BOOM_COLOR = new Color(75,100,255,200);
    private static final Color NEBULA_COLOR = new Color(140,125,255,255);
    private static final float NEBULA_SIZE = 15f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 25f;
    private static final float NEBULA_DUR = 1.2f;
    private static final float NEBULA_RAMPUP = 0.15f;
    
    @Override
    public void onHit(DamagingProjectileAPI projectile,
            CombatEntityAPI target,
            Vector2f point,
            boolean shieldHit,
            ApplyDamageResultAPI damageResult, 
            CombatEngineAPI engine)
    {
        Vector2f v_target = new Vector2f(target.getVelocity());

        // check whether we've hit a ship
        if (target instanceof ShipAPI)
        {
            engine.spawnDamagingExplosion(createExplosionSpec(), projectile.getSource(), point);
            //do visual effects
            engine.addSwirlyNebulaParticle(point,
                target.getVelocity(),
                NEBULA_SIZE,
                NEBULA_SIZE_MULT,
                NEBULA_RAMPUP,
                0.2f,
                NEBULA_DUR,
                NEBULA_COLOR,
                true
            );
                engine.spawnExplosion(point, 
                target.getVelocity(),
                NEBULA_COLOR, // color of the explosion
                NEBULA_SIZE * 3,
                NEBULA_DUR / 3
            );            
        }
    }
    public DamagingExplosionSpec createExplosionSpec() {
        float damage = 100f;
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                        0.3f, // duration
                        120f, // radius
                        40f, // coreRadius
                        damage, // maxDamage
                        damage / 3f, // minDamage
                        CollisionClass.PROJECTILE_FF, // collisionClass
                        CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                        5f, // particleSizeMin
                        2f, // particleSizeRange
                        1f, // particleDuration
                        48, // particleCount
                        NEBULA_COLOR, // particleColor
                        BOOM_COLOR  // explosionColor
        );
        spec.setDamageType(DamageType.ENERGY);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("istl_shockarty_crit");
        return spec;		
    }
}
