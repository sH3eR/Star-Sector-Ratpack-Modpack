package data.scripts.xo.synthesis;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class MindMachineInterface extends SCBaseSkillPlugin {
	
	private static float CR_BONUS = 6f;
	public static float PEAK_BONUS_PERCENT = 25f;
	private static String BONUS_MOD_ID = "vice_adaptive_neural_net";
	
	@Override
    public String getAffectsString() {
        return "all ships with human officers";
    }

    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("+25%% peak performance time", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("Human officers gain the maximum 10%% bonus to combat readiness when Adaptive Neural Net is enabled", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
	}
	
    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
		boolean isHuman = false;
		if (stats.getFleetMember() != null) isHuman = !stats.getFleetMember().getCaptain().isAICore();
		if (isHuman) {
			stats.getPeakCRDuration().modifyPercent(id, PEAK_BONUS_PERCENT);
			if (variant.hasHullMod(BONUS_MOD_ID)) {
				stats.getMaxCombatReadiness().modifyFlat(id, CR_BONUS * 0.01f, "Mind-machine interface");
			}
		}
	}
}