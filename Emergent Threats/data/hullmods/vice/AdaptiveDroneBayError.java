package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class AdaptiveDroneBayError extends BaseHullMod {

	public static float CR_PENALTY = 100f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (stats.getNumFighterBays().getModifiedInt() == 0) stats.getVariant().getHullMods().remove(id);
		else stats.getMaxCombatReadiness().modifyFlat(id, -CR_PENALTY * 0.01f, "Drone bay malfunction");
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) CR_PENALTY + "%";
		return null;
	}
}