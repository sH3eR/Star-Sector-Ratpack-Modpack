package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lazywizard.lazylib.MathUtils;

public class istl_ElecRocketOnHit implements OnHitEffectPlugin {

    private static final int MIN_ARCS = 2;
    private static final int MAX_ARCS = 3;
    private static final float ARC_DAMAGE = 0.2f;
    private static final float ARC_EMP = 0.25f;
    private static final String SFX = "istl_kinetic_crit_med";
    // -- explosion graphics -------------------------------------------------
    private static final Color EXPLOSION_COLOR = new Color(100,110,255,255);
    private static final float EXPLOSION_RADIUS = 150f;
    private static final float EXPLOSION_DURATION = 0.1f;
    
    @Override
    public void onHit(DamagingProjectileAPI projectile,
            CombatEntityAPI target,
            Vector2f point,
            boolean shieldHit,
            ApplyDamageResultAPI damageResult, 
            CombatEngineAPI engine)
    {
        if (!shieldHit && target instanceof ShipAPI) 
        {
            float dam = projectile.getDamageAmount() * ARC_DAMAGE;
            float emp = projectile.getEmpAmount() * ARC_EMP;

            int arcs = MathUtils.getRandomNumberInRange(MIN_ARCS, MAX_ARCS);
            
            for (int i = 0; i < arcs; i++) {
                engine.spawnEmpArc(projectile.getSource(), point, target, target,
                        DamageType.ENERGY, // damage type
                        dam, // damage
                        emp, // emp 
                        100000f, // max range of arcs (on target)
                        "tachyon_lance_emp_impact", // sound
                        25f, // thickness
                        new Color(50, 55, 155, 255), // fringe color
                        new Color(200, 220, 255, 255) // core color
                );
            
            // get the target's velocity to render the crit FX
            Vector2f v_target = new Vector2f(target.getVelocity());    
            // do visual effects
            engine.spawnExplosion(point, v_target,
                    EXPLOSION_COLOR, // color of the explosion
                    EXPLOSION_RADIUS,
                    EXPLOSION_DURATION
            );
            Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
            }
        }
    }
}