package org.niatahl.tahlan.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.magiclib.util.MagicLensFlare;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class GleipnirOnHitEffect implements OnHitEffectPlugin {

    //Percentage of the projectile's original damage dealt as bonus damage on hull hit: too high and AI issues start appearing
    //private static final float DAMAGE_MULT = 0.3f;

    //Variables for explosion visuals
    private static final Color EXPLOSION_COLOR = new Color(255, 85, 10);
    private static final float EXPLOSION_SIZE = 200f;
    private static final float EXPLOSION_DURATION_MIN = 0.3f;
    private static final float EXPLOSION_DURATION_MAX = 0.7f;

    //Variables for the small particles generated with the explosion
    private static final Color PARTICLE_COLOR = new Color(255, 85, 10);
    private static final float PARTICLE_SIZE = 120f;
    private static final float PARTICLE_BRIGHTNESS = 90f;

    private static final Color FLASH_COLOR = new Color(255, 245, 209);
    private static final int NUM_PARTICLES = 100;

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        if ( !(target instanceof ShipAPI) ) {
            return;
        }

        Global.getCombatEngine().spawnExplosion(point, new Vector2f(0f, 0f), EXPLOSION_COLOR, EXPLOSION_SIZE, EXPLOSION_DURATION_MAX);
        DamagingExplosionSpec blast = new DamagingExplosionSpec(0.1f,
                EXPLOSION_SIZE,
                EXPLOSION_SIZE/2,
                500f,
                250f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                10f,
                10f,
                0f,
                0,
                PARTICLE_COLOR,
                null);
        blast.setDamageType(DamageType.FRAGMENTATION);
        blast.setShowGraphic(false);
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        MagicLensFlare.createSharpFlare(engine, projectile.getSource(), projectile.getLocation(), 12, 750, 0, EXPLOSION_COLOR, new Color(255, 255, 255));

        engine.addSmoothParticle(point, ZERO, 200f, 0.5f, 0.1f, PARTICLE_COLOR);
        engine.addHitParticle(point, ZERO, 300f, 0.3f, 0.05f, FLASH_COLOR);
        for (int x = 0; x < NUM_PARTICLES; x++) {
            engine.addHitParticle(point,
                    MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 400f), (float) Math.random() * 360f),
                    5f, 1f, MathUtils.getRandomNumberInRange(0.6f, 1f), PARTICLE_COLOR);
        }

    }
}
