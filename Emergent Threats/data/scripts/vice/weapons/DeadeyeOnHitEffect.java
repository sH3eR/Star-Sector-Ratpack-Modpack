package data.scripts.vice.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class DeadeyeOnHitEffect implements OnHitEffectPlugin {
	
	private static float DAMAGE = 50f;
	
	public void onHit(DamagingProjectileAPI proj, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (!shieldHit) {
			engine.applyDamage(target, point, DAMAGE, DamageType.HIGH_EXPLOSIVE, 0, false, false, proj.getSource());
		}
	}
}