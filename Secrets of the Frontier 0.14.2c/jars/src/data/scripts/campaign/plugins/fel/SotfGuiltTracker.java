// handles guilt gain from atrocities
// Sierra's responses to such are handled in her conversation intel
package data.scripts.campaign.plugins.fel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import data.scripts.campaign.ids.SotfIDs;
import data.scripts.utils.SotfMisc;

import java.util.HashSet;
import java.util.Set;

public class SotfGuiltTracker extends BaseCampaignEventListener implements EveryFrameScript, ColonyPlayerHostileActListener {

    // so we know if Reality Breaker is OK to use
    public float timeSinceSave = 0f;

    public SotfGuiltTracker() {
        super(true);
    }

    public void advance(float amount) {
        timeSinceSave += amount;
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }

    // no guilt buildup for these ones - factions who don't really have civilians to satbomb
    public static final Set<String> GUILTY_FACTIONS = new HashSet<>();
    static {
        GUILTY_FACTIONS.add(SotfIDs.DUSTKEEPERS); // when the singularity happens, they're gonna kill me for this one
        GUILTY_FACTIONS.add("nex_derelict"); // Derelict Empire
        GUILTY_FACTIONS.add("fang"); // werewolves
        GUILTY_FACTIONS.add("draco"); // vampires
        GUILTY_FACTIONS.add("HIVER"); // bugs
        GUILTY_FACTIONS.add("enigma"); // ???
    }

    public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData, CargoAPI cargo) {
        //
    }
    public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData, Industry industry) {
        //
    }

    public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
        //
    }
    public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
        if (GUILTY_FACTIONS.contains(market.getFactionId())) {
            return;
        }
        float guiltToAdd = 1f;
        if (market.getSize() >= 4) {
            guiltToAdd++;
        }
        if (market.getSize() >= 6) {
            guiltToAdd++;
        }
        if (market.getSize() >= 8) {
            guiltToAdd++;
        }
        SotfMisc.addGuilt(guiltToAdd);
    }

}
