package data.hullmods.tw;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

//dummy hullmod, stats changes handled by TrinityRetrofit
public class EnhancedControlNode extends BaseHullMod {

	private static String TW_DRONE_NAME = "Nimbus Combat Drones";
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return TW_DRONE_NAME;
		return null;
	}
}