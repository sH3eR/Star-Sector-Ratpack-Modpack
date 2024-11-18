package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class istl_RailSparks20cm implements OnHitEffectPlugin
{
    // -- stuff for tweaking particle characteristics ------------------------
    private static final Color BRIGHT_COLOR = new Color(155,225,255,255);
    private static final Color DIM_COLOR = new Color(0,50,100,25);
    private static final float PARTICLE_SIZE = 5f;
    private static final float PARTICLE_BRIGHTNESS = 255f;
    private static final float PARTICLE_DURATION = 3.2f;
    private static final int PARTICLE_COUNT = 9;
    // rail slugs hurt
    private static final String SFX = "istl_rail_crit_20cm";
    // -- particle geometry --------------------------------------------------
    private static final float CONE_ANGLE = 150f;
    private static final float VEL_MIN = 0.05f;
    private static final float VEL_MAX = 0.1f;

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
                && !shieldHit)
        {
            float speed = projectile.getVelocity().length();
            float facing = projectile.getFacing();
            for (int i = 1; i <= PARTICLE_COUNT; i++)
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
                        BRIGHT_COLOR
                );
                engine.addHitParticle(point,
                        vector,
                        PARTICLE_SIZE * 4,
                        PARTICLE_BRIGHTNESS,
                        PARTICLE_DURATION * 0.5f,
                        DIM_COLOR
                );
                //play a sound
                Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
            }
        }
    }
}
