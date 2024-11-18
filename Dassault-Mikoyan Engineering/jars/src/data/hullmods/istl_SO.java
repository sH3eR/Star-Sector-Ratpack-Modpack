package data.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.util.Misc;

public class istl_SO extends BaseHullMod {

	private static Map speed = new HashMap();
	static {
		speed.put(HullSize.FRIGATE, 50f);
		speed.put(HullSize.DESTROYER, 35f);
		speed.put(HullSize.CRUISER, 20f);
		speed.put(HullSize.CAPITAL_SHIP, 10f);
	}
	
	private static final float PEAK_MULT = 0.33f;
	private static final float FLUX_DISSIPATION_MULT = 2f;
	
	private static final float RANGE_THRESHOLD = -150f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().modifyFlat(id, (Float) speed.get(hullSize));
		stats.getAcceleration().modifyFlat(id, (Float) speed.get(hullSize) * 2f);
		stats.getDeceleration().modifyFlat(id, (Float) speed.get(hullSize) * 2f);
		stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 5f); // set to five, meaning boost is always on 
		
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
		
		stats.getPeakCRDuration().modifyMult(id, PEAK_MULT);

		stats.getVentRateMult().modifyMult(id, 0f);
		
		stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD);		

	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) speed.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) speed.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) speed.get(HullSize.CRUISER)).intValue();
		if (index == 3) return Misc.getRoundedValue(FLUX_DISSIPATION_MULT);
		if (index == 4) return Misc.getRoundedValue(PEAK_MULT);
		if (index == 5) return Misc.getRoundedValue(RANGE_THRESHOLD);
		
		return null;
	}

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Must be installed on a Dassault-Mikoyan ship";
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a DME hull id
        return ship.getHullSpec().getHullId().startsWith("istl_");
    }
	

	private Color color = new Color(255,100,255,255);
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
			ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.4f);
			ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
	}

	

}
