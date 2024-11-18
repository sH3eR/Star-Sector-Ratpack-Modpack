package data.hullmods.vice;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class CompactAutomation extends BaseHullMod {
	
	//dummy hullmod, effect is negative OP cost
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "major malfunctions";
		return null;
	}
}