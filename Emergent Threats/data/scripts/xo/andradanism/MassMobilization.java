package data.scripts.xo.andradanism;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class MassMobilization extends SCBaseSkillPlugin {
	
	private static float DP_REDUCTION = 10f;
	private static float DP_REDUCTION_LG = 15f;
	
	@Override
    public String getAffectsString() {
        return "all non-automated ships in the fleet";
    }
    
	@Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("The people build the future, the Party guides the people, the Supreme Executor leads the Party. We all have our place in the Movement.", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addPara("  -Quotations from the Supreme Executor", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addSpacer(10f);
		tooltip.addPara("15%% deployment cost reduction for Lion's Guard ships, up to 10 points", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("10%% deployment cost reduction for all other ships, up to 10 points", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("Exclusive with Management aptitude In Good Hands skill", 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
	}
	
    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
		if (data.isSkillActive("sc_management_in_good_hands")) return;
		if (!variant.hasHullMod("automated")) {
			String manufacturer = variant.getHullSpec().getManufacturer();
			float baseCost = stats.getSuppliesToRecover().getBaseValue();
			float reductionMult = manufacturer.equals("Lion's Guard") ? DP_REDUCTION_LG : DP_REDUCTION;
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult(id, 1f - reductionMult * 0.01f);
		}
	}
}