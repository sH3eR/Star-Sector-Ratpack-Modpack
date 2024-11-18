package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class MSS_LastStand extends BaseHullMod {
    public static final float CR_ACTIVATION_LIMIT = 31f;            //Below (or equal to) this amount of CR, all effects are applied
    public static final float TIME_DILATION_EFFECT = 0.75f;         //The actual time dilation: 0.75 means 75% of original speed
    public static final float REPAIR_BONUS_MULT = 2.0f;          //Actually inverted: 2 means twice as fast
    public static final String EFFECT_ID = "MSS_LAST_STAND_ID";  //Just a unique ID for the effect: any effect with the same ID will override this one, so keep it unique.

    //The code has to run every frame to work properly.
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        //No running without a ship!
        if (ship == null) {
            return;
        }

        //Saves some values for later use
        MutableShipStatsAPI stats = ship.getMutableStats();
        String trueID = EFFECT_ID + ship.getId();

        //Meat of the code: applies bonuses, and displays a small icon if we are the player ship
        if (ship.getCurrentCR() <= CR_ACTIVATION_LIMIT / 100f && !ship.isHulk()) {
            stats.getCombatWeaponRepairTimeMult().modifyMult(trueID, 1 / REPAIR_BONUS_MULT);
            stats.getCombatEngineRepairTimeMult().modifyMult(trueID, 1 / REPAIR_BONUS_MULT);

            //Only change perception of time and show icon if the player is piloting the ship
            if (ship == Global.getCombatEngine().getPlayerShip()) {
                Global.getCombatEngine().getTimeMult().modifyMult(trueID, TIME_DILATION_EFFECT);

                //Change "graphics/icons/hullsys/flare_launcher.png" to the path to your icon
                Global.getCombatEngine().maintainStatusForPlayerShip(trueID, "graphics/mayasura/hullmods/laststand.png", "Heroic Last Stand", "No surrender, no remorse: they will not break us!", false);
            } else {
                Global.getCombatEngine().getTimeMult().unmodify(trueID);
            }
        } else {
            stats.getCombatWeaponRepairTimeMult().unmodify(trueID);
            stats.getCombatEngineRepairTimeMult().unmodify(trueID);
            Global.getCombatEngine().getTimeMult().unmodify(trueID);
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        //This should obviously be replaced with whatever applying condition you want
        boolean canIt = true;
        return canIt;
    }
}
