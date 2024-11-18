package DE.scripts.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;

// Thanks WMGreywind
public class DE_OmegaListenerFleet extends BaseCampaignEventListener implements FleetEventListener {

    public DE_OmegaListenerFleet() {
        super(false);
    }

    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (!battle.isPlayerInvolved()) {
            return;
        }
        if (battle.isPlayerInvolved()) {
            MemoryAPI sector_mem = Global.getSector().getMemoryWithoutUpdate();
            for (CampaignFleetAPI otherFleet : battle.getNonPlayerSideSnapshot()) {
                for (FleetMemberAPI member : Misc.getSnapshotMembersLost(otherFleet)) {
                    if (member.getHullId().equals("tesseract")) {
                        sector_mem.set("$de_FoughtOmega", true);
                    }
                    if (member.getHullId().equals("facet")) {
                        sector_mem.set("$de_FoughtOmega", true);
                    }
                    if (member.getHullId().equals("shard_left")) {
                        sector_mem.set("$de_FoughtOmega", true);
                    }
                    if (member.getHullId().equals("shard_right")) {
                        sector_mem.set("$de_FoughtOmega", true);
                    }
                }
            }
        }
    }

    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
    }
}