package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.util.HashMap;
import java.util.Map;

public class shieldwallstats extends BaseShipSystemScript {

	private static Map mag = new HashMap<>();
	static {
		mag.put(HullSize.FRIGATE, 200f);
		mag.put(HullSize.DESTROYER, 400f);
		mag.put(HullSize.CRUISER, 600f);
		mag.put(HullSize.CAPITAL_SHIP, 1000f);
	}
	
	public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, State state, float effectLevel) {
		stats.getArmorBonus().modifyFlat(id, (Float) mag.get(hullSize));
	}

	@Override
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getArmorBonus().unmodify(id);
	}
	
	@Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Last Stand Activated!", false);
        }
        return null;
	}
}