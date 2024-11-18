package data.hullmods.tw;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

//fighter placement and applying penalty hullmod handled by TrinityRetrofit
public class DroneControlNode extends BaseHullMod {
	
	private static String TW_HULLMOD = "tw_trinity_retrofit";
	private static String TW_DRONE_NAME = "Nimbus Combat Drones";
	private static float SHIELD_PENALTY = 10f;

	private static String CONFLICT_MOD = "hardenedshieldemitter";
	private static String CONFLICT_MOD_2 = "tw_enhanced_control_node";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getShieldDamageTakenMult().modifyMult(id, 1f + SHIELD_PENALTY * 0.01f);
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.isFrigate()) return false;
		if (ship.getVariant().hasHullMod(CONFLICT_MOD) || ship.getVariant().hasHullMod(CONFLICT_MOD_2)) return false;
		return ship.getVariant().hasHullMod(TW_HULLMOD);
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.isFrigate()) return "Cannot be installed on frigate";
		if (ship.getVariant().hasHullMod(CONFLICT_MOD)) return "Incompatible with Hardened Shields";
		if (ship.getVariant().hasHullMod(CONFLICT_MOD_2)) return "Control node is already installed";
		if (!ship.getVariant().hasHullMod(TW_HULLMOD)) return "Can only be fitted to Trinity Worlds ship";
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return TW_DRONE_NAME;
		if (index == 1) return "" + index;
		if (index == 2) return "" + index;
		if (index == 3) return "" + index;
		if (index == 4) return "" + (int) SHIELD_PENALTY + "%";
		return null;
	}
}