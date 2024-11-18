package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;


import java.io.IOException;
import java.util.List;

@SuppressWarnings("unchecked")
public class SectorGenDerelict implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
        FactionAPI derelict = Global.getSector().getFaction(Factions.DERELICT);
        for (FactionAPI faction : Global.getSector().getAllFactions())
        {
            if (faction.getId().contains(Factions.DERELICT)) continue;
            faction.setRelationship(derelict.getId(), RepLevel.HOSTILE);
        }
		
	}
}