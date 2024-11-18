package data.scripts.weapons;

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

public class istl_FrappeurOnHit implements OnHitEffectPlugin
{
    // amount of extra damage dealt
    private static final int DAMAGE = 350;
    public static float DAMAGE_MAXRADIUS = 75f;
    public static float DAMAGE_MINRADIUS = 25f;
    // All the good stuff to make the nebula particles behave
    private static final Color EXPLOSION_COLOR = new Color(125, 155, 255, 200);
    private static final Color NEBULA_COLOR = new Color(75, 105, 255, 255);
    private static final float NEBULA_SIZE = 12f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 30f;
    private static final float NEBULA_DUR = 1.5f;
    private static final float NEBULA_RAMPUP = 0.2f;

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
            engine.addNebulaParticle(point,
                target.getVelocity(),
                NEBULA_SIZE,
                NEBULA_SIZE_MULT,
                NEBULA_RAMPUP,
                0.3f,
                NEBULA_DUR,
                NEBULA_COLOR,
                true
            );
        }
        engine.spawnExplosion(point, 
            target.getVelocity(),
            NEBULA_COLOR, // color of the explosion
            NEBULA_SIZE * 4,
            NEBULA_DUR / 2
        );
    }
    public DamagingExplosionSpec createExplosionSpec() {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                        0.3f, // duration
                        DAMAGE_MAXRADIUS, // radius
                        DAMAGE_MINRADIUS, // coreRadius
                        DAMAGE, // maxDamage
                        DAMAGE / 2f, // minDamage
                        CollisionClass.PROJECTILE_FF, // collisionClass
                        CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                        7f, // particleSizeMin
                        3f, // particleSizeRange
                        3.0f, // particleDuration
                        12, // particleCount
                        NEBULA_COLOR, // particleColor
                        EXPLOSION_COLOR  // explosionColor
        );
        spec.setDamageType(DamageType.FRAGMENTATION);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("istl_mine_explosion_sm");
        return spec;		
    }
}
