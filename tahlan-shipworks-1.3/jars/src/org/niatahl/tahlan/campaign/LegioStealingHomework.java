//code by Vayra, kudos

package org.niatahl.tahlan.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.IntervalUtil;
import org.apache.log4j.Logger;

public class LegioStealingHomework implements EveryFrameScript {

    public static Logger log = Global.getLogger(LegioStealingHomework.class);

    public static final String LEGIO_ID = "tahlan_legioinfernalis";

    // only check every couple days
    private final IntervalUtil timer = new IntervalUtil(4f,4f);
    private IntervalUtil t;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {

        float days = Global.getSector().getClock().convertToDays(amount);
        timer.advance(days);
        if (timer.intervalElapsed()) {
            log.info("Interval elapsed, the space fascists gonna learn today");
            stealPirateBlueprints();
        }
    }

    public void stealPirateBlueprints() {

        for (String weapon : Global.getSector().getFaction(Factions.PIRATES).getKnownWeapons()) {
            if (!Global.getSector().getFaction(LEGIO_ID).knowsWeapon(weapon)) {
                Global.getSector().getFaction(LEGIO_ID).addKnownWeapon(weapon, true);
            }
        }

        // Copy all ships except for LTA garbage
        for (String ship : Global.getSector().getFaction(Factions.PIRATES).getKnownShips()) {
            if (!Global.getSector().getFaction(LEGIO_ID).knowsShip(ship) && !ship.startsWith("LTA_")) {
                Global.getSector().getFaction(LEGIO_ID).addKnownShip(ship, true);
            }
        } 
        
        for (String baseShip : Global.getSector().getFaction(Factions.PIRATES).getAlwaysKnownShips()) {
            if (!Global.getSector().getFaction(LEGIO_ID).useWhenImportingShip(baseShip)) {
                Global.getSector().getFaction(LEGIO_ID).addUseWhenImportingShip(baseShip);
            }
        }

        for (String fighter : Global.getSector().getFaction(Factions.PIRATES).getKnownFighters()) {
            if (!Global.getSector().getFaction(LEGIO_ID).knowsFighter(fighter)) {
                Global.getSector().getFaction(LEGIO_ID).addKnownFighter(fighter, true);
            }
        }

        // Now copy everything over to the Elite faction
        FactionAPI elites = Global.getSector().getFaction("tahlan_legioelite");
        if (elites==null) {
            return;
        }

        for (String weapon : Global.getSector().getFaction(LEGIO_ID).getKnownWeapons()) {
            if (!elites.knowsWeapon(weapon)) {
                elites.addKnownWeapon(weapon, true);
            }
        }

        // Copy all ships except for LTA garbage
        for (String ship : Global.getSector().getFaction(LEGIO_ID).getKnownShips()) {
            if (!elites.knowsShip(ship) && !ship.startsWith("LTA_")) {
                elites.addKnownShip(ship, true);
            }
        }

        for (String baseShip : Global.getSector().getFaction(LEGIO_ID).getAlwaysKnownShips()) {
            if (!elites.useWhenImportingShip(baseShip)) {
                elites.addUseWhenImportingShip(baseShip);
            }
        }

        for (String fighter : Global.getSector().getFaction(LEGIO_ID).getKnownFighters()) {
            if (!elites.knowsFighter(fighter)) {
                elites.addKnownFighter(fighter, true);
            }
        }
    }
}
