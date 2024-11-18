package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

//Subsystem compatibility handled by Emergent Threats core mod
public class SemiAutomated extends BaseHullMod {

	private static float MIN_CREW = 0f;
	private static float MAX_CREW = 0f;
	private static float DP_INCREASE = 2f;
	private static String CREWLESS = "Crewless";
	private static String SUBSYSTEMS = "adaptive subsystems";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMinCrewMod().modifyMult(id, MIN_CREW);
		stats.getMaxCrewMod().modifyMult(id, MAX_CREW);
		stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, DP_INCREASE);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return CREWLESS;
		if (index == 1) return SUBSYSTEMS;
		if (index == 2) return "" + (int) DP_INCREASE;
		return null;
	}
}