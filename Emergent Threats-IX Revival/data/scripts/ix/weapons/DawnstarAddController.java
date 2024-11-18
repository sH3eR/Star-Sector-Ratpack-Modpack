package data.scripts.ix.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class DawnstarAddController implements EveryFrameWeaponEffectPlugin {
	
	private boolean runOnce = false;
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (runOnce) return;
		else if (!weapon.getShip().getVariant().hasHullMod("ix_dawnstar_controller")) {
			weapon.getShip().getVariant().getHullMods().add("ix_dawnstar_controller");
		}
		runOnce = true;
	}
}