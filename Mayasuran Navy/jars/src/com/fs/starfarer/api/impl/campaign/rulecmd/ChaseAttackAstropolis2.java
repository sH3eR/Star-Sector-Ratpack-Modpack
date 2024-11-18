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


public class ChaseAttackAstropolis2 extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (Global.getSettings().getModManager().isModEnabled("nexerelin")) {
			DiplomacyManager.createDiplomacyEvent(Global.getSector().getFaction("mayasura"), Global.getSector().getEconomy().getMarket("port_tse").getFaction(), "declare_war", null);
            InvasionIntel intel = new InvasionIntel(Global.getSector().getFaction("mayasura"), Global.getSector().getEconomy().getMarket("mairaath"), Global.getSector().getEntityById("port_tse").getMarket(), InvasionFleetManager.getWantedFleetSize(Global.getSector().getFaction("mayasura"), Global.getSector().getEntityById("port_tse").getMarket(), 0.2f, false), 1);
            intel.init();
            Global.getSector().getIntelManager().addIntelToTextPanel(intel, dialog.getTextPanel());
        }
        return true;
    }

}