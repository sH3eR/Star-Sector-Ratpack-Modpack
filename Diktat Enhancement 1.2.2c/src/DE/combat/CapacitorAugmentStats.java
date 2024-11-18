package DE.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class CapacitorAugmentStats extends BaseShipSystemScript {

	public static final float DAMAGE_BONUS_PERCENT = 25f;
	boolean systemswitch = false;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

		ShipAPI ship = (ShipAPI) stats.getEntity();
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}

		float fluxlevel = ship.getCurrFlux();
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		if (fluxlevel < (ship.getMaxFlux() / 4) && !systemswitch) {
			ship.getFluxTracker().decreaseFlux(ship.getMaxFlux()/4);
			systemswitch = true;
		}
		stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusPercent);
		//stats.getEnergyWeaponRangeBonus().modifyPercent(id, bonusPercent);
		
		//float damageTakenPercent = EXTRA_DAMAGE_TAKEN_PERCENT * effectLevel;
//		stats.getArmorDamageTakenMult().modifyPercent(id, damageTakenPercent);
//		stats.getHullDamageTakenMult().modifyPercent(id, damageTakenPercent);
//		stats.getShieldDamageTakenMult().modifyPercent(id, damageTakenPercent);
		//stats.getWeaponDamageTakenMult().modifyPercent(id, damageTakenPercent);
		//stats.getEngineDamageTakenMult().modifyPercent(id, damageTakenPercent);
		
		//stats.getBeamWeaponFluxCostMult().modifyMult(id, 10f);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().unmodify(id);
//		stats.getEnergyWeaponRangeBonus().unmodify(id);
//		stats.getArmorDamageTakenMult().unmodify(id);
//		stats.getHullDamageTakenMult().unmodify(id);
//		stats.getShieldDamageTakenMult().unmodify(id);
//		stats.getWeaponDamageTakenMult().unmodify(id);
//		stats.getEngineDamageTakenMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		if (index == 0) {
			return new StatusData("+" + (int) bonusPercent + "% energy weapon damage" , false);
		} else if (index == 1) {
			//return new StatusData("+" + (int) damageTakenPercent + "% weapon/engine damage taken", false);
			return null;
		} else if (index == 2) {
			//return new StatusData("shield damage taken +" + (int) damageTakenPercent + "%", true);
			return null;
		}
		return null;
	}
}
