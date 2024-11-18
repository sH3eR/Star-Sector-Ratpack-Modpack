package data.scripts.xo.synthesis;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class ReactorMonitoring extends SCBaseSkillPlugin {
    
	private static float REACTOR_BONUS = 5f;
	private static float REACTOR_VENT_BONUS = 20f;
		
	@Override
    public String getAffectsString() {
        return "all ships in the fleet";
    }
	
    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("Ships with an Adaptive Reactor Chamber gain 20%% bonus to flux dissipation rate while venting", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("Ships without an Adaptive Reactor Chamber gain 5%% improvement to flux capacity and dissipation", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
        if (!variant.hasHullMod("vice_adaptive_reactor_chamber")) {
			stats.getFluxCapacity().modifyMult(id, 1f + REACTOR_BONUS * 0.01f);
			stats.getFluxDissipation().modifyMult(id, 1f + REACTOR_BONUS * 0.01f);
		}
		else stats.getVentRateMult().modifyMult(id, 1f + REACTOR_VENT_BONUS * 0.01f);
    }
}
