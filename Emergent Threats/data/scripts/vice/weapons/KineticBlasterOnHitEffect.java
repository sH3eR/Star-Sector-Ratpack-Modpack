package data.scripts.vice.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class KineticBlasterOnHitEffect implements OnHitEffectPlugin {

	public static float DAMAGE_BONUS = 50f;

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (shieldHit && target instanceof ShipAPI) {
			engine.applyDamage(target, point, DAMAGE_BONUS, DamageType.KINETIC, 0, false, false, projectile.getSource());		
		}
	}
}