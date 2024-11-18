package data.scripts.xo.andradanism;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class SupremeLeadership extends SCBaseSkillPlugin {
    
	private static float CR_BOOST = 0.05f;
	private static float ML_BOOST = 0.10f;
	private static float LG_BOOST = 0.15f;
	
	@Override
    public String getAffectsString() {
        return "all ships in the fleet";
    }

    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("If there is to be revolution, there must be a revolutionary leader, a Supreme Executor of the people's will.", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addPara("  -Quotations from the Supreme Executor", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addSpacer(10f);
        tooltip.addPara("15%% to combat readiness for Lion's Guard ships", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		String s = "10%% to combat readiness for midline";
		if (Global.getSettings().getModManager().isModEnabled("PAGSM")) s += " and Sindrian Fuel Company ships"; 
		else s += " ships";
		tooltip.addPara(s, 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("5%% to combat readiness for all other ships", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("Exclusive with Management aptitude Crew Training skill", 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
    }
	
    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
        String manufacturer = variant.getHullSpec().getManufacturer();
		float bonus = CR_BOOST;
		if (manufacturer.equals("Midline") 
					|| manufacturer.equals("Sindrian Diktat") 
					|| manufacturer.equals("Sindrian Fuel Company")) bonus = ML_BOOST;
		else if (manufacturer.equals("Lion's Guard")) bonus = LG_BOOST;
		if (data.isSkillActive("sc_management_crew_training")) return;
		stats.getMaxCombatReadiness().modifyFlat(id, bonus, "Andradanism");
    }
}