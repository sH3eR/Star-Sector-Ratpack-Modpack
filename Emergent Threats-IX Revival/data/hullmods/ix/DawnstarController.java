package data.hullmods.ix;

import java.util.Collection;
import java.util.Random;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

//equipping Dawnstar weapon adds this controller
//controller scans for handler mods, if none exist, add random reactor and handler
//each handler scans for its reactor mode, if not present, adds next mode and handler and deletes self
//if controller does not see Dawnstar equipped, deletes this mod, all modes, and all handlers
public class DawnstarController extends BaseHullMod {
	
	private static String DAWNSTAR_P = "ix_dawnstar_proton";
	private static String DAWNSTAR_PH = "ix_dawnstar_proton_handler";
	private static String DAWNSTAR_N = "ix_dawnstar_neutron";
	private static String DAWNSTAR_NH = "ix_dawnstar_neutron_handler";
	private static String DAWNSTAR_E = "ix_dawnstar_electron";
	private static String DAWNSTAR_EH = "ix_dawnstar_electron_handler";
	
	private static String CPB_L_ID = "dawnstar_lcpb_ix";
	private static String CPB_H_ID = "dawnstar_hcpb_ix";
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean hasDawnstar = false;
		ShipVariantAPI variant = stats.getVariant();
		Collection<String> weaponSlotList = variant.getFittedWeaponSlots();
		for (String slot : weaponSlotList) {
			String weaponId = variant.getWeaponSpec(slot).getWeaponId();
			if (CPB_L_ID.equals(weaponId) || CPB_H_ID.equals(weaponId)) hasDawnstar = true;
		}
		if (!hasDawnstar) {
			variant.getHullMods().remove(id);
			variant.getHullMods().remove(DAWNSTAR_PH);
			variant.getHullMods().remove(DAWNSTAR_NH);
			variant.getHullMods().remove(DAWNSTAR_EH);
			variant.getHullMods().remove(DAWNSTAR_P);
			variant.getHullMods().remove(DAWNSTAR_N);
			variant.getHullMods().remove(DAWNSTAR_E);
		}
		else if (!variant.hasHullMod(DAWNSTAR_PH) 
				&& !variant.hasHullMod(DAWNSTAR_NH)
				&& !variant.hasHullMod(DAWNSTAR_EH)) {
			Random rand = new Random();
			int mod = rand.nextInt(3);
			if (mod == 0) {
				variant.addMod(DAWNSTAR_P);
				variant.addMod(DAWNSTAR_PH);
			}
			else if (mod == 1) {
				variant.addMod(DAWNSTAR_N);
				variant.addMod(DAWNSTAR_NH);
			}
			else {
				variant.addMod(DAWNSTAR_E);
				variant.addMod(DAWNSTAR_EH);
			}
		}
	}
}