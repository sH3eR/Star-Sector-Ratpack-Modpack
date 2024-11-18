package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class ChaseAstropolis2 extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
            return (Global.getSector().getEntityById("port_tse") != null && !Global.getSector().getEntityById("port_tse").getMarket().getFactionId().equals(Factions.PLAYER) && !Global.getSector().getEntityById("port_tse").getMarket().getFactionId().equals("mayasura") && Global.getSettings().getModManager().isModEnabled("nexerelin"));
        }
}