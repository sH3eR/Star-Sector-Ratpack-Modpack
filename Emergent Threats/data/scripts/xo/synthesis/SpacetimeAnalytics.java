package data.scripts.xo.synthesis;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class SpacetimeAnalytics extends SCBaseSkillPlugin {
    
	private static float FUEL_REDUCTION_MULT = 0.25f;
	
	@Override
    public String getAffectsString() {
        return "all ships in the fleet";
    }

    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("+1 system charge for ships with a Degraded Phase Skimmer", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		//tooltip.addPara("+1 extra burn level for ships with less than 8 burn", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addSpacer(10f);
		tooltip.addPara("Abyssal Entropy Projector will always chain to a second target", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("Adaptive Entropy Projector gains 20%% bonus to recharge time and range", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
		//if (stats.getMaxBurnLevel().getModifiedValue() < 8f) stats.getMaxBurnLevel().modifyFlat(id, 1f);
		if (stats.getVariant().hasHullMod("ix_converted_hull")) stats.getSystemUsesBonus().modifyFlat(id, 1);
    }
	
	//entropy projector hullmod mem check returns true only for player ships
	@Override
	public void onActivation(SCData data) {
		if (data.isPlayer()) Global.getSector().getMemoryWithoutUpdate().set("$xo_spacetime_analytics_is_active", true);
	}
	
	@Override
	public void onDeactivation(SCData data) {
		if (data.isPlayer()) Global.getSector().getMemoryWithoutUpdate().set("$xo_spacetime_analytics_is_active", false);
	}
}
