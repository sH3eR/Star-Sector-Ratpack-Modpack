package data.hullmods.vice;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import com.fs.starfarer.api.combat.ShipAPI;

public class CompactAutomationHandler extends BaseHullMod {
	
	private static String BONUS_MOD_ID = "vice_compact_automation";
	private static String ERROR_MOD_ID = "vice_compact_automation_error";
	private static String ERROR_MOD_TW = "tw_equipment_error";
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		//if XO is active, add hullmod, otherwise remove it
		if (Global.getSector().getMemoryWithoutUpdate().is("$xo_compact_automation_is_active", true)) {
			if (!stats.getVariant().hasHullMod(BONUS_MOD_ID)) stats.getVariant().addMod(BONUS_MOD_ID);
		}
		else stats.getVariant().getHullMods().remove(BONUS_MOD_ID);
		
		//remove error mod and do nothing if TW drone error mod already present
		if (stats.getVariant().hasHullMod(ERROR_MOD_TW)) {
			stats.getVariant().getHullMods().remove(ERROR_MOD_ID);
			return;
		}
		
		//workaround for unofficial new game plus incompatability
		if (Global.getSettings().getModManager().isModEnabled("ungp")) return;

		int shipOp = getShipOP(stats.getVariant());
		int usedOp = getUsedOP(stats.getVariant());
		int op = shipOp - usedOp;
		
		//add error mod if over the OP limit
		if (op < 0 && !stats.getVariant().hasHullMod(ERROR_MOD_ID) && !stats.getVariant().hasHullMod(ERROR_MOD_TW)) {
			stats.getVariant().getHullMods().add(ERROR_MOD_ID);
		}
		//remove the error mod if under or at the OP limit
		else if (op >= 0) {
			stats.getVariant().getHullMods().remove(ERROR_MOD_ID);
		}
	}
	
	private int getShipOP(ShipVariantAPI variant) {
		int points = 0;
		try {
			points = variant.getHullSpec().getOrdnancePoints(Global.getSector().getCharacterData().getPerson().getFleetCommanderStats()); 
		}
		catch (Exception e) {
			points = variant.getHullSpec().getOrdnancePoints(Global.getFactory().createPerson().getFleetCommanderStats());
		}
		return points;
	}
	
	private int getUsedOP(ShipVariantAPI variant) {
		int points = 0;
		try {
			points = variant.computeOPCost(Global.getSector().getCharacterData().getPerson().getFleetCommanderStats()); 
		}
		catch (Exception e) {
			points = variant.computeOPCost(Global.getFactory().createPerson().getFleetCommanderStats());
		}
		return points;
	}
}