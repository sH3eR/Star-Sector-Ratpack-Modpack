package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
public class istl_NodalPhaseCloak extends BaseHullMod {

        private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);
	public static final float DEGRADE_INCREASE_PERCENT = 50f;
        
            static
        {
        // These hullmods will automatically be removed
        // Not as elegant as blocking them in the first place, but
        // this method doesn't require editing every hullmod's script
        BLOCKED_HULLMODS.add("convertedhangar"); // No fighters!
        BLOCKED_HULLMODS.add("roider_fighterClamps"); // We mean it!
        }
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSensorProfile().modifyMult(id, 0f);
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);
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
		if (index == 0) return "" + (int) DEGRADE_INCREASE_PERCENT + "%";
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null && (ship.getHullSpec().getNoCRLossTime() < 10000 || ship.getHullSpec().getCRLossPerSecond() > 0); 
	}
}
