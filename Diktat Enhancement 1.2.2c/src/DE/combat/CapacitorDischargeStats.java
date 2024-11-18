package DE.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class CapacitorDischargeStats extends BaseShipSystemScript {

	public static final float ROF_BONUS_PERCENT = 1f;
	public static final float SPEED_BONUS_PERCENT = 50f;
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

		float bonusPercent = 1f + ROF_BONUS_PERCENT * effectLevel;
		float speedPercent = SPEED_BONUS_PERCENT * effectLevel;
		if (fluxlevel < (ship.getMaxFlux() / 2) && !systemswitch) {
			ship.getFluxTracker().decreaseFlux(ship.getMaxFlux()/2);
			systemswitch = true;
		}
		stats.getEnergyRoFMult().modifyPercent(id, bonusPercent);
		stats.getMaxSpeed().modifyFlat(id, speedPercent);
		stats.getAcceleration().modifyPercent(id, 150f * effectLevel);
		stats.getDeceleration().modifyPercent(id, 150f * effectLevel);
		stats.getTurnAcceleration().modifyFlat(id, 60f * effectLevel);
		stats.getTurnAcceleration().modifyPercent(id, 150f * effectLevel);
		stats.getMaxTurnRate().modifyFlat(id, 20f);
		stats.getMaxTurnRate().modifyPercent(id, 180f);
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
		stats.getEnergyRoFMult().unmodify(id);
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		systemswitch = false;
//		stats.getEnergyWeaponRangeBonus().unmodify(id);
//		stats.getArmorDamageTakenMult().unmodify(id);
//		stats.getHullDamageTakenMult().unmodify(id);
//		stats.getShieldDamageTakenMult().unmodify(id);
//		stats.getWeaponDamageTakenMult().unmodify(id);
//		stats.getEngineDamageTakenMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS_PERCENT * effectLevel;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 0) {
			return new StatusData("energy weapon rate of fire +" + (int) bonusPercent + "%", false);
		} else if (index == 1) {
			return new StatusData("increased engine power by" + (int) SPEED_BONUS_PERCENT + " units", false);
		} else if (index == 2) {
			return new StatusData("improved maneuverability", false);
		}
		return null;
	}
}
