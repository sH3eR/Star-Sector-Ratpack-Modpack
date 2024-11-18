package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class ChaseAstropolis extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
            return (Global.getSector().getEntityById("mairaath_abandoned_station2") != null && !Global.getSector().getEntityById("mairaath_abandoned_station2").getMarket().getFactionId().equals(Factions.PLAYER) && !Global.getSector().getEntityById("mairaath_abandoned_station2").getMarket().getFactionId().equals("mayasura") && Global.getSettings().getModManager().isModEnabled("nexerelin"));
        }
}