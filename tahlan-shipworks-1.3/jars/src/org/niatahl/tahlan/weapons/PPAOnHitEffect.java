package org.niatahl.tahlan.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.magiclib.plugins.MagicTrailPlugin;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class PPAOnHitEffect implements OnHitEffectPlugin {

    private static final Color COLOR1 = new Color(52, 255, 235, 150);
    private static final Color COLOR2 = new Color(237, 255, 246, 200);

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (point == null) {
            return;
        }

        //Also spawns lightning, though only against ships, and only on direct hull hits
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI)target;

            float hitLevel = 0f;
            float emp = projectile.getEmpAmount();
            float dam = projectile.getDamageAmount() / 2f;
            for (int x = 0; x < 4; x++) {
                float arcChance = 0.2f;
                boolean triggered = (float) Math.random() < arcChance;
                if (!shieldHit && triggered) {
                    hitLevel += 0.25f;
                    ShipAPI empTarget = ship;
                    EmpArcEntityAPI arc =  engine.spawnEmpArcPierceShields(projectile.getSource(), point, empTarget, empTarget,
                            projectile.getDamageType(), dam, emp, 1000f * hitLevel, null, 10f, COLOR1, COLOR2);
                }
            }

            if (hitLevel > 0f) {
                Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1f - (0.2f * hitLevel), hitLevel, point, new Vector2f(0f, 0f));
            }
        }
    }
}

