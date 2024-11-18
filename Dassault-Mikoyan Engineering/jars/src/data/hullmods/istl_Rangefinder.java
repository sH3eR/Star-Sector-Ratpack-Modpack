package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;

public class istl_Rangefinder extends BaseHullMod
{
    private static Map mag = new HashMap(); //
    static {
	mag.put(HullSize.FIGHTER, 0f);
	mag.put(HullSize.FRIGATE, 0f);
	mag.put(HullSize.DESTROYER, 100f);
	mag.put(HullSize.CRUISER, 200f);
	mag.put(HullSize.CAPITAL_SHIP, 200f);
    }
    
    public static final float AUTOFIRE_BONUS = 60f;    
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
    stats.getAutofireAimAccuracy().modifyFlat(id, AUTOFIRE_BONUS * 0.01f);
    //stats.getWeaponRangeThreshold().modifyFlat(id, (Float) mag.get(hullSize)); // increase range threshold.
    }

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) AUTOFIRE_BONUS + "%";
                
                if (index == 1) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
                if (index == 2) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
                if (index == 3) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
                if (index == 4) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
                return null;
	}
}