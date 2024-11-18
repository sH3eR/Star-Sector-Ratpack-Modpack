package data.scripts.xo.synthesis;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

import data.scripts.vice.util.RemnantSubsystemsUtil;

public class CompactAutomation extends SCBaseSkillPlugin {
	
	//private static String BONUS_MOD_ID = "vice_compact_automation";
	private static String BONUS_MOD_ID_HANDLER = "vice_compact_automation_handler";
	
	@Override
    public String getAffectsString() {
        return "all ships in the fleet";
    }

    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
        tooltip.addPara("Increases ship fitting by 3/5/7/10 ordnance points, based on hull size", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
		if (data.isPlayer() && !variant.hasHullMod(BONUS_MOD_ID_HANDLER)) {
			variant.addPermaMod(BONUS_MOD_ID_HANDLER);
		}
    }
	
	//skill is never picked by NPC fleets, bonus hullmod self deletes if skill is deactivated
	@Override
	public void onActivation(SCData data) {
		if (data.isPlayer()) Global.getSector().getMemoryWithoutUpdate().set("$xo_compact_automation_is_active", true);
	}
	
	@Override
	public void onDeactivation(SCData data) {
		if (data.isPlayer()) Global.getSector().getMemoryWithoutUpdate().set("$xo_compact_automation_is_active", false);
	}
}
