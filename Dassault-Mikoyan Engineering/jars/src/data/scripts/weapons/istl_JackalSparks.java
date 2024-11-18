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
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class istl_JackalSparks implements OnHitEffectPlugin
{
    //Armor/hull AoE
    private static final float BOOM_CHANCE = 0.67f;
    private static final Color BOOM_COLOR = new Color(255,155,75,90);
    public static float DAMAGE = 100f;
    public static float DAMAGE_MAXRADIUS = 75f;
    public static float DAMAGE_MINRADIUS = 25f;
    // -- stuff for tweaking nebulaparticle characteristics ------------------------
    private static final Color NEBULA_COLOR = new Color(255,155,75,175);
    private static final float NEBULA_SIZE = 9f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 15f;
    private static final float NEBULA_DUR = 0.6f;
    private static final float NEBULA_RAMPUP = 0.1f;
    //Shield sparks and ricochets
    private static final float SPARK_CHANCE = 0.3f;
    // -- stuff for tweaking particle characteristics ------------------------
    private static final Color PARTICLE_COLOR = new Color(255,155,75,225);
    private static final float PARTICLE_SIZE = 5;
    private static final float PARTICLE_BRIGHTNESS = 255f;
    private static final float PARTICLE_DURATION = 2.0f;
    private static final int PARTICLE_COUNT = 3;
    private static final String SFX = "istl_jackal_crit";
    // -- particle geometry --------------------------------------------------
    private static final float CONE_ANGLE = 150f;
    private static final float VEL_MIN = 0.1f;
    private static final float VEL_MAX = 0.2f;

    // one half of the angle. used internally, don't mess with thos
    private static final float A_2 = CONE_ANGLE / 2;

    @Override
    public void onHit(DamagingProjectileAPI projectile,
            CombatEntityAPI target,
            Vector2f point,
            boolean shieldHit,
            ApplyDamageResultAPI damageResult, 
            CombatEngineAPI engine)
    {
        // Check if we hit a ship (not its shield)
        if (target instanceof ShipAPI
                && !shieldHit
                && Math.random() <= BOOM_CHANCE)
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
                NEBULA_SIZE * 4,
                NEBULA_DUR / 2
            );
        }
        // Check if we hit a shield and do crit chance check
        if (target instanceof ShipAPI
                && shieldHit
                && Math.random() <= SPARK_CHANCE)
        {
            //spawn sparks
            float speed = projectile.getVelocity().length();
            float facing = projectile.getFacing();
            for (int i = 0; i <= PARTICLE_COUNT; i++)
            {
                float angle = MathUtils.getRandomNumberInRange(facing - A_2,
                        facing + A_2);
                float vel = MathUtils.getRandomNumberInRange(speed * -VEL_MIN,
                        speed * -VEL_MAX);
                Vector2f vector = MathUtils.getPointOnCircumference(null,
                        vel,
                        angle);
                engine.addHitParticle(point,
                        vector,
                        PARTICLE_SIZE,
                        PARTICLE_BRIGHTNESS,
                        PARTICLE_DURATION,
                        PARTICLE_COLOR);
            }
            //spawn ricochet projectiles 
            float angle = VectorUtils.getAngle(target.getLocation(), projectile.getLocation());
            for(int i = 0;i < 1 ;i++) {
    		float newangle = angle -30 + (float)(Math.random()*60f);
    		Vector2f spawn = MathUtils.getPoint(projectile.getLocation(), 30, newangle);
    		engine.spawnProjectile(projectile.getSource(), projectile.getWeapon(), "istl_jackal_sub", spawn, newangle, new Vector2f());
            }
            //play a sound
            Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
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
        spec.setSoundSetId("istl_ballistic_crit");
        return spec;		
    }
}
