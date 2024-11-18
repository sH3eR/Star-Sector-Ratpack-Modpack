package data.scripts.ix.backgrounds;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import exerelin.campaign.backgrounds.BaseCharacterBackground;
import exerelin.utilities.NexFactionConfig;

public class ColonialOverseerBackground extends BaseCharacterBackground {
	
	private static String IX_FACTION_ID = "ix_battlegroup";
	private static String IX_TRINITY_ID = "ix_trinity";
	private static String IX_MARZANNA_ID = "ix_marzanna";
	private static String IX_ADMIN_SKILL_ID = "ix_ai_assisted_command";
	private static String IX_SKILL_NAME = "AI Assisted Command";
	private static String INSTANCE_NAME = "Panopticon Instance";
	private static String STRUCTURE_NAME = "Fleet Embassy";
	private static String IND_PLANNING_NAME = "Industrial Planning";
	private static String IND_PLANNING_ID = "industrial_planning";
	private static String IND_PLANNING_IX = "ix_industrial_planning";
	
	public boolean isSecComInstalled () {
		return (Global.getSettings().getModManager().isModEnabled("second_in_command"));
	}

	@Override
	public boolean shouldShowInSelection(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
		if (factionConfig.factionId.equals("player")) return true;
		return (factionConfig.factionId.equals(IX_FACTION_ID) || factionConfig.factionId.equals(IX_TRINITY_ID));
	}
	
	@Override
	public void onNewGameAfterTimePass(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
		PersonAPI player = Global.getSector().getPlayerPerson();
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		cargo.addCommodity("ix_panopticon_instance", 1);
		player.getStats().setSkillLevel(IX_ADMIN_SKILL_ID, 1f);
		if (isSecComInstalled()) {
			if (!player.getStats().hasSkill(IND_PLANNING_ID)) player.getStats().setSkillLevel(IND_PLANNING_IX, 1f);
		}
		
		String fac = Misc.getCommissionFactionId();
		if (!IX_FACTION_ID.equals(fac) && !IX_TRINITY_ID.equals(fac)) {
			Global.getSector().getFaction(IX_FACTION_ID).setRelationship("player", 0.50f);
			Global.getSector().getFaction(IX_TRINITY_ID).setRelationship("player", 0.50f);
			Global.getSector().getFaction(IX_MARZANNA_ID).setRelationship("player", 0.50f);
		}
		Global.getSector().getMemoryWithoutUpdate().set("$can_build_embassy", true);
	}
	
	@Override
	public void addTooltipForSelection(TooltipMakerAPI tooltip, FactionSpecAPI factionSpec, NexFactionConfig factionConfig, Boolean expanded) {
		super.addTooltipForSelection(tooltip, factionSpec, factionConfig, expanded);

		if (expanded) {
			String s = "Gain the %s colony skill and a %s AI core, you start with good relations with the IX Battlegroup and Trinity Worlds, and can build a %s immediately.";
            tooltip.addSpacer(10f);
            tooltip.addPara(s, 0f, Misc.getTextColor(), Misc.getHighlightColor(), IX_SKILL_NAME, INSTANCE_NAME, STRUCTURE_NAME);
			if (isSecComInstalled()) {
				s = "(Second In Command) Also gain the %s skill.";
				tooltip.addSpacer(10f);
				tooltip.addPara(s, 0f, Misc.getTextColor(), Misc.getHighlightColor(), IND_PLANNING_NAME);
			}
		}
	}
}