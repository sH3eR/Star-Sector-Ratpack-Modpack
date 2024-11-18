package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import org.lwjgl.util.vector.Vector2f;
import java.awt.Color;

public class MSS_grenadelauncheronhit implements OnHitEffectPlugin {

    private final String ID = "MSS_thumper_sub";
    private static final Color EXPLOSION_COLOR = new Color(46, 91, 255);

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        if (!shieldHit && target instanceof ShipAPI) {
            ((MSS_grenadelauncherscript) projectile.getWeapon().getEffectPlugin()).putHIT(target);
            engine.spawnProjectile(
                    projectile.getSource(),
                    projectile.getWeapon(),
                    ID,
                    point,
                    projectile.getFacing(),
                    target.getVelocity()
            );
            Global.getSoundPlayer().playSound("MSS_thumper_ping", 1.0f, 0.66f, point, target.getVelocity());
        }
        engine.addSmoothParticle(point, target.getVelocity(), 100f, 1.5f, 0.25f, EXPLOSION_COLOR);
    }
}
