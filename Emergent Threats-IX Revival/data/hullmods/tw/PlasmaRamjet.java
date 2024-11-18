package data.hullmods.tw;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class PlasmaRamjet extends BaseHullMod {

	//Dummy hullmod, improved ship system is new system assigned directly to the hull
	private static float DURATION_INCREASE = 33f;
	private static float SPEED_INCREASE = 20f;
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) DURATION_INCREASE + "%";
		if (index == 1) return "" + (int) SPEED_INCREASE + "%";
		return null;
	}
}