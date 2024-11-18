package org.niatahl.tahlan.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;
import java.util.EnumSet;

import static org.niatahl.tahlan.utils.Utils.txt;

public class FluxFeederStats extends BaseShipSystemScript {

    private static final float WEAPON_BOOST = 0.5f;
    private static final Color GLOW_COLOR = new Color(255, 120, 16);
    private static EnumSet<WeaponAPI.WeaponType> WEAPON_TYPES = EnumSet.of(WeaponAPI.WeaponType.BALLISTIC);

    private float power = 0f;
    private boolean runOnce = true;


    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        if (runOnce) {
            power = Math.min((ship.getFluxLevel() / 0.90f), 1f);
            runOnce = false;
        }
        float actualBoost = 1.5f+WEAPON_BOOST*power*effectLevel;

        stats.getBallisticRoFMult().modifyMult(id,actualBoost);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id,1f/actualBoost);

        ship.setWeaponGlow(((0.7f+0.3f*power)*effectLevel), GLOW_COLOR, WEAPON_TYPES);

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        stats.getBallisticRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        //ship.setWeaponGlow(0f, GLOW_COLOR, WEAPON_TYPES);
        runOnce = true;

    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if ( index == 0 ) {
            return new StatusData(txt("sys_FluxFeeder") + (int)((0.5+WEAPON_BOOST*power*effectLevel)*100f) + "%", false);
        }
        return null;
    }

}
