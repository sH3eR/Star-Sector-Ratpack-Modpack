package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class istl_ITU extends BaseHullMod {

	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FIGHTER, 0f);
		mag.put(HullSize.FRIGATE, 100f);
		mag.put(HullSize.DESTROYER, 200f);
		mag.put(HullSize.CRUISER, 300f);
		mag.put(HullSize.CAPITAL_SHIP, 400f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue() + "%";
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue() + "%";
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue() + "%";
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
		return null;
	}
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponRangeBonus().modifyFlat(id, (Float) mag.get(hullSize));
		stats.getEnergyWeaponRangeBonus().modifyFlat(id, (Float) mag.get(hullSize));
                stats.getWeaponRangeThreshold().modifyFlat(id, (Float) mag.get(hullSize)); // increase range threshold by same amount as range increase
	}


        @Override
        public String getUnapplicableReason(ShipAPI ship) {
            return "Must be installed on a Dassault-Mikoyan ship with Monobloc Construction";
        }

        @Override
        public boolean isApplicableToShip(ShipAPI ship) {
            // Allows any ship with a DME hull id
            return ship.getHullSpec().getHullId().startsWith("istl_");
        }
	
}
