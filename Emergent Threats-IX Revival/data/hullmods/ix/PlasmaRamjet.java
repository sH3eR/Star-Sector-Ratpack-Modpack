package data.hullmods.ix;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class PlasmaRamjet extends BaseHullMod {

	//this hullmod's presence allows the Tigershark (HG) bounty variant to equip the Adaptive Flux Dissipator or Adaptive Entropy Arrester subsystems, improved ship system is new system assigned directly to the hull
	private static float DURATION_INCREASE = 33f;
	private static float SPEED_INCREASE = 20f;
	
	private static String DECO_WEAPON = "antecedent_deco_ix";
	private static String DEM_ID = "vampyr_ix";
	private static String DEM_MOD = "ix_vampyr_retrofit";
	private static String MAIN_SLOT = "WS 004";
	
	//removes Vampyr DEM when retrofit hullmod is not present
	@Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();
		
		if (variant.getWeaponSpec(MAIN_SLOT) == null) {
			deleteDEM();
			return;
		}
		if (variant.getWeaponSpec(MAIN_SLOT).getWeaponId().equals(DEM_ID) && !variant.hasHullMod(DEM_MOD)) {
			variant.clearSlot(MAIN_SLOT);
		}
		deleteDEM();
	}
	
	private void deleteDEM() {
		try {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			for (CargoStackAPI s : cargo.getStacksCopy()) {
				if (s.isWeaponStack() && s.getWeaponSpecIfWeapon().getWeaponId().equals(DEM_ID)) {
					cargo.removeStack(s);	
				}
			}
		}
		catch (Exception e) {}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) DURATION_INCREASE + "%";
		if (index == 1) return "" + (int) SPEED_INCREASE + "%";
		return null;
	}
}