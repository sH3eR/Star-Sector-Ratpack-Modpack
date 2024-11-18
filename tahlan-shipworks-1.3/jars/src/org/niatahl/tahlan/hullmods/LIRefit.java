package org.niatahl.tahlan.hullmods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import static org.niatahl.tahlan.utils.Utils.txt;

public class LIRefit extends BaseHullMod {

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

	private static final Map<HullSize, Float> mag = new HashMap<HullSize, Float>();
	static {
		mag.put(HullSize.FRIGATE, 25f);
		mag.put(HullSize.DESTROYER, 20f);
		mag.put(HullSize.CRUISER, 15f);
		mag.put(HullSize.CAPITAL_SHIP, 10f);
	}

    static {
        BLOCKED_HULLMODS.add("unstable_injector");
    }

	private static final float BURN_LEVEL_BONUS = 1f;
	private static final float PROFILE_MULT = 0.75f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

	    stats.getMaxSpeed().modifyFlat(id, mag.get(hullSize));
		stats.getMaxBurnLevel().modifyFlat(id, BURN_LEVEL_BONUS);
		stats.getSensorProfile().modifyMult(id, PROFILE_MULT);

	}

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
            }
        }
    }

	public boolean isApplicableToShip(ShipAPI ship) {
		return false;
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + (mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + (mag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + (mag.get(HullSize.CAPITAL_SHIP)).intValue();

		if (index == 4) return "" + (int) BURN_LEVEL_BONUS;
		if (index == 5) return "" + Math.round((1f - PROFILE_MULT) * 100f) + txt("%");

		if (index == 6) return txt("hmd_LIRefit1");

		return null;
	}
	

}
