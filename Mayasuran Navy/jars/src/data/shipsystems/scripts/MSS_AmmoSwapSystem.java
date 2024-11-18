package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import data.hullmods.MSS_AmmoSwapHullmod.AmmoSwapMode;

public class MSS_AmmoSwapSystem extends BaseShipSystemScript {
    int mode = 1;
    boolean firstFrame = true;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel)
    {
        if (stats.getEntity() instanceof ShipAPI && firstFrame)
        {
            firstFrame = false;
            ShipAPI ship = (ShipAPI) stats.getEntity();
            mode++;
            if (mode >= 4)
                mode = 1;
            ship.getSystem().setAmmo(mode);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id)
    {
        firstFrame = true;
        if (stats.getEntity() instanceof ShipAPI)
        {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            if (ship.getSystem().getAmmo() != mode)
            {
                ship.getSystem().setAmmo(mode);
            }
        }
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship)
    {
        switch (system.getAmmo())
        {
            case 1:
                return AmmoSwapMode.KINETIC.name();
            case 2:
                return AmmoSwapMode.HE.name();
            case 3:
                return AmmoSwapMode.CLUSTER.name();
            default:
                return "bruh";
        }
    }
}
