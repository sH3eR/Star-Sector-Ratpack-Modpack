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

public class istl_MinirailOnHit implements OnHitEffectPlugin
{
    public static float DAMAGE = 100f;
    public static float DAMAGE_MAXRADIUS = 90f;
    public static float DAMAGE_MINRADIUS = 45f;
    private static final float FLUXLOWER_MULT = 0.4f;
    //FX
    private static final Color BOOM_COLOR = new Color(75,125,200,215);
    private static final Color NEBULA_COLOR = new Color(75,125,200,215);
    private static final float NEBULA_SIZE = 10f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 30f;
    private static final float NEBULA_DUR = 1.2f;
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
                BOOM_COLOR,
                true
            );
        }
        engine.addSwirlyNebulaParticle(point,
            target.getVelocity(),
            NEBULA_SIZE * 0.75f,
            NEBULA_SIZE_MULT * 0.75f,
            NEBULA_RAMPUP / 2,
            0.2f,
            NEBULA_DUR * 0.75f,
            NEBULA_COLOR,
            true
        );
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            ShipAPI targetship = (ShipAPI) target;
            //calculate a number to lower target flux by
            float fluxmult = projectile.getDamageAmount() * FLUXLOWER_MULT;
            //Lower target ship flux on hull hit
            targetship.getFluxTracker().decreaseFlux(fluxmult);
        }
    }
    public DamagingExplosionSpec createExplosionSpec() {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                        0.25f, // duration
                        DAMAGE_MAXRADIUS, // radius
                        DAMAGE_MINRADIUS, // coreRadius
                        DAMAGE, // maxDamage
                        DAMAGE / 2f, // minDamage
                        CollisionClass.PROJECTILE_FF, // collisionClass
                        CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                        7f, // particleSizeMin
                        3f, // particleSizeRange
                        1f, // particleDuration
                        24, // particleCount
                        NEBULA_COLOR, // particleColor
                        BOOM_COLOR  // explosionColor
        );
        spec.setDamageType(DamageType.FRAGMENTATION);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("istl_shockarty_crit_sm");
        return spec;		
    }
}
