package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class VampyrRetrofit extends BaseHullMod {

	private static String DEM = "Vampyr DEM";
	private static String DEM_ID = "vampyr_ix";
	private static String MAIN_SLOT = "WS 004";
	private static String TIGERSHARK_HULLMOD = "ix_plasma_ramjet";
	
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();
		if (variant.getWeaponSpec(MAIN_SLOT) == null) variant.addWeapon(MAIN_SLOT, DEM_ID);
    }
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		ShipVariantAPI variant = ship.getVariant();
		if (!variant.hasHullMod(TIGERSHARK_HULLMOD)) return false;
		return (variant.getWeaponSpec(MAIN_SLOT) == null 
				|| variant.getWeaponSpec(MAIN_SLOT).getWeaponId().equals(DEM_ID)) ;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		ShipVariantAPI variant = ship.getVariant();
		if (!variant.hasHullMod(TIGERSHARK_HULLMOD)) {
			return "Can only be installed on Tigershark (IX)";
		}
		if (variant.getWeaponSpec(MAIN_SLOT) != null 
				&& !variant.getWeaponSpec(MAIN_SLOT).getWeaponId().equals(DEM_ID)) {
			return "Large composite hardpoint is occupied";
		}
		return null;
	}	
	
	@Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return DEM;
        return null;
    }
}