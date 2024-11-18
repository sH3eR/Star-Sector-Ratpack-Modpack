package data.hullmods.vice;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

import data.scripts.vice.util.RemnantSubsystemsUtil;

public class AdaptiveDroneBay extends BaseHullMod {

	private static float EXTRA_BAYS = 1f;
	private static String VAST_HANGAR = "vast_hangar";
	private static String DRONE_BAY = "combat drone bay";
	
	//exception shipIds always qualify
	private static String MIMESIS = "vice_mimesis";
	private static String MIMESIS_D = "vice_mimesis_default_D";
	private static String ERROR_MOD_ID = "vice_drone_bay_malfunction";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float bays = EXTRA_BAYS;
		if (stats.getVariant().hasHullMod(VAST_HANGAR)) bays++;
		stats.getNumFighterBays().modifyFlat(id, bays);
		
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
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (!isApplicableToShip(ship) && ship.getOwner() == 0) ship.getVariant().getHullMods().remove(id);
	}
	
	private boolean isPhaseShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("phasefield") || ship.getVariant().getHullSpec().isPhase()) return true;
		return false;
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.isFrigate()) return false;
		if (ship.getHullSpec().getHullId().equals(MIMESIS) || ship.getHullSpec().getHullId().equals(MIMESIS_D)) return true;
		if (ship.getVariant().hasHullMod("ix_odyssey_retrofit")) return false;
		if (util.isModuleCheck(ship)) return false;
		if (ship.getHullSpec().getFighterBays() > 0) return false;
		if (ship.getVariant().hasHullMod("tw_trinity_retrofit")) return false;
		if (ship.getVariant().hasHullMod("converted_hangar")) return false;
		if (ship.getVariant().hasHullMod("converted_fighterbay")) return false;
		if (ship.getVariant().hasHullMod("roider_fighterClamps")) return false;
		if (ship.getVariant().hasHullMod("magellan_convertedbay")) return false;
		if (isPhaseShip(ship)) return false;

		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship));
	}

	public String getUnapplicableReason(ShipAPI ship) {
		if (util.isModuleCheck(ship)) return util.getIncompatibleCauseString("module");
		if (ship.isFrigate()) return "Cannot be installed on frigate";
		if (ship.getVariant().hasHullMod("ix_odyssey_retrofit")) return "Incompatible hull, install Terminus Relay to enable drones";
		if (ship.getHullSpec().getFighterBays() > 0) return "Ship has standard fighter bays";
		if (ship.getVariant().hasHullMod("tw_trinity_retrofit")) return "Incompatible hull";
		if (ship.getVariant().hasHullMod("converted_hangar")) return "Ship has a converted hangar";
		if (ship.getVariant().hasHullMod("converted_fighterbay")) return "Ship has converted fighter bays";
		if (ship.getVariant().hasHullMod("magellan_convertedbay")) return "Ship has converted fighter bays";
		if (ship.getVariant().hasHullMod("roider_fighterClamps")) return "Ship has fighter clamps";
		if (isPhaseShip(ship)) return "Cannot be installed on phase ship";
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return DRONE_BAY;
		return null;
	}
}