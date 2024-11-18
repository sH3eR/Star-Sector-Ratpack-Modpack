package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

public class istl_BlazeGunOnHit implements OnHitEffectPlugin
{
    private static final Color EXPLOSION_COLOR = new Color(255, 120, 75, 255);

    @Override
    public void onHit(DamagingProjectileAPI projectile,
            CombatEntityAPI target,
            Vector2f point,
            boolean shieldHit,
            ApplyDamageResultAPI damageResult, 
            CombatEngineAPI engine)
    {
        // check whether or not we want to apply critical damage
        if (target instanceof ShipAPI && !shieldHit)
        {
            // get the target's velocity to render the crit FX
            Vector2f v_target = new Vector2f(target.getVelocity());
            // variable FX for different projectiles
            final float size;
            final float dur;
            final String projid = projectile.getProjectileSpecId();
            IMPACT: {
                switch (projid) {
                    case "istl_blaze_shot": {
                        size = 15f;
                        dur = 0.3f;
                        break IMPACT;
                    }
                    case "istl_blaze_medshot": {
                        size = 30f;
                        dur = 0.6f;
                        break IMPACT;
                    }
                    default:
                        break;
                }
                return;
            };
            // do visual effects
            engine.spawnExplosion(point, v_target,
                EXPLOSION_COLOR, // color of the explosion
                size, // sets the size of the explosion
                dur // how long the explosion lingers for
            );            
            engine.addHitParticle(point, v_target,
                size * 2,
                1f,
                dur / 3,
                EXPLOSION_COLOR
            );            
        }
    }
}
