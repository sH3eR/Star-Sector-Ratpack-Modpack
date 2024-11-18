package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;

import java.util.HashMap;
import java.util.Map;

public class MSS_SkysplitterM_system extends BaseHullMod {

    private final Map<String, Integer> SWITCH_SYSTEM_TO = new HashMap<>();

    {
        SWITCH_SYSTEM_TO.put("MSS_Skyrend_Elite", 1);
        SWITCH_SYSTEM_TO.put("MSS_Skyrend_EliteB", 2);
        SWITCH_SYSTEM_TO.put("MSS_Skyrend_EliteC", 3);
        SWITCH_SYSTEM_TO.put("MSS_Skyrend_EliteD", 0);
    }

    private final Map<Integer, String> SWITCH_SYSTEM = new HashMap<>();

    {
        SWITCH_SYSTEM.put(0, "MSS_Skyrend_Elite");
        SWITCH_SYSTEM.put(1, "MSS_Skyrend_EliteB");
        SWITCH_SYSTEM.put(2, "MSS_Skyrend_EliteC");
        SWITCH_SYSTEM.put(3, "MSS_Skyrend_EliteD");
    }

	    // map containing the hullmod that corresponds to each weapon option
    private final Map<Integer,String> SYSTEMSWITCH = new HashMap<>();
    {
        SYSTEMSWITCH.put(0,"MSS_selector_SkyrendA");
        SYSTEMSWITCH.put(1,"MSS_selector_SkyrendB");
        SYSTEMSWITCH.put(2,"MSS_selector_SkyrendC");
        SYSTEMSWITCH.put(3,"MSS_selector_SkyrendD");
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        // trigger a system switch if none of the selector hullmods are present
        boolean switchSystem = true;
        for (String hullmod : SWITCH_SYSTEM_TO.keySet()) {
            // get the name of the corresponding selector hullmod
            String selectorHullmod = SYSTEMSWITCH.get(SWITCH_SYSTEM_TO.get(hullmod));
            // check if the ship has that hullmod
            if (stats.getVariant().getHullMods().contains(selectorHullmod)) {
                switchSystem = false;
                break;
            }
        }

        // swap the source variant and add the proper hullmod
        if (switchSystem && stats.getEntity() != null && ((ShipAPI) stats.getEntity()).getHullSpec() != null) {
            // get the ID of the hullspec to switch to
            String hullId = ((ShipAPI) stats.getEntity()).getHullSpec().getHullId();
            if (SWITCH_SYSTEM_TO.containsKey(hullId)) {
                int switchTo = SWITCH_SYSTEM_TO.get(hullId);
                // get the hullspec to switch to
                ShipHullSpecAPI ship = Global.getSettings().getHullSpec(SWITCH_SYSTEM.get(switchTo));
                // set the hullspec of the ship's variant to the new one
                ((ShipAPI) stats.getEntity()).getVariant().setHullSpecAPI(ship);
                // set the hullspec of the mutable ship stats to the new one
                stats.getVariant().setHullSpecAPI(ship);
                // add the proper hullmod to the variant
                stats.getVariant().addMod(SYSTEMSWITCH.get(switchTo));
            } else {
                // handle the case where the hullId is not in the SWITCH_SYSTEM_TO map
                // for example, you can log the issue or throw an exception with a custom error message
                Global.getLogger(this.getClass()).warn("Hull ID not found in SWITCH_SYSTEM_TO map: " + hullId);
            }
        }
    }
        @Override
        public String getDescriptionParam(int index, HullSize hullSize) {
            if (index == 0) return "A";
            if (index == 1) return "B";
            if (index == 2) return "C";
            if (index == 3) return "D";
            return null;
        }
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ( ship.getHullSpec().getHullId().startsWith("MSS_"));
    }
}


