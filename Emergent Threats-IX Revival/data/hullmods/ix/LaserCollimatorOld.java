package data.hullmods.ix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class LaserCollimatorOld extends BaseHullMod {
	
	private static String NEW_MOD = "vice_laser_collimator";
	private static String NEW_WEAPON_ID = "vice_twin_tactical_laser";
	private static String NEW_WEAPON_ALT_ID = "daythorn_tapb_ix";
	private static String OLD_WEAPON_ID = "twin_tactical_laser_ix";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)	{
		ShipVariantAPI variant = stats.getVariant();
		boolean isCoreModEnabled = Global.getSettings().getModManager().isModEnabled("EmergentThreats_Vice");
		if (isCoreModEnabled) {
			variant.getHullMods().add(NEW_MOD);
			LinkedHashSet<String> sMods = variant.getSMods();
			if (sMods.contains(id)) {
				variant.getPermaMods().add(NEW_MOD);
				variant.getSMods().add(NEW_MOD);
			}
		}
		
		Collection<String> fittedWeaponSlots = variant.getFittedWeaponSlots();
		List<String> slotsToChange = new ArrayList<String>();
		for (String slot : fittedWeaponSlots) {
			if (variant.getWeaponId(slot).equals(OLD_WEAPON_ID)) slotsToChange.add(slot);
		}
		for (String slot : slotsToChange) {
			if (isCoreModEnabled) variant.addWeapon(slot, NEW_WEAPON_ID);
			else variant.addWeapon(slot, NEW_WEAPON_ALT_ID);
		}
		
		variant.getSMods().remove(id);
		variant.getPermaMods().remove(id);
		variant.getHullMods().remove(id);
	}
}