package DE.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import static com.fs.starfarer.api.impl.campaign.rulecmd.RemoveShip.addShipLossText;

import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * DE_HasSierraInFleet <guilt> for Inventor Raccoon :^)
 * I definitely did not rip this from Iron Shell(thanks tim)
 */

public class DE_HasSierraInFleet extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        if (Global.getSector().getPlayerFleet() == null) return false;
        if (!Global.getSettings().getModManager().isModEnabled("secretsofthefrontier")) return false;
        if (params.size() > 0) {
            Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("$fs_guilt", Global.getSector().getCharacterData().getMemoryWithoutUpdate().getFloat("$fs_guilt") + params.get(0).getFloat(memoryMap));
            for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
                if (member.getVariant().hasHullMod("sotf_sierrasconcord")) {
                    Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(member);
                    addShipLossText(member, dialog.getTextPanel());
                    return true;
                }
            }
        }
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
            if (member.getVariant().hasHullMod("sotf_sierrasconcord")) {
                return true;
            }
        }
        return false;
    }
}