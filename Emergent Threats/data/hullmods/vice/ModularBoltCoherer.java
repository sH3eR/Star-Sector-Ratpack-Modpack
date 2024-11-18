package data.hullmods.vice;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ModularBoltCoherer extends BaseHullMod {
	
	private static float PULSE_RANGE_BONUS = 100f;
	private static float CASUALTY_PENALTY = 25f;
	private static float CASUALTY_PENALTY_XO = 0f;
	private static float FLUX_COST_BONUS = 5f;
	
	private static String CONFLICT_MOD = "coherer";
	private static String CONFLICT_MOD_2 = "vice_adaptive_pulse_resonator";
	private static String NEGATE_MOD = "vice_special_modifications";
	
	private static boolean isPriorityRequisitionActive = false;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponRangeBonus().modifyFlat(id, PULSE_RANGE_BONUS);
		stats.getBeamWeaponRangeBonus().modifyFlat(id, -PULSE_RANGE_BONUS);
		if (isSMod(stats)) stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - FLUX_COST_BONUS * 0.01f);
		if (stats.getVariant().hasHullMod(NEGATE_MOD)) isPriorityRequisitionActive = true;
		else {
			stats.getCrewLossMult().modifyPercent(id, CASUALTY_PENALTY);
			isPriorityRequisitionActive = false;
		}
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (isForModSpec || ship == null) return;
		if (ship.getVariant().hasHullMod(NEGATE_MOD)) {
			tooltip.addPara("Casualty penalty is %s by Special Modifications", 10f, Misc.getHighlightColor(), "negated");
		}
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		return (!ship.getVariant().hasHullMod(CONFLICT_MOD) && !ship.getVariant().hasHullMod(CONFLICT_MOD_2));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod(CONFLICT_MOD) || ship.getVariant().hasHullMod(CONFLICT_MOD_2)) return "Comparable system is already installed";
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) PULSE_RANGE_BONUS;
		if (index == 1) return "" + (int) CASUALTY_PENALTY + "%";
		return null;
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FLUX_COST_BONUS + "%";
		return null;
	}
}