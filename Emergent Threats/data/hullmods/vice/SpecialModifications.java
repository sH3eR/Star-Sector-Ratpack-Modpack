package data.hullmods.vice;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class SpecialModifications extends BaseHullMod {
	
	private static String OLD_MOD = "andrada_mods";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (ship.getOwner() == 0 && ship.getVariant().hasHullMod(OLD_MOD) && isAndradaActive(ship)) {
			ship.getVariant().getHullMods().remove(OLD_MOD);
		}
		else if (ship.getOwner() == 0 && ship.getVariant().hasHullMod(id) && !isAndradaActive(ship)) {
			ship.getVariant().getHullMods().add(OLD_MOD);
			ship.getVariant().getHullMods().remove(id);
		}
	}
	
	private boolean isAndradaActive(ShipAPI ship) {
		if (Global.getSector().getMemoryWithoutUpdate().is("$xo_andrada_is_active", true) && ship.getOwner() == 0) return true;
		else return false;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "fully negates";
		return null;
	}
}