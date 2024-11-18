package com.fs.starfarer.api.impl.campaign.rulecmd;


import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc.Token;
import exerelin.campaign.fleets.InvasionFleetManager;
import exerelin.campaign.intel.invasion.InvasionIntel;
import exerelin.campaign.DiplomacyManager;


public class ChaseAttackAstropolis extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (Global.getSettings().getModManager().isModEnabled("nexerelin")) {
			DiplomacyManager.createDiplomacyEvent(Global.getSector().getFaction("mayasura"), Global.getSector().getEconomy().getMarket("mairaath_abandoned_station2").getFaction(), "declare_war", null);
            InvasionIntel intel = new InvasionIntel(Global.getSector().getFaction("mayasura"), Global.getSector().getEconomy().getMarket("mairaath"), Global.getSector().getEntityById("mairaath_abandoned_station2").getMarket(), InvasionFleetManager.getWantedFleetSize(Global.getSector().getFaction("mayasura"), Global.getSector().getEntityById("mairaath_abandoned_station2").getMarket(), 0.2f, false), 1);
            intel.init();
            Global.getSector().getIntelManager().addIntelToTextPanel(intel, dialog.getTextPanel());
        }
        return true;
    }

}
