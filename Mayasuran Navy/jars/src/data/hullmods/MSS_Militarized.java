package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MSS_Militarized extends BaseHullMod {

	/* Militarized Auxiliary 
	 * Basically take a civilian hull, bring it up to military standards. (More or less.)
	 * - military sensor profile/detection (handled in skin by removing civ hull)
	 * - more OP (handled in skin)
	 * - slightly better flux handling
	 * - slightly better shields
	 * - slightly better maneuvering
	 * - added fighter bay
	 */
	private static final float CAPACITY_MULT = 1.1f;
	private static final float DISSIPATION_MULT = 1.1f;
	private static final float HANDLING_MULT = 1.1f;
	private static final float SHIELD_BONUS = 20f;
	private static final float ZERO_FLUX_BOOST_BONUS = 10f;
	private static final int CREW_REQ = 20;
	private static final float REFIT_PENALTY = 25f;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		// Slightly better armour
		//stats.getArmorBonus().modifyPercent(id, (Float) mag.get(hullSize));
		// Fighter Bay
		stats.getFighterRefitTimeMult().modifyPercent(id, REFIT_PENALTY);
		stats.getMinCrewMod().modifyFlat(id, CREW_REQ);
		stats.getNumFighterBays().modifyFlat(id, 1f);
		
		// 10% better flux stats
		stats.getFluxCapacity().modifyMult(id, CAPACITY_MULT);
		stats.getFluxDissipation().modifyMult(id, DISSIPATION_MULT);
		
		// 10% better maneuvering
		//stats.getMaxSpeed().modifyMult(id, HANDLING_MULT);
		stats.getAcceleration().modifyMult(id, HANDLING_MULT);
		stats.getDeceleration().modifyMult(id, HANDLING_MULT);
		stats.getMaxTurnRate().modifyMult(id, HANDLING_MULT);
		stats. getTurnAcceleration().modifyMult(id, HANDLING_MULT);
		// 0.2 better shield
		stats.getShieldAbsorptionMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		// +10 better zero flux bonus
		stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_BOOST_BONUS);
		}
		
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((HANDLING_MULT - 1f) * 100f); // int) ((1f - CORONA_EFFECT_REDUCTION) * 100f);
		if (index == 3) return "" + (int) (ZERO_FLUX_BOOST_BONUS); 
		if (index == 1) return "" + (int) ((CAPACITY_MULT - 1f) * 100f);
		if (index == 2) return "" + (int) (SHIELD_BONUS);
		if (index == 4) return "" + (int) REFIT_PENALTY + "%";
		if (index == 5) return "" + CREW_REQ;
		return null;
		//if (index == 0) return "" + ((Float) mag.get(hullSize)).intValue();
		//return null;
	}


}
