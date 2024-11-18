package data.scripts.ix.listeners;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;

import lunalib.lunaSettings.LunaSettings;

//handles adding/removing AI Assisted Command to IX/Marzanna/Trinity colony admins
public class IXAdminEasyModeListener extends BaseCampaignEventListener {
	
	private static String IX_ADMIN_SKILL_ID = "ix_ai_assisted_command";
	private static String INDUSTRY_SKILL_ID = "industrial_planning";
	private static String IX_FAC_ID = "ix_battlegroup";
	private static String MZ_FAC_ID = "ix_marzanna";
	private static String TW_FAC_ID = "ix_trinity";
	private static String IX_IND_PLANNING = "ix_industrial_planning";
	
	public IXAdminEasyModeListener() {
		super(true);
	}	
	
	@Override
	public void reportPlayerOpenedMarket(MarketAPI market) {
		PersonAPI player = Global.getSector().getPlayerPerson();
		
		//adds copy of Industrial Planning to player that already took background, can be deleted after v0.7.5
		if (Global.getSettings().getModManager().isModEnabled("second_in_command")) {
			if (!player.getStats().hasSkill(INDUSTRY_SKILL_ID)
						&& player.getStats().hasSkill(IX_ADMIN_SKILL_ID)) {
				player.getStats().setSkillLevel(IX_IND_PLANNING, 1f);
			}
		}
		if (player.getStats().hasSkill(INDUSTRY_SKILL_ID)) player.getStats().setSkillLevel(IX_IND_PLANNING, 0f);
		
		List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
		List<PersonAPI> people = new ArrayList<PersonAPI>();
		for (MarketAPI market : markets) {
			if (market == null || market.getFactionId() == null) continue;
			String facId = market.getFactionId();
			if (facId.equals(IX_FAC_ID) || facId.equals(MZ_FAC_ID) || facId.equals(TW_FAC_ID)) {
				if (market.getAdmin() != null && !market.getAdmin().isAICore()) people.add(market.getAdmin());
			}
			
			//clear skill from admins of conquered former ix/tw colonies
			else if (market.getAdmin() != null && market.getAdmin() != player) {
				PersonAPI p = market.getAdmin();
				if (p.getStats().hasSkill(IX_ADMIN_SKILL_ID) && p.getStats().hasSkill(INDUSTRY_SKILL_ID)) {
					p.getStats().setSkillLevel(IX_ADMIN_SKILL_ID, 0f);
					p.getStats().refreshGovernedOutpostEffects(p.getMarket());
				}
			}
		}
		
		people.remove(player);
		
		if (people.size() == 0) return;
		
		//give AI Assisted Command skill to all IX/Marzanna/Trinity colony admins on normal mode
		if (!("Easy").equals(LunaSettings.getString("EmergentThreats_IX_Revival", "ix_difficulty_setting"))) {
			for (PersonAPI p : people) {
				if (!p.getStats().hasSkill(IX_ADMIN_SKILL_ID)) {
					p.getStats().setSkillLevel(IX_ADMIN_SKILL_ID, 1f);
					p.getStats().refreshGovernedOutpostEffects(p.getMarket());
				}
			}
		}
		
		//remove AI Assisted Command skill from all admins except player and Caelum Station admin
		else {
			for (PersonAPI p : people) {
				if (p.getStats().hasSkill(IX_ADMIN_SKILL_ID) && p.getStats().hasSkill(INDUSTRY_SKILL_ID)) {
					p.getStats().setSkillLevel(IX_ADMIN_SKILL_ID, 0f);
					p.getStats().refreshGovernedOutpostEffects(p.getMarket());
				}
			}
		}
	}
}