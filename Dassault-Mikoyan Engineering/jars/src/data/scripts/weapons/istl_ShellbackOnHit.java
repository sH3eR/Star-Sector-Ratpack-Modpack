package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import org.lwjgl.util.vector.Vector2f;

import org.lazywizard.lazylib.MathUtils;

import java.awt.Color;

public class istl_ShellbackOnHit implements OnHitEffectPlugin
{
    // -- flux raise --------------------------------------------------------
    private static final float FLUXRAISE_MULT = 1f;
    private static final Color EXPLOSION_COLOR = new Color(75,90,255,200);
    // -- stuff for tweaking particle characteristics ------------------------
    private static final Color PARTICLE_COLOR = new Color(100,110,255,255);
    private static final float PARTICLE_SIZE = 5f;
    private static final float PARTICLE_BRIGHTNESS = 255f;
    private static final float PARTICLE_DURATION = 1.2f;
    private static final int PARTICLE_COUNT = 2;
    // -- particle geometry --------------------------------------------------
    private static final float CONE_ANGLE = 75f;
    private static final float VEL_MIN = 0.12f;
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
        // check whether or not you're hitting hull
        if (!shieldHit && target instanceof ShipAPI) {
            ShipAPI targetship = (ShipAPI) target;

            
            // do visual effects ---------------------------------------------
            engine.spawnExplosion(point,
                    target.getVelocity(),
                    EXPLOSION_COLOR, // color of the explosion
                    15f, // sets the size of the explosion
                    0.3f // how long the explosion lingers for
            );
            // calculate a number to raise target flux by
            float fluxmult = projectile.getDamageAmount() * FLUXRAISE_MULT;
            // get target max flux level
            float maxflux = targetship.getMaxFlux();
            //Check that the target can handle the flux; if so, raise target ship flux on hull hit
            if (maxflux > (fluxmult * 1.5f)) {
                targetship.getFluxTracker().increaseFlux(fluxmult, false);              
            }
            
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
        }
    }
}
