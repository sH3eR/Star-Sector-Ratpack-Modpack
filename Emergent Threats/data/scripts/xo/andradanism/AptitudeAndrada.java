package data.scripts.xo.andradanism;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

import second_in_command.SCData;
import second_in_command.specs.SCAptitudeSection;
import second_in_command.specs.SCBaseAptitudePlugin;

import lunalib.lunaSettings.LunaSettings;

public class AptitudeAndrada extends SCBaseAptitudePlugin {
	
	private static String IN_FAC_ID = "independent";
	private static String LG_FAC_ID = "lions_guard";
	private static String SD_FAC_ID = "sindrian_diktat";
	
    //The ID of the skill that is always active
    @Override
    public String getOriginSkillId() {
        return "xo_andrada_supreme_leadership";
    }
	
    //Determines which skills are added and how they are sectioned off in the UI
    @Override
    public void createSections() {
		
        SCAptitudeSection section1 = new SCAptitudeSection(true, 0, "leadership1");
		section1.addSkill("xo_andrada_battlefield_persistence");
		section1.addSkill("xo_andrada_forward_to_victory");
		section1.addSkill("xo_andrada_priority_requisition");
		section1.addSkill("xo_andrada_unbreakable_defense");
		section1.addSkill("xo_andrada_unstoppable_offense");
        addSection(section1);

        SCAptitudeSection section2 = new SCAptitudeSection(false, 3, "leadership2");
		
		section2.addSkill("xo_andrada_doctrinal_purity");
		section2.addSkill("xo_andrada_energy_focus_mastery");
        addSection(section2);
		
        SCAptitudeSection section3 = new SCAptitudeSection(false, 4, "leadership5");
		section3.addSkill("xo_andrada_mass_mobilization");
		section3.addSkill("xo_andrada_unwavering_conviction");
        addSection(section3);
    }

    public Float getMarketSpawnweight(MarketAPI market){
        float weight = spec.getSpawnWeight();
		String id = "";
		if (market.getFaction() != null) id = market.getFaction().getId();
		
        if (SD_FAC_ID.equals(id)) {
			if (market.hasIndustry("lionsguard")) weight = 3f;
			else weight *= 2f;
		}
        else if (IN_FAC_ID.equals(id)) weight *= 0.5f;
		else weight = 0f;
        return weight;
    }

    @Override
    public Float getNPCFleetSpawnWeight(SCData data, CampaignFleetAPI fleet) {
		String id = "";
		boolean isAndradaEnabled = LunaSettings.getBoolean("EmergentThreats_Vice", "vice_andradaEnabled");
		if (fleet.getFaction() != null) id = fleet.getFaction().getId();
        if (SD_FAC_ID.equals(id) || LG_FAC_ID.equals(id)) return isAndradaEnabled ? Float.MAX_VALUE : 0f;
		if ("vice_diktat_navy".equals(id) || "vice_lions_guard".equals(id)) return Float.MAX_VALUE; //hvb boss
        return 0f;
    }
}