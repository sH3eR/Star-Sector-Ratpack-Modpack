package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MSS_WarhawkHangarCruiser extends BaseHullMod
{

	public static final int CREW_REQ = 75;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
	{
		stats.getMinCrewMod().modifyFlat(id, CREW_REQ);
		stats.getNumFighterBays().modifyFlat(id, 1f);
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize)
	{
		if (index == 0)
			return "" + CREW_REQ;
		return null;
		// if (index == 0) return "" + ((Float) mag.get(hullSize)).intValue();
		// return null;

	}
}
