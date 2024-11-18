package data.scripts.xo.andradanism;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class ForwardToVictory extends SCBaseSkillPlugin {
    
	private static float MANEUVER_BONUS = 25f;
	private static float ZERO_FLUX_LEVEL = 10f;
	private static float FUEL_USAGE = 1.15f;
	
	@Override
    public String getAffectsString() {
        return "all ships in the fleet";
    }
    
	@Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("Strike first, strike fast, and you shall always know victory.", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addPara("  -Quotations from the Supreme Executor", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addSpacer(10f);
		tooltip.addPara("0-flux speed bonus is active at or below 10%% flux", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("+25%% to ship maneuverability", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("+15%% fuel usage", 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
	}

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
		stats.getAcceleration().modifyPercent(id, MANEUVER_BONUS * 2f);
		stats.getDeceleration().modifyPercent(id, MANEUVER_BONUS);
		stats.getTurnAcceleration().modifyPercent(id, MANEUVER_BONUS * 2f);
		stats.getMaxTurnRate().modifyPercent(id, MANEUVER_BONUS);
		stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, ZERO_FLUX_LEVEL * 0.01f);
		stats.getFuelUseMod().modifyMult(id, FUEL_USAGE);
    }
}