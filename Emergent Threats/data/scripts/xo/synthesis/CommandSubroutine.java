package data.scripts.xo.synthesis;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class CommandSubroutine extends SCBaseSkillPlugin {
    
	//Panopticon Interface 10% cr mitigation bonus handled by PanopticInterfaceUtil
	//Synthesis Core officer mechanics handled by hullmod AdaptiveTacticalCore
	@Override
    public String getAffectsString() {
        return "all ships in the fleet";
    }
	
    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("Panoptic Interface fleetwide readiness penalty reduced by 10%%", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("Adaptive Tactical Core upgraded to gamma-level AI performance in the absence of a human captain", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }
	
	//skill is never picked by npcs, bonuses handled by hullmods
	@Override
	public void onActivation(SCData data) {
		if (data.isPlayer()) Global.getSector().getMemoryWithoutUpdate().set("$xo_command_subroutine_is_active", true);
	}
	
	@Override
	public void onDeactivation(SCData data) {
		if (data.isPlayer()) Global.getSector().getMemoryWithoutUpdate().set("$xo_command_subroutine_is_active", false);
	}
}
