package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.campaign.fleets.HMIScavFleetRouteManager;
import data.scripts.world.systems.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;

import java.io.IOException;
import java.util.List;

import static com.fs.starfarer.api.impl.campaign.ids.Tags.THEME_RUINS;
import static com.fs.starfarer.api.impl.campaign.ids.Tags.THEME_RUINS_MAIN;

@SuppressWarnings("unchecked")
    public class HMI_procgen implements SectorGeneratorPlugin {



    @Override
    public void generate(SectorAPI sector) {
        new HMI_mansa().generate(sector);
        new HMI_seele().generate(sector);

        FactionAPI mess_remnant = sector.getFaction("mess_remnant");
        FactionAPI hmi_nightmare = sector.getFaction("hmi_nightmare");
        FactionAPI hmi_maxwell = sector.getFaction("hmi_maxwell");


        List<FactionAPI> allFactions = sector.getAllFactions();

        for (FactionAPI curFaction : allFactions) {
            if (curFaction == mess_remnant || curFaction.isNeutralFaction()) {
                continue;
            }
            mess_remnant.setRelationship(curFaction.getId(), RepLevel.VENGEFUL);
        }

        for (FactionAPI curFaction : allFactions) {
            if (curFaction == hmi_nightmare || curFaction.isNeutralFaction()) {
                continue;
            }
            hmi_nightmare.setRelationship(curFaction.getId(), RepLevel.VENGEFUL);
        }


        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
        FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
        FactionAPI pirates = sector.getFaction(Factions.PIRATES);
        FactionAPI independent = sector.getFaction(Factions.INDEPENDENT);
        FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
        FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
        FactionAPI kol = sector.getFaction(Factions.KOL);
        FactionAPI diktat = sector.getFaction(Factions.DIKTAT);
        FactionAPI persean = sector.getFaction(Factions.PERSEAN);
        FactionAPI remnant = sector.getFaction(Factions.REMNANTS);

        hmi_maxwell.setRelationship("player_npc", RepLevel.NEUTRAL);
        hmi_maxwell.setRelationship(Factions.PLAYER, RepLevel.NEUTRAL);
        hmi_maxwell.setRelationship(hegemony.getId(), -0.3f);
        hmi_maxwell.setRelationship(tritachyon.getId(), 0.2f);
        hmi_maxwell.setRelationship(pirates.getId(), -0.7f);
        hmi_maxwell.setRelationship(independent.getId(), 0.0f);
        hmi_maxwell.setRelationship(persean.getId(), 0.0f);
        hmi_maxwell.setRelationship(church.getId(), -0.3f);
        hmi_maxwell.setRelationship(path.getId(), -1.0f);
        hmi_maxwell.setRelationship(kol.getId(), -0.5f);
        hmi_maxwell.setRelationship(diktat.getId(), 0f);
        hmi_maxwell.setRelationship(remnant.getId(), 0.2f);
        hmi_maxwell.setRelationship("interstellarimperium", 0.2f);
        hmi_maxwell.setRelationship("knights_of_selkie", -1.0f);
    }
}

