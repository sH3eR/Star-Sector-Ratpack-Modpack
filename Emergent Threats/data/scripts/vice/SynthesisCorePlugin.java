package data.scripts.vice;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.BaseAICoreOfficerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class SynthesisCorePlugin extends BaseAICoreOfficerPluginImpl implements AICoreOfficerPlugin {
	
	@Override
	public PersonAPI createPerson(String aiCoreId, String factionId, Random random) {
		if (random == null) random = new Random();
		PersonAPI person = Global.getFactory().createPerson();
		person.setFaction(factionId);
		person.setAICoreId(aiCoreId);
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(aiCoreId);
		person.getStats().setSkipRefresh(true);
		float mult = 0f;
		person.setName(new FullName("Command Subroutine", "", Gender.ANY));
		person.setPortraitSprite("graphics/vice/portraits/xo_command_subroutine.png");
		person.getStats().setLevel(3);
		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
		person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
		person.setRankId(Ranks.SPACE_LIEUTENANT);
		//person.getMemoryWithoutUpdate().set("$autoPointsMult", mult);
		person.setPersonality(Personalities.RECKLESS);
		person.setPostId(null);
		person.getStats().setSkipRefresh(false);
        return person;
	}
}