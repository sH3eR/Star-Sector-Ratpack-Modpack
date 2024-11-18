package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import java.util.HashSet;
import java.util.Set;

public class istl_AdvNavRelay extends BaseHullMod {

	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 5f);
		mag.put(HullSize.DESTROYER, 10f);
		mag.put(HullSize.CRUISER, 10f);
		mag.put(HullSize.CAPITAL_SHIP, 15f);
	}
        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

        static
        {
            // These hullmods will automatically be removed
            // Not as elegant as blocking them in the first place, but
            // this method doesn't require editing every hullmod's script
            BLOCKED_HULLMODS.add("navrelay"); // No stacking, fucko!
        }
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_FLAT).modifyFlat(id, (Float) mag.get(hullSize));
	}
	
            @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
        {
            for (String tmp : BLOCKED_HULLMODS)
            {
                if (ship.getVariant().getHullMods().contains(tmp))
                {
                    ship.getVariant().removeMod(tmp);
                    DMEBlockedHullmodDisplayScript.showBlocked(ship);
                }
            }
        }
        
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue() + "%";
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue() + "%";
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue() + "%";
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
		return null;
	}
}




