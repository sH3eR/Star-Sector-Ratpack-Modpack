package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class MSS_Prime extends BaseHullMod
{

	/*
	 * A Ship of the 14th Domain Battlegroup well-maintained survivor of the original battlegroup which founded the Hegemony Sterling example of the Domain Navy's traditional "decisive battle" doctrine focus on superior armour and firepower on ships of the line to destroy the enemy - slightly better flux handling - slightly better armour - slightly worse speed/maneuver -
	 */

	// private static final float ARMOR_BONUS_MULT = 1.1f;
	// private static final float ARMOR_BONUS = 100f;
	private static final float CAPACITY_MULT = 1.05f;
	private static final float DISSIPATION_MULT = 1.05f;
	private static final float HANDLING_MULT = 0.92f;
	public static final float REFIT_BUFF = 15f;
	// public static final float SUPPLIES_PENALTY = 1.25f;
	public static final float BOMBER_COST_DISCOUNT = 2;

	private static Map<HullSize, Float> mag = new HashMap<>();
	static
	{
		mag.put(HullSize.FRIGATE, 50f);
		mag.put(HullSize.DESTROYER, 100f);
		mag.put(HullSize.CRUISER, 100f);
		mag.put(HullSize.CAPITAL_SHIP, 100f);
	}
	/*
	 * private static Map mag = new HashMap(); static { mag.put(HullSize.FIGHTER, 0.0f); mag.put(HullSize.FRIGATE, 0.25f); mag.put(HullSize.DESTROYER, 0.15f); mag.put(HullSize.CRUISER, 0.10f); mag.put(HullSize.CAPITAL_SHIP, 0.05f); }
	 */

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
	{

		// Slightly better armour
		stats.getArmorBonus().modifyFlat(id, (Float) mag.get(hullSize));
		// Fighter Bay
		stats.getFighterRefitTimeMult().modifyPercent(id, -REFIT_BUFF);
		// stats.getMinCrewMod().modifyFlat(id, CREW_REQ);
		// stats.getNumFighterBays().modifyFlat(id, 1f);

		// 10% better flux stats
		stats.getFluxCapacity().modifyMult(id, CAPACITY_MULT);
		stats.getFluxDissipation().modifyMult(id, DISSIPATION_MULT);

		// 25% more supply cost
		// stats.getSuppliesPerMonth().modifyMult(id, SUPPLIES_PENALTY);
		// stats.getSuppliesToRecover().modifyMult(id, SUPPLIES_PENALTY);

		// 2 OP discount for Bombers
		stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyFlat(id, -BOMBER_COST_DISCOUNT);

		// 8% worse handling
		stats.getMaxSpeed().modifyMult(id, HANDLING_MULT);
		stats.getAcceleration().modifyMult(id, HANDLING_MULT);
		stats.getDeceleration().modifyMult(id, HANDLING_MULT);
		stats.getMaxTurnRate().modifyMult(id, HANDLING_MULT);
		stats.getTurnAcceleration().modifyMult(id, HANDLING_MULT);

		// 10% better maneuvering
		// stats.getMaxSpeed().modifyMult(id, HANDLING_MULT);
		// stats.getAcceleration().modifyMult(id, HANDLING_MULT);
		// stats.getDeceleration().modifyMult(id, HANDLING_MULT);
		// stats.getMaxTurnRate().modifyMult(id, HANDLING_MULT);
		// stats. getTurnAcceleration().modifyMult(id, HANDLING_MULT);
		// 0.2 better shield
		// stats.getShieldAbsorptionMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		// +10 better zero flux bonus
		// stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_BOOST_BONUS);
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize)
	{
		// if (index == 4) return "" + (int) ((HANDLING_MULT - 1f) * 100f); // int) ((1f - CORONA_EFFECT_REDUCTION) * 100f);
		// if (index == 3) return "" + (int) (ZERO_FLUX_BOOST_BONUS);
		if (index == 2)
			return "" + (int) ((CAPACITY_MULT - 1f) * 100f) + "%";
		// if (index == 2) return "" + (int) (SHIELD_BONUS);
		if (index == 0)
			return "" + (int) REFIT_BUFF + "%";
		if (index == 3)
			return "" + (mag.get(HullSize.FRIGATE).intValue());
		if (index == 4)
			return "" + (mag.get(HullSize.DESTROYER).intValue());
		if (index == 5)
			return "" + (mag.get(HullSize.CRUISER).intValue());
		if (index == 6)
			return "" + (mag.get(HullSize.CAPITAL_SHIP).intValue());
		if (index == 1)
			return "" + (int) BOMBER_COST_DISCOUNT + "";
		if (index == 7)
			return "" + (int) ((1f - HANDLING_MULT) * 100f) + "%";
		// if (index == 7) {
		// return "" + Math.round(SUPPLIES_PENALTY * 100 - 100);

		// if (index == 5) return "" + CREW_REQ;
		return null;
		// if (index == 0) return "" + ((Float) mag.get(hullSize)).intValue();
		// return null;
	}

	@Override
	public boolean affectsOPCosts()
	{
		return true;
	}
}