package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.scripts.vice.util.RemnantSubsystemsUtil;

public class AdaptiveMalfunction extends BaseHullMod {

	private static float TIME_ACCELERATION_PENALTY = 20f;
	private static String THIS_MOD = "vice_adaptive_defective";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.getMutableStats().getTimeMult().modifyMult(id, 1f - TIME_ACCELERATION_PENALTY * 0.01f);
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) TIME_ACCELERATION_PENALTY + "%";
		return null;
	}
}