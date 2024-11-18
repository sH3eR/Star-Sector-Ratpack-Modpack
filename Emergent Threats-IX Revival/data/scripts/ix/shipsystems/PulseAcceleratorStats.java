package data.scripts.ix.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class PulseAcceleratorStats extends BaseShipSystemScript {

	public static float ROF_BONUS = 1f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		stats.getEnergyRoFMult().modifyMult(id, mult);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyRoFMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}
}