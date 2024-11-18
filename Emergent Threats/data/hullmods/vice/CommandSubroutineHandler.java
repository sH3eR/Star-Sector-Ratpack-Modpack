package data.hullmods.vice;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class CommandSubroutineHandler extends BaseHullMod {
	
	private static String SYNTHESIS_MOD_ID = "vice_adaptive_tactical_core";
	private static String SYNTHESIS_CORE_ID = "xo_synthesis_core";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//deletes core if hullmod is no longer present or Command Subroutine skill is no longer active
		if ((!stats.getVariant().hasHullMod(SYNTHESIS_MOD_ID) || !isCommandSubroutineActive())
					&& stats.getFleetMember() != null 
					&& SYNTHESIS_CORE_ID.equals(stats.getFleetMember().getCaptain().getAICoreId())) {
			stats.getFleetMember().setCaptain(Global.getFactory().createPerson());
			stats.getFleetMember().updateStats();
		}
	}
	
	private boolean isCommandSubroutineActive() {
		return Global.getSector().getMemoryWithoutUpdate().is("$xo_command_subroutine_is_active", true);
	}
}