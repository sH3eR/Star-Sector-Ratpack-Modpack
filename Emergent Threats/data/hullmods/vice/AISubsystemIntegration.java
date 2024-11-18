package data.hullmods.vice;

import java.util.LinkedHashSet;

import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

import data.scripts.vice.util.RemnantSubsystemsUtil;

public class AISubsystemIntegration extends BaseLogisticsHullMod {
	
	private static float NO_MIN_CREW_BONUS = 0f;
	private static float CREWED_SQUADRON_SPEED_PENALTY = 20f;
	private static String ADAPTIVE_SUBSYSTEMS = "adaptive subsystems";
	private static String FIGHTER_AUTOMATION = "Built-in fighters";

	private static String REPLACEMENT_S_MOD = "vice_abomination_interface";
	private static String SHIPWIDE_INTEGRATION_CHECKER = "vice_shipwide_integration_checker";
	private static String ERROR_MOD_ID = "vice_drone_bay_malfunction";

	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	//RemnantSubsystemsUtil uses this hullmod to check for permission to equip subsystems on non-remnant ships.
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();
		if (isBuiltInMod(variant, id)) {
			if (!variant.getModuleSlots().isEmpty()) variant.addPermaMod(SHIPWIDE_INTEGRATION_CHECKER);
			stats.getMinCrewMod().modifyMult(id, 0);
			stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, NO_MIN_CREW_BONUS);
			util.applyShipwideIntegration(variant);
		}
		else return;
		
		boolean isAllValidDrones = true;
		int builtInOffset = stats.getVariant().getFittedWings().size() - stats.getVariant().getNonBuiltInWings().size();
		int fighterBays = stats.getNumFighterBays().getModifiedInt();
		for (int i = 0; i < fighterBays; i++) {
			if (i < builtInOffset) continue;
			FighterWingSpecAPI spec = stats.getVariant().getWing(i);
			if (spec == null) continue;
			if (spec.getVariant().getHullSpec().getMinCrew() != 0) isAllValidDrones = false;
		}
		if (stats.getVariant().hasHullMod("SKR_remote")) isAllValidDrones = true;
		if (!isAllValidDrones) stats.getVariant().getHullMods().add(ERROR_MOD_ID);
		else stats.getVariant().getHullMods().remove(ERROR_MOD_ID);
	}
	
	//needed due to bounty ships with non-smod built-in versions
	private boolean isBuiltInMod(ShipVariantAPI variant, String id) {
		if (variant.getHullSpec().isBuiltInMod(id)) return true;
		if (variant.getSMods().contains(id)) return true;
		return false;
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return false;
		return (util.isSubsystemIntegrationApplicable(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return "Integration is performed on the central hub";
		if (!util.isMinCrewWithinAutomationLimit(ship)) return "Minimum crew exceeds " + (int) util.getAutomationCrewLimit();
		if (util.isAbomination(ship)) return "Hull is incompatible with automation overhaul";
		if (!util.isSubsystemIntegrationApplicable(ship)) return util.getIncompatibleCauseString("unnecessary");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return ADAPTIVE_SUBSYSTEMS;
		if (index == 1) return "" + (int) NO_MIN_CREW_BONUS;
		if (index == 2) return FIGHTER_AUTOMATION;
		if (index == 3) return "";
		
		return null;
	}
}