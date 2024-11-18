package data;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import data.scripts.MothershipFleetManager;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;


import java.util.List;

public class DDPlusPlugin extends BaseModPlugin {


    @Override
    public void onNewGameAfterTimePass() {
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            for (SectorEntityToken entity : location.getAllEntities()) {
                if (entity instanceof CustomCampaignEntityAPI && entity.getCustomEntityType().equals("derelict_mothership")) {
                    MothershipFleetManager manager = new MothershipFleetManager(entity, 5f, 4, 6, 15f, 5, 20);
                    entity.getStarSystem().addScript(manager);
                }
//                if (entity instanceof CustomCampaignEntityAPI && entity.getCustomEntityType().equals("derelict_probe")) {
//                    MothershipFleetManager manager = new MothershipFleetManager(entity, 5F, 0, 0, 30.0F, 1, 5);
//                    entity.getStarSystem().addScript(manager);
//                }
//                if (entity instanceof CustomCampaignEntityAPI && entity.getCustomEntityType().equals("derelict_survey_ship")) {
//                    MothershipFleetManager manager = new MothershipFleetManager(entity, 5F, 2, 3, 30.0F, 2, 6);
//                    entity.getStarSystem().addScript(manager);
//                }
            }
        }
    }
}
