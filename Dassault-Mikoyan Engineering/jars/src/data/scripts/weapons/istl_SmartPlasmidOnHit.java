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

public class istl_SmartPlasmidOnHit implements OnHitEffectPlugin
{
    public static float DAMAGE = 150f;
    public static float DAMAGE_MAXRADIUS = 120f;
    public static float DAMAGE_MINRADIUS = 60f;
    //FX
    private static final Color BOOM_COLOR = new Color(135,75,255,155);
    private static final Color NEBULA_COLOR = new Color(155,100,255,255);
    private static final float NEBULA_SIZE = 15f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 30f;
    private static final float NEBULA_DUR = 1.8f;
    private static final float NEBULA_RAMPUP = 0.2f;
    
    private static final String SFX = "istl_mine_explosion_sm";
    
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
            Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
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
                        2.4f, // particleDuration
                        30, // particleCount
                        NEBULA_COLOR, // particleColor
                        BOOM_COLOR  // explosionColor
        );
        spec.setDamageType(DamageType.ENERGY);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("devastator_explosion");
        return spec;		
    }
}
