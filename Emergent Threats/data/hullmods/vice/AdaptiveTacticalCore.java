package data.hullmods.vice;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.vice.util.RemnantSubsystemsUtil;
import data.scripts.vice.SynthesisCorePlugin;

public class AdaptiveTacticalCore extends BaseHullMod {
	
	private static float SHIELD_DAMAGE_REDUCTION = 15f;
	private static float PHASE_FLUX_UPKEEP_REDUCTION = 25f;
	private static float DAMAGE_TO_DESTROYERS = 10;
	private static float DAMAGE_TO_CRUISERS = 15;
	private static float DAMAGE_TO_CAPITALS = 20;
	
	private static String FIELD_MODULATION = "Field Modulation";
	private static String TARGET_ANALYSIS = "Target Analysis";
	private static String THIS_MOD = "vice_adaptive_tactical_core";
	private static String RAT_CONFLICT_MOD = "rat_delta_assistant";
	private static String SW_INTEGRATION_MOD = "vice_shipwide_integration";
	
	private static String HANDLER_MOD_ID = "xo_command_subroutine_handler";
	private static String SYNTHESIS_CORE_ID = "xo_synthesis_core";
	
	//Utility variables
	private RemnantSubsystemsUtil util = new RemnantSubsystemsUtil();
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		util.applyShipwideHullMod(stats.getVariant(), id, true);
		util.addModuleHandler(stats);
		
		if (util.isWithoutCaptain(stats, true) && isCommandSubroutineActive() && !util.isModuleCheck(stats)) {
			stats.getVariant().addMod(HANDLER_MOD_ID);
			if (stats.getFleetMember() == null 
					|| stats.getFleetMember().getOwner() != 0 //should never apply to enemy fleets
					|| stats.getFleetMember().getCaptain() == null 
					|| !stats.getFleetMember().getCaptain().isDefault()) return;
			PersonAPI p = new SynthesisCorePlugin().createPerson(SYNTHESIS_CORE_ID, "player", null);
			stats.getFleetMember().setCaptain(p);
			removeCore();
		}
	}
	
	private boolean isCommandSubroutineActive() {
		return Global.getSector().getMemoryWithoutUpdate().is("$xo_command_subroutine_is_active", true);
	}
	
	private void removeCore() {
		try {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			cargo.removeCommodity(SYNTHESIS_CORE_ID, 1f);
		}
		catch (Exception e) {}
	}
	
	//put here because effect always gets applied if left in applyEffectsBeforeShipCreation
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		//self clear if invalid player hull, but do not clear on modules since they can be added by handler
		if (!isApplicableToShip(ship) && ship.getOwner() == 0 && !util.isModuleCheck(ship)) {
			ship.getVariant().getHullMods().remove(id);
		}
		
		MutableShipStatsAPI stats = ship.getMutableStats();
		if (util.isWithoutCaptain(stats)) {
			stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_DAMAGE_REDUCTION / 100f);
			stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, 1f - PHASE_FLUX_UPKEEP_REDUCTION / 100f);
			stats.getDamageToDestroyers().modifyPercent(id, DAMAGE_TO_DESTROYERS);
			stats.getDamageToCruisers().modifyPercent(id, DAMAGE_TO_CRUISERS);
			stats.getDamageToCapital().modifyPercent(id, DAMAGE_TO_CAPITALS);
		}
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (isCommandSubroutineActive() && !util.isModuleCheck(ship)) return true;
		if (ship.getVariant().hasHullMod(RAT_CONFLICT_MOD)) return false;
		if (util.isModuleCheck(ship)) return false;
		if (ship.getVariant().hasHullMod(HullMods.NEURAL_INTERFACE) 
				|| ship.getVariant().hasHullMod(HullMods.NEURAL_INTEGRATOR)) return false;
		return (util.isApplicable(ship) && util.isOnlyRemnantMod(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod(SW_INTEGRATION_MOD) && (ship.getVariant().hasHullMod(THIS_MOD))) return null;
		if (ship.getVariant().hasHullMod(RAT_CONFLICT_MOD)) return "Assistant AI core already installed";
		if (util.isModuleCheck(ship)) return util.getIncompatibleCauseString("hub");
		if (ship.getVariant().hasHullMod(HullMods.NEURAL_INTERFACE) 
				|| ship.getVariant().hasHullMod(HullMods.NEURAL_INTEGRATOR)) return "Incompatible neural interface present";
		if (!util.isApplicable(ship)) return util.getIncompatibleCauseString("manufacturer");
		if (!util.isOnlyRemnantMod(ship)) return util.getIncompatibleCauseString("modcount");
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (isForModSpec || ship == null || !ship.getVariant().hasHullMod(THIS_MOD)) return;
		String s = "";
		String h = "";
		String h2 = "";
		if (util.isWithoutCaptain(ship.getMutableStats(), true) && isCommandSubroutineActive()) {
			s = "Tactical core has been %s by an executive officer, and performs as a %s when active.";
			h = "upgraded";
			h2 = "gamma-level AI core";
			tooltip.addPara(s, 10f, Misc.getHighlightColor(), h, h2);
			return;
		}
		else if (util.isWithoutCaptain(ship.getMutableStats())) {
			s = "Tactical core is %s.";
			h = "active";
		}
		else {
			s = "Tactical core is %s due to the presence of a captain.";
			h = "inactive";
		} 
		tooltip.addPara(s, 10f, Misc.getHighlightColor(), h);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return FIELD_MODULATION;
		if (index == 1) return TARGET_ANALYSIS;
		
		return null;
	}
}