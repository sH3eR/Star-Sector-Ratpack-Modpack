package data.scripts.xo.andradanism;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class PriorityRequisition extends SCBaseSkillPlugin {
	
	private static float CREW_CASUALTIES = -50f;
	
	@Override
    public String getAffectsString() {
        return "all Lion's Guard ships in the fleet";
    }
    
	@Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("No blood or treasure shall be spared in the defense of Askonia.", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addPara("  -Quotations from the Supreme Executor", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addSpacer(10f);
		tooltip.addPara("Special Modifications hullmod no longer inflicts penalties", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("Also negates the crew casualty penalty of the Energy and Modular Bolt Coherer", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("No benefit if Special Modifications has been removed", 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
	}

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
		if (variant.hasHullMod("coherer")) stats.getCrewLossMult().modifyPercent(id, CREW_CASUALTIES);
		if (data.isPlayer()) {
			if (variant.hasHullMod("andrada_mods")) variant.addPermaMod("vice_special_modifications");
		}
    }
	
	//hullmod swapping for NPCs handled by EnemyEncounterListener
	@Override
	public void onActivation(SCData data) {
		if (data.isNPC()) data.getFleet().getMemoryWithoutUpdate().set("$xo_andrada_fleet", true);
		if (data.isPlayer()) Global.getSector().getMemoryWithoutUpdate().set("$xo_andrada_is_active", true);
	}
	
	@Override
	public void onDeactivation(SCData data) {
		if (data.isPlayer()) Global.getSector().getMemoryWithoutUpdate().set("$xo_andrada_is_active", false);
	}
}