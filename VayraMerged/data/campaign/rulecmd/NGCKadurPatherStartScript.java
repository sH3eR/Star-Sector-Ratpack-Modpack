package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.domain.PersonBountyEventDataRepository;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Vayra
 */
public class NGCKadurPatherStartScript extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
        //final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);

        data.addScript(new Script() {
            @Override
            public void run() {

                //SectorAPI sector = Global.getSector();

                PersonBountyEventDataRepository.getInstance().addParticipatingFaction(Factions.LUDDIC_PATH);

                String parentId = Factions.LUDDIC_PATH;
                FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
                FactionAPI parent = Global.getSector().getFaction(parentId);
                for (FactionAPI other : Global.getSector().getAllFactions()) {
                    player.setRelationship(other.getId(), parent.getRelationship(other.getId()));
                }
                player.setRelationship(parentId, RepLevel.WELCOMING);

                CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                CargoAPI cargo = fleet.getCargo();
                cargo.initPartialsIfNeeded();
                cargo.addCommodity(Commodities.HAND_WEAPONS, 50);

            }

        });
        return true;
    }

}
