package data.scripts.vice.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class AtroposTorpOnFire implements OnFireEffectPlugin {
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		engine.spawnProjectile(weapon.getShip(), 
								weapon, 
								"vice_atropos_shell", 
								projectile.getLocation(), 
								weapon.getCurrAngle(),
								null);
	}
}