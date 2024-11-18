package data.scripts.xo.andradanism;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class BattlefieldPersistence extends SCBaseSkillPlugin {
    
	private static float REPAIR_BONUS = 25f;
	private static float PEAK_OPERATING_BONUS = 25f;
	
	@Override
    public String getAffectsString() {
        return "all ships in the fleet";
    }
    
	@Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("Through adversity, knowledge. Through hardship, strength. Through persistence, victory.", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addPara("  -Quotations from the Supreme Executor", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addSpacer(10f);
		tooltip.addPara("+25%% peak operating time", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("+25%% faster weapon and engine repairs in combat", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("+25%% faster ship repairs and readiness recovery out of combat", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
	}

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
		stats.getPeakCRDuration().modifyPercent(id, PEAK_OPERATING_BONUS);
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
		stats.getBaseCRRecoveryRatePercentPerDay().modifyPercent(id, REPAIR_BONUS);
		stats.getRepairRatePercentPerDay().modifyPercent(id, REPAIR_BONUS);
    }
}