package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.DisintegratorEffect;
import com.fs.starfarer.api.util.Misc;

import org.lwjgl.util.vector.Vector2f;

import org.lazywizard.lazylib.MathUtils;

import java.awt.Color;

public class istl_BrigadorOnHit implements OnHitEffectPlugin
{
	public static float DAMAGE = 50;
        private static final float FLUXRAISE_MULT = 0.25f;
        private static final Color EXPLOSION_COLOR = new Color(75,125,200,215);
        
        private static final Color PARTICLE_COLOR = new Color(75,125,200,255);
        private static final float PARTICLE_SIZE = 5f;
        private static final float PARTICLE_BRIGHTNESS = 255f;
        private static final float PARTICLE_DURATION = 1.2f;
        private static final int PARTICLE_COUNT = 2;

        // -- particle geometry --------------------------------------------------
        private static final float CONE_ANGLE = 150f;
        private static final float VEL_MIN = 0.06f;
        private static final float VEL_MAX = 0.1f;

        // one half of the angle. used internally, don't mess with thos
        private static final float A_2 = CONE_ANGLE / 2;

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
            Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

            engine.spawnExplosion(point,
                target.getVelocity(),
                EXPLOSION_COLOR, // color of the explosion
                45f, // sets the size of the explosion
                0.4f // how long the explosion lingers for
            );

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
            if (shieldHit && target instanceof ShipAPI) {
            ShipAPI targetship = (ShipAPI) target;
            // calculate a number to raise target flux by
            float fluxmult = projectile.getDamageAmount() * FLUXRAISE_MULT;
            //Raise target ship flux on hull hit
            targetship.getFluxTracker().increaseFlux(fluxmult, false);
            }         
            if (!shieldHit && target instanceof ShipAPI) {
                    dealArmorDamage(projectile, (ShipAPI) target, point);
            }
	}
	
	public static void dealArmorDamage(DamagingProjectileAPI projectile, ShipAPI target, Vector2f point) {
		CombatEngineAPI engine = Global.getCombatEngine();

		ArmorGridAPI grid = target.getArmorGrid();
		int[] cell = grid.getCellAtLocation(point);
		if (cell == null) return;
		
		int gridWidth = grid.getGrid().length;
		int gridHeight = grid.getGrid()[0].length;
		
		float damageTypeMult = DisintegratorEffect.getDamageTypeMult(projectile.getSource(), target);
		
		float damageDealt = 0f;
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				if ((i == 2 || i == -2) && (j == 2 || j == -2)) continue; // skip corners
				
				int cx = cell[0] + i;
				int cy = cell[1] + j;
				
				if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;
				
				float damMult = 1/30f;
				if (i == 0 && j == 0) {
					damMult = 1/15f;
				} else if (i <= 1 && i >= -1 && j <= 1 && j >= -1) { // S hits
					damMult = 1/15f;
				} else { // T hits
					damMult = 1/30f;
				}
				
				float armorInCell = grid.getArmorValue(cx, cy);
				float damage = DAMAGE * damMult * damageTypeMult;
				damage = Math.min(damage, armorInCell);
				if (damage <= 0) continue;
				
				target.getArmorGrid().setArmorValue(cx, cy, Math.max(0, armorInCell - damage));
				damageDealt += damage;
			}
		}
		
		if (damageDealt > 0) {
			if (Misc.shouldShowDamageFloaty(projectile.getSource(), target)) {
				engine.addFloatingDamageText(point, damageDealt, Misc.FLOATY_ARMOR_DAMAGE_COLOR, target, projectile.getSource());
			}
			target.syncWithArmorGridState();
		}
	}
}