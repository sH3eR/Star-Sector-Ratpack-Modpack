package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class cum_ds extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
                if (params.isEmpty()) {final PersonAPI person = OfficerManagerEvent.createMercInternal(Global.getSector().getFaction(Factions.HEGEMONY), Global.getSettings().getInt("officerMercMaxLevel") < 4 ? 1 : Global.getSettings().getInt("officerMercMaxLevel")-3, 0, false, new Random());
                person.setPersonality(Personalities.AGGRESSIVE);
                person.getName().setLast("Rao");
                MutableCharacterStatsAPI stats = person.getStats();
                if (stats.getSkillLevel(Skills.BALLISTIC_MASTERY) > 0) {stats.setSkillLevel(Skills.BALLISTIC_MASTERY, 2);} else {stats.setSkillLevel(Skills.BALLISTIC_MASTERY, 2);person.getStats().setLevel(person.getStats().getLevel()+1);}
                if (stats.getSkillLevel(Skills.ORDNANCE_EXPERTISE) > 0) {stats.setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);} else {stats.setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);person.getStats().setLevel(person.getStats().getLevel()+1);}
                Misc.setMercenary(person, true);
		TextPanelAPI text = dialog.getTextPanel();
		Color hl = Misc.getHighlightColor();
		text.addSkillPanel(person, false);
		text.setFontSmallInsignia();
		String personality = Misc.lcFirst(person.getPersonalityAPI().getDisplayName());
		text.addParagraph("Personality: " + personality + ", level: " + stats.getLevel());
		text.highlightInLastPara(hl, personality, "" + stats.getLevel());
		text.addParagraph(person.getPersonalityAPI().getDescription());
		text.setFontInsignia();
                dialog.getVisualPanel().showSecondPerson(person);
                
                Global.getSector().getMemoryWithoutUpdate().set("$cum_ds_officer", person, 0);
                Global.getSector().getMemoryWithoutUpdate().set("$cum_ds_officer_id", person.getId(), 0);
                Global.getSector().getMemoryWithoutUpdate().set("$cum_ds_mercContractDurStr", Global.getSettings().getInt("officerMercContractDur"), 0);
                Global.getSector().getMemoryWithoutUpdate().set("$cum_ds_salary", Misc.getWithDGS((int) Misc.getOfficerSalary(person)), 0);}
                else {
                    dialog.getInteractionTarget().getActivePerson().getMemoryWithoutUpdate().set("$cum_ds_checkout", true, Global.getSettings().getInt("officerMercContractDur"));
                    PersonAPI officer = (PersonAPI) Global.getSector().getMemoryWithoutUpdate().get("$cum_ds_officer");
                    officer.setPostId(Ranks.POST_OFFICER);
                    officer.getMemoryWithoutUpdate().set("$cum_rao", true);
                    Global.getSector().getPlayerFleet().getFleetData().addOfficer(officer);
                    Misc.setMercHiredNow(officer);
                    AddRemoveCommodity.addOfficerGainText(officer, dialog.getTextPanel());
                }
                return true;
	}
        
}

