package data.scripts.xo.andradanism;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class DoctrinalPurity extends SCBaseSkillPlugin {
	
	@Override
    public String getAffectsString() {
        return "all carriers with a Fleet Override";
    }
    
	@Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("Discipline is life.", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addPara("  -Quotations from the Supreme Executor", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addSpacer(10f);
		tooltip.addPara("Built-in and Modular Fleet Override fighter damage bonus increased by 5%%", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("Gain access to the Modular Fleet Override hullmod", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
	}
    
	//bonus handled by hullmods
	@Override
	public void onActivation(SCData data) {
		if (data.isPlayer()) {
			CharacterDataAPI player = Global.getSector().getCharacterData();
			player.addHullMod("vice_modular_fleet_override");
			Global.getSector().getMemoryWithoutUpdate().set("$xo_doctrinal_purity_is_active", true);
		}
	}
	
	@Override
	public void onDeactivation(SCData data) {
		if (data.isPlayer()) Global.getSector().getMemoryWithoutUpdate().set("$xo_doctrinal_purity_is_active", false);
	}
}