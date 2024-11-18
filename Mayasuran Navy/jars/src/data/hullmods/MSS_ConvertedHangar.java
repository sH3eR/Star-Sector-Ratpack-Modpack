package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class MSS_ConvertedHangar extends BaseHullMod {

	public static final int CREW_REQ = 20;
	public static final float REFIT_PENALTY = 25f;
	public static final int BOMBER_COST_PERCENT = 100;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFighterRefitTimeMult().modifyPercent(id, REFIT_PENALTY);
		stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyPercent(id, BOMBER_COST_PERCENT);
		stats.getMinCrewMod().modifyFlat(id, CREW_REQ);
		stats.getNumFighterBays().modifyFlat(id, 1f);
	}
	
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) REFIT_PENALTY + "%";
		if (index == 1) return "" + CREW_REQ;
		if (index == 2) return "" + BOMBER_COST_PERCENT + "%";
		return null;
		//if (index == 0) return "" + ((Float) mag.get(hullSize)).intValue();
		//return null;
	}
}



