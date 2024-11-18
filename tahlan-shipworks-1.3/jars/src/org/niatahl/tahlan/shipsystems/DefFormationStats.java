package org.niatahl.tahlan.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.niatahl.tahlan.utils.Utils.txt;

public class DefFormationStats extends BaseShipSystemScript {

    private static final float DAMAGE_TAKEN_MULT = 0.5f;
    private static final Object KEY_JITTER = new Object();
    private static final Color JITTER_UNDER_COLOR = new Color(255, 150, 0, 125);
    private static final Color JITTER_COLOR = new Color(255, 150, 0, 75);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }


        if (effectLevel > 0) {
            float jitterRangeBonus = effectLevel * 5f;
            for (ShipAPI fighter : getFighters(ship)) {
                if (fighter.isHulk()) continue;
                MutableShipStatsAPI fStats = fighter.getMutableStats();

                fStats.getShieldDamageTakenMult().modifyMult(id,DAMAGE_TAKEN_MULT);
                fStats.getArmorDamageTakenMult().modifyMult(id,DAMAGE_TAKEN_MULT);
                fStats.getHullDamageTakenMult().modifyMult(id,DAMAGE_TAKEN_MULT);

                fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, effectLevel, 5, 0f, jitterRangeBonus);
                fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, effectLevel, 2, 0f, 1f + jitterRangeBonus);
                Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());

            }
        }
    }

    private List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<ShipAPI>();

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) continue;
            if (ship.getWing() == null) continue;
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) continue;
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            fStats.getShieldDamageTakenMult().unmodify(id);
            fStats.getArmorDamageTakenMult().unmodify(id);
            fStats.getHullDamageTakenMult().unmodify(id);
        }

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(txt("defFormation"), false);
        }
        return null;
    }
}
